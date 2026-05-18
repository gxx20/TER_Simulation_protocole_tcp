package fr.uvsq.tcpsim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import fr.uvsq.tcpsim.Config;
import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;

public class TcpGui {
    private static final Color BG = new Color(245, 247, 250);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(220, 226, 235);
    private static final Color ACCENT = new Color(31, 78, 121);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color GREEN = new Color(21, 128, 61);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color RED = new Color(185, 28, 28);
    private static final Color MUTED = new Color(96, 108, 129);

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ignored) {
        }

        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 12));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 12));
        UIManager.put("ComboBox.font", new Font("SansSerif", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("Monospaced", Font.PLAIN, 12));

        EventQueue.invokeLater(() -> new TcpGui().show());
    }

    private void show() {
        Config config = new Config();

        JFrame frame = new JFrame("TCP Simulation Studio");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1040, 700));
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout(16, 16));

        JLabel title = new JLabel("Simulation TCP");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("Pédagogie TCP : Handshake, transfert fiable, retransmission, fermeture.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 6, 20));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        frame.add(header, BorderLayout.NORTH);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Consolas", Font.PLAIN, 12));
        output.setBorder(new EmptyBorder(12, 12, 12, 12));
        output.setBackground(new Color(17, 17, 17));
        output.setForeground(Color.WHITE);
        output.setCaretPosition(0);
        output.setText("Console prête. Cliquez sur 'Lancer' pour démarrer la simulation.\n");
        output.setRows(12);
        output.setColumns(80);

        JScrollPane logScroll = new JScrollPane(output);
        logScroll.setBorder(createCardBorder("Trace complète"));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setPreferredSize(new Dimension(720, 280));

        DefaultListModel<String> eventModel = new DefaultListModel<>();
        JList<String> eventList = new JList<>(eventModel);
        eventList.setBackground(Color.WHITE);

        JScrollPane eventScroll = new JScrollPane(eventList);
        eventScroll.setBorder(createCardBorder("Événements critiques"));
        eventScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        eventScroll.setPreferredSize(new Dimension(300, 280));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, eventScroll);
        split.setDividerLocation(720);
        split.setResizeWeight(0.75);

        JLabel statusValue = createMetricValue("Prêt");
        JLabel packetsValue = createMetricValue("-");
        JLabel summaryValue = createMetricValue("-");
        JLabel corruptionValue = createMetricValue("-");
        JLabel lossValue = createMetricValue("-");

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(280, 20));

        JTextField packetsField = new JTextField(String.valueOf(config.getDefaultPackets()), 8);
        JTextField windowField = new JTextField(String.valueOf(config.getDefaultWindow()), 8);
        JTextField corruptionField = new JTextField(String.valueOf(config.getCorruptionProbability()), 8);
        JTextField lossField = new JTextField(String.valueOf(config.getLossProbability()), 8);

        packetsField.setPreferredSize(new Dimension(150, 26));
        windowField.setPreferredSize(new Dimension(150, 26));
        corruptionField.setPreferredSize(new Dimension(150, 26));
        lossField.setPreferredSize(new Dimension(150, 26));

        JComboBox<String> presetBox = new JComboBox<>(
                new String[]{"Défaut", "Rapide", "Fiabilité élevée", "Stress"});
        presetBox.setPreferredSize(new Dimension(170, 26));
        presetBox.setToolTipText("Choisir un préréglage de simulation");

        JButton runBtn = primaryButton("Lancer");
        JButton stopBtn = secondaryButton("Stop");
        JButton exportBtn = secondaryButton("Exporter CSV");

        runBtn.setPreferredSize(new Dimension(96, 32));
        stopBtn.setPreferredSize(new Dimension(80, 32));
        exportBtn.setPreferredSize(new Dimension(126, 32));

        exportBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        JPanel metricsPanel = createCardPanel("Résumé du transfert");
        metricsPanel.setLayout(new GridBagLayout());
        metricsPanel.setPreferredSize(new Dimension(980, 110));

        addMetric(metricsPanel, 0, 0, "État", statusValue, ACCENT);
        addMetric(metricsPanel, 1, 0, "Paquets reçus", packetsValue, GREEN);
        addMetric(metricsPanel, 2, 0, "Cycles", summaryValue, PRIMARY);
        addMetric(metricsPanel, 3, 0, "Corruptions", corruptionValue, ORANGE);
        addMetric(metricsPanel, 4, 0, "Pertes", lossValue, RED);

        JPanel formPanel = createCardPanel("Configuration");
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(980, 120));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        addFormCell(formPanel, c, 0, 0, "Préréglage", presetBox);
        addFormCell(formPanel, c, 0, 1, "Paquets demandés", packetsField);
        addFormCell(formPanel, c, 1, 0, "Fenêtre de réception", windowField);
        addFormCell(formPanel, c, 1, 1, "Corruption (0.0-1.0)", corruptionField);
        addFormCell(formPanel, c, 2, 0, "Perte (0.0-1.0)", lossField);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBackground(PANEL_BG);
        actionsPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        actionsPanel.add(runBtn);
        actionsPanel.add(stopBtn);
        actionsPanel.add(exportBtn);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 14));
        leftPanel.setOpaque(false);
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(metricsPanel, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(980, 250));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(leftPanel, BorderLayout.NORTH);
        centerPanel.add(split, BorderLayout.CENTER);

        JButton togglePanelsBtn = new JButton("Masquer Panneaux");
        togglePanelsBtn.setPreferredSize(new Dimension(140, 24));
        togglePanelsBtn.addActionListener(evt -> {
            boolean visible = leftPanel.isVisible();
            leftPanel.setVisible(!visible);
            togglePanelsBtn.setText(visible ? "Afficher Panneaux" : "Masquer Panneaux");
            frame.validate();
        });
        actionsPanel.add(togglePanelsBtn, 0);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(8, 20, 18, 20));
        statusPanel.setOpaque(false);
        statusPanel.add(actionsPanel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);

        AtomicReference<TransferSummary> lastSummaryRef = new AtomicReference<>();
        AtomicReference<TcpClient> clientRef = new AtomicReference<>();
        AtomicReference<PrintStream> previousOutRef = new AtomicReference<>();
        java.util.concurrent.atomic.AtomicInteger cyclesCounter =
                new java.util.concurrent.atomic.AtomicInteger(0);

        presetBox.addActionListener(
                evt -> applyPreset(presetBox, packetsField, windowField, corruptionField, lossField));

        runBtn.addActionListener((ActionEvent e) -> {
            runBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            exportBtn.setEnabled(false);

            output.setText("Démarrage de la simulation...\n");
            output.setCaretPosition(output.getDocument().getLength());
            progressBar.setValue(0);
            statusValue.setText("Exécution...");
            cyclesCounter.set(0);

            PrintStream originalOut = System.out;
            previousOutRef.set(originalOut);

            PrintStream ps = new PrintStream(
                    new TextAreaOutputStream(output, originalOut, eventModel, eventList, corruptionValue));
            System.setOut(ps);

            Thread worker = new Thread(() -> {
                try {
                    int total = parseIntOrDefault(packetsField.getText(), config.getDefaultPackets());
                    int window = parseIntOrDefault(windowField.getText(), config.getDefaultWindow());
                    double corruption = parseDoubleOrDefault(
                            corruptionField.getText(), config.getCorruptionProbability());
                    double loss = parseDoubleOrDefault(lossField.getText(), config.getLossProbability());

                    TcpClient client = new TcpClient();
                    clientRef.set(client);

                    TcpServer server = new TcpServer(corruption, loss);

                    client.setTransferListener((received, requested) -> {
                        int percent = (int) Math.round(100.0 * received / Math.max(1, requested));
                        int cycles = cyclesCounter.incrementAndGet();

                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(percent);
                            packetsValue.setText(received + " / " + requested);
                            summaryValue.setText(String.valueOf(cycles));
                        });
                    });

                    client.resetCancel();
                    client.connect(server);

                    TransferSummary summary = client.requestAllData(server, total, window);

                    client.closeConnection(server);
                    lastSummaryRef.set(summary);

                    SwingUtilities.invokeLater(() -> {
                        if (summary != null) {
                            packetsValue.setText(
                                    summary.getReceivedPackets() + " / " + summary.getRequestedPackets());
                            summaryValue.setText(String.valueOf(summary.getCycles()));
                            corruptionValue.setText(
                                    String.valueOf(summary.getCorruptedPacketsDetected()));
                            lossValue.setText(String.valueOf(loss));
                            statusValue.setText(summary.isCompleted() ? "Terminé" : "Interrompu");
                            progressBar.setValue(summary.isCompleted() ? 100 : progressBar.getValue());
                            exportBtn.setEnabled(true);
                        }
                    });
                } catch (RuntimeException ex) {
                    System.err.println("Erreur pendant la simulation : " + ex.getMessage());
                    SwingUtilities.invokeLater(() -> statusValue.setText("Erreur"));
                } finally {
                    System.setOut(previousOutRef.get());
                    SwingUtilities.invokeLater(() -> {
                        runBtn.setEnabled(true);
                        stopBtn.setEnabled(false);
                    });
                }
            }, "tcp-sim-worker");

            worker.start();
        });

        stopBtn.addActionListener((ActionEvent e) -> {
            TcpClient client = clientRef.get();

            if (client != null) {
                client.cancelTransfer();
                appendLine(output, "Interruption demandée. Le client va s'arrêter.");
                statusValue.setText("Arrêt demandé");
            } else {
                appendLine(output, "Aucune simulation active.");
            }
        });

        exportBtn.addActionListener((ActionEvent e) -> {
            TransferSummary summary = lastSummaryRef.get();

            if (summary == null) {
                appendLine(output, "Aucun résumé disponible pour l'export.");
                return;
            }

            try {
                Path out = Paths.get(config.getExportPath());

                Files.write(
                        out,
                        Arrays.asList(TransferSummary.csvHeader(), summary.toCsvRow()),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);

                appendLine(output, "Résumé exporté vers : " + out.toAbsolutePath());
            } catch (IOException ex) {
                System.err.println("Erreur export CSV : " + ex.getMessage());
                appendLine(output, "Erreur lors de l'export CSV.");
            }
        });

        frame.setSize(1280, 840);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void applyPreset(JComboBox<String> presetBox,
                                    JTextField packetsField,
                                    JTextField windowField,
                                    JTextField corruptionField,
                                    JTextField lossField) {
        String preset = (String) presetBox.getSelectedItem();

        switch (preset) {
            case "Rapide":
                packetsField.setText("5");
                windowField.setText("2");
                corruptionField.setText("0.15");
                lossField.setText("0.0");
                break;

            case "Fiabilité élevée":
                packetsField.setText("8");
                windowField.setText("3");
                corruptionField.setText("0.05");
                lossField.setText("0.0");
                break;

            case "Stress":
                packetsField.setText("12");
                windowField.setText("2");
                corruptionField.setText("0.35");
                lossField.setText("0.10");
                break;

            default:
                break;
        }
    }

    private static JLabel createMetricValue(String value) {
        JLabel label = new JLabel(value, javax.swing.SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(ACCENT);
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(120, 24));
        return label;
    }

    private static void addMetric(JPanel panel, int x, int y, String title, JLabel value, Color accentColor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        card.setPreferredSize(new Dimension(150, 66));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        panel.add(card, c);
    }

    private static javax.swing.border.Border createBorderTitle(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(CARD_BORDER), title),
                new EmptyBorder(10, 10, 10, 10));
    }

    private static javax.swing.border.Border createCardBorder(String title) {
        return createBorderTitle(title);
    }

    private static JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(createBorderTitle(title));
        return panel;
    }

    private static void addFormCell(JPanel panel,
                                    GridBagConstraints c,
                                    int row,
                                    int group,
                                    String labelText,
                                    java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setPreferredSize(new Dimension(130, 24));

        int labelCol = group * 2;
        int fieldCol = labelCol + 1;

        c.gridy = row;
        c.gridx = labelCol;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        panel.add(label, c);

        c.gridx = fieldCol;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        panel.add(field, c);
    }

    private static JButton primaryButton(String label) {
        JButton button = new JButton(label);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private static JButton secondaryButton(String label) {
        JButton button = new JButton(label);
        button.setBackground(new Color(229, 231, 235));
        button.setForeground(new Color(55, 65, 81));
        button.setFocusPainted(false);
        return button;
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed < 0 ? defaultValue : parsed;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void appendLine(JTextArea area, String text) {
        SwingUtilities.invokeLater(() -> area.append(text + System.lineSeparator()));
    }

    private static class TextAreaOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final PrintStream fallback;
        private final DefaultListModel<String> eventModel;
        private final JList<String> eventList;
        private final JLabel corruptionLabel;
        private final StringBuilder lineBuffer = new StringBuilder();

        TextAreaOutputStream(JTextArea textArea,
                             PrintStream fallback,
                             DefaultListModel<String> eventModel,
                             JList<String> eventList,
                             JLabel corruptionLabel) {
            this.textArea = textArea;
            this.fallback = fallback;
            this.eventModel = eventModel;
            this.eventList = eventList;
            this.corruptionLabel = corruptionLabel;
        }

        @Override
        public void write(int b) throws IOException {
            fallback.write(b);
            fallback.flush();

            final String s = new String(new byte[]{(byte) b});

            SwingUtilities.invokeLater(() -> {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
                processForEvents(s);
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            fallback.write(b, off, len);
            fallback.flush();

            final String s = new String(b, off, len);

            SwingUtilities.invokeLater(() -> {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
                processForEvents(s);
            });
        }

        private void processForEvents(String chunk) {
            lineBuffer.append(chunk);

            int idx;

            while ((idx = lineBuffer.indexOf("\n")) != -1) {
                String line = lineBuffer.substring(0, idx).trim();
                lineBuffer.delete(0, idx + 1);

                if (line.length() == 0) {
                    continue;
                }

                String lower = line.toLowerCase();

                if (lower.contains("packet{") || lower.contains("packet=")) {
                    continue;
                }

                boolean importantEvent =
                        lower.contains("syn")
                                || lower.contains("fin_ack")
                                || lower.contains("nack")
                                || lower.contains("retransmission")
                                || lower.contains("connexion établie")
                                || lower.contains("connexion fermée")
                                || lower.contains("résumé du transfert")
                                || lower.contains("transfert terminé")
                                || lower.contains("erreur");

                if (importantEvent) {
                    final String ev = line;

                    eventModel.addElement(ev);

                    if (eventModel.size() > 500) {
                        eventModel.remove(0);
                    }

                    int last = eventModel.getSize() - 1;

                    if (last >= 0) {
                        eventList.ensureIndexIsVisible(last);
                    }

                    if (lower.contains("nack")
                            || lower.contains("corrompu")
                            || lower.contains("corruption")) {
                        try {
                            int cur = Integer.parseInt(corruptionLabel.getText());
                            corruptionLabel.setText(String.valueOf(cur + 1));
                        } catch (NumberFormatException ignored) {
                            corruptionLabel.setText("1");
                        }
                    }
                }
            }
        }
    }
}