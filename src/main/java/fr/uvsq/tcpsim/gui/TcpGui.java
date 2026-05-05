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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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
    private static final Color ACCENT = new Color(31, 78, 121);
    private static final Color MUTED = new Color(96, 108, 129);

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        EventQueue.invokeLater(() -> new TcpGui().show());
    }

    private void show() {
        Config config = new Config();

        JFrame frame = new JFrame("TCP Simulation Studio");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1040, 700));
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout(16, 16));

        JLabel title = new JLabel("TCP Simulation Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("Handshaking, transferts, retransmissions et export CSV dans une interface interactive.");
        subtitle.setForeground(MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 20, 8, 20));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        frame.add(header, BorderLayout.NORTH);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setBorder(new EmptyBorder(12, 12, 12, 12));
        JScrollPane logScroll = new JScrollPane(output);
        logScroll.setBorder(createBorderTitle("Console de simulation"));

        JLabel statusValue = createMetricValue("Prâ”œÂ¬t");
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

        JComboBox<String> presetBox = new JComboBox<>(new String[]{"Dâ”œÂ®faut", "Rapide", "Fiabilitâ”œÂ® â”œÂ®levâ”œÂ®e", "Stress"});

        JButton runBtn = primaryButton("Lancer");
        JButton stopBtn = secondaryButton("Stop");
        JButton exportBtn = secondaryButton("Exporter CSV");
        exportBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        JPanel metricsPanel = new JPanel(new GridBagLayout());
        metricsPanel.setBorder(createBorderTitle("Râ”œÂ®sumâ”œÂ®"));
        metricsPanel.setBackground(PANEL_BG);

        addMetric(metricsPanel, 0, 0, "â”œÃ«tat", statusValue);
        addMetric(metricsPanel, 1, 0, "Paquets reâ”œÂºus", packetsValue);
        addMetric(metricsPanel, 2, 0, "Cycles", summaryValue);
        addMetric(metricsPanel, 0, 1, "Corruptions", corruptionValue);
        addMetric(metricsPanel, 1, 1, "Pertes", lossValue);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(createBorderTitle("Paramâ”œÂ¿tres de simulation"));
        formPanel.setBackground(PANEL_BG);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(new JLabel("Prâ”œÂ®râ”œÂ®glage"), c);
        c.gridx = 1;
        formPanel.add(presetBox, c);

        c.gridy++;
        c.gridx = 0;
        formPanel.add(new JLabel("Paquets demandâ”œÂ®s"), c);
        c.gridx = 1;
        formPanel.add(packetsField, c);

        c.gridy++;
        c.gridx = 0;
        formPanel.add(new JLabel("Fenâ”œÂ¬tre de râ”œÂ®ception"), c);
        c.gridx = 1;
        formPanel.add(windowField, c);

        c.gridy++;
        c.gridx = 0;
        formPanel.add(new JLabel("Corruption (0.0-1.0)"), c);
        c.gridx = 1;
        formPanel.add(corruptionField, c);

        c.gridy++;
        c.gridx = 0;
        formPanel.add(new JLabel("Perte (0.0-1.0)"), c);
        c.gridx = 1;
        formPanel.add(lossField, c);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBackground(PANEL_BG);
        actionsPanel.add(runBtn);
        actionsPanel.add(stopBtn);
        actionsPanel.add(exportBtn);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 14));
        leftPanel.setOpaque(false);
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(metricsPanel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(leftPanel, BorderLayout.NORTH);
        centerPanel.add(logScroll, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(8, 20, 18, 20));
        statusPanel.setOpaque(false);
        statusPanel.add(actionsPanel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);

        AtomicReference<TransferSummary> lastSummaryRef = new AtomicReference<>();
        AtomicReference<TcpClient> clientRef = new AtomicReference<>();
        AtomicReference<Thread> workerRef = new AtomicReference<>();
        AtomicReference<PrintStream> previousOutRef = new AtomicReference<>();

        presetBox.addActionListener(evt -> applyPreset(presetBox, packetsField, windowField, corruptionField, lossField));

        runBtn.addActionListener((ActionEvent e) -> {
            runBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            exportBtn.setEnabled(false);
            output.setText("");
            progressBar.setValue(0);
            statusValue.setText("Exâ”œÂ®cution...");

            PrintStream originalOut = System.out;
            previousOutRef.set(originalOut);
            PrintStream ps = new PrintStream(new TextAreaOutputStream(output, originalOut));
            System.setOut(ps);

            Thread worker = new Thread(() -> {
                try {
                    int total = parseIntOrDefault(packetsField.getText(), config.getDefaultPackets());
                    int window = parseIntOrDefault(windowField.getText(), config.getDefaultWindow());
                    double corruption = parseDoubleOrDefault(corruptionField.getText(), config.getCorruptionProbability());
                    double loss = parseDoubleOrDefault(lossField.getText(), config.getLossProbability());

                    TcpClient client = new TcpClient();
                    clientRef.set(client);

                    TcpServer server = new TcpServer(corruption, loss);
                    client.setTransferListener((received, requested) -> {
                        int percent = (int) Math.round(100.0 * received / Math.max(1, requested));
                        SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
                    });

                    client.resetCancel();
                    client.connect(server);
                    TransferSummary summary = client.requestAllData(server, total, window);
                    client.closeConnection(server);
                    lastSummaryRef.set(summary);

                    SwingUtilities.invokeLater(() -> {
                        if (summary != null) {
                            packetsValue.setText(summary.getReceivedPackets() + " / " + summary.getRequestedPackets());
                            summaryValue.setText(String.valueOf(summary.getCycles()));
                            corruptionValue.setText(String.valueOf(summary.getCorruptedPacketsDetected()));
                            lossValue.setText(String.valueOf(loss));
                            statusValue.setText(summary.isCompleted() ? "Terminâ”œÂ®" : "Interrompu");
                            progressBar.setValue(summary.isCompleted() ? 100 : progressBar.getValue());
                            exportBtn.setEnabled(true);
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> statusValue.setText("Erreur"));
                } finally {
                    System.setOut(previousOutRef.get());
                    SwingUtilities.invokeLater(() -> {
                        runBtn.setEnabled(true);
                        stopBtn.setEnabled(false);
                    });
                }
            }, "tcp-sim-worker");

            workerRef.set(worker);
            worker.start();
        });

        stopBtn.addActionListener((ActionEvent e) -> {
            TcpClient client = clientRef.get();
            if (client != null) {
                client.cancelTransfer();
                appendLine(output, "Interruption demandâ”œÂ®e. Le client va s'arrâ”œÂ¬ter.");
                statusValue.setText("Arrâ”œÂ¬t demandâ”œÂ®");
            } else {
                appendLine(output, "Aucune simulation active.");
            }
        });

        exportBtn.addActionListener((ActionEvent e) -> {
            TransferSummary summary = lastSummaryRef.get();
            if (summary == null) {
                appendLine(output, "Aucun râ”œÂ®sumâ”œÂ® disponible pour l'export.");
                return;
            }

            try {
                Path out = Path.of(config.getExportPath());
                if (!Files.exists(out)) {
                    Files.writeString(out, TransferSummary.csvHeader() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
                Files.writeString(out, summary.toCsvRow() + System.lineSeparator(), StandardOpenOption.APPEND);
                appendLine(output, "Râ”œÂ®sumâ”œÂ® exportâ”œÂ® vers : " + out.toAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                appendLine(output, "Erreur lors de l'export CSV.");
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void applyPreset(JComboBox<String> presetBox,
                                    JTextField packetsField,
                                    JTextField windowField,
                                    JTextField corruptionField,
                                    JTextField lossField) {
        String preset = (String) presetBox.getSelectedItem();
        if ("Rapide".equals(preset)) {
            packetsField.setText("5");
            windowField.setText("2");
            corruptionField.setText("0.15");
            lossField.setText("0.0");
        } else if ("Fiabilitâ”œÂ® â”œÂ®levâ”œÂ®e".equals(preset)) {
            packetsField.setText("8");
            windowField.setText("3");
            corruptionField.setText("0.05");
            lossField.setText("0.0");
        } else if ("Stress".equals(preset)) {
            packetsField.setText("12");
            windowField.setText("2");
            corruptionField.setText("0.35");
            lossField.setText("0.10");
        }
    }

    private static JLabel createMetricValue(String value) {
        JLabel label = new JLabel(value);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        label.setForeground(ACCENT);
        return label;
    }

    private static void addMetric(JPanel panel, int x, int y, String title, JLabel value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(12, 14, 12, 14);
        c.anchor = GridBagConstraints.WEST;

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(223, 229, 236)), new EmptyBorder(10, 12, 10, 12)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        panel.add(card, c);
    }

    private static javax.swing.border.Border createBorderTitle(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(223, 229, 236)), title),
                new EmptyBorder(10, 10, 10, 10));
    }

    private static JPanel borderCard(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(createBorderTitle(title));
        return panel;
    }

    private static JButton primaryButton(String label) {
        JButton button = new JButton(label);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private static JButton secondaryButton(String label) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        return button;
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed < 0 ? defaultValue : parsed;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static void appendLine(JTextArea area, String text) {
        SwingUtilities.invokeLater(() -> area.append(text + System.lineSeparator()));
    }

    private static class TextAreaOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final PrintStream fallback;

        TextAreaOutputStream(JTextArea ta, PrintStream fallback) {
            this.textArea = ta;
            this.fallback = fallback;
        }

        @Override
        public void write(int b) throws IOException {
            fallback.write(b);
            fallback.flush();
            final String s = new String(new byte[]{(byte) b});
            SwingUtilities.invokeLater(() -> textArea.append(s));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            fallback.write(b, off, len);
            fallback.flush();
            final String s = new String(b, off, len);
            SwingUtilities.invokeLater(() -> textArea.append(s));
        }
    }
}
