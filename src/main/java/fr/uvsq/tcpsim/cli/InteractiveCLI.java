package fr.uvsq.tcpsim.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.server.TcpServer;
import fr.uvsq.tcpsim.model.TransferSummary;

/**
 * Simple interactive CLI for the TCP simulation.
 */
public class InteractiveCLI {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("TCP Simulation - Interactive CLI");

		while (true) {
			System.out.println();
			System.out.println("Menu :");
			System.out.println("1) Lancer une simulation");
			System.out.println("2) Quitter");
			System.out.print("Choix: ");

			String choice = readLineOrNull(scanner);
			if (choice == null) {
				System.out.println("Entrée fermée. Arrêt du programme.");
				break;
			}

			if (choice.equals("1")) {
				fr.uvsq.tcpsim.Config config = new fr.uvsq.tcpsim.Config();

				System.out.print("Nombre total de paquets à demander (défaut " + config.getDefaultPackets() + "): ");
				String tp = readLineOrDefault(scanner, "");
				int total = tp.isEmpty()
						? config.getDefaultPackets()
						: parsePositiveInt(tp, config.getDefaultPackets());

				System.out.print("Taille de la fenêtre de réception (défaut " + config.getDefaultWindow() + "): ");
				String rw = readLineOrDefault(scanner, "");
				int window = rw.isEmpty() ? config.getDefaultWindow() : parsePositiveInt(rw, config.getDefaultWindow());

				System.out.print(
						"Probabilité de corruption (0.0-1.0, défaut " + config.getCorruptionProbability() + "): ");
				String cp = readLineOrDefault(scanner, "");
				double corruption = cp.isEmpty()
						? config.getCorruptionProbability()
						: parseProbability(cp, config.getCorruptionProbability());

				System.out.print("Probabilité de perte (0.0-1.0, défaut " + config.getLossProbability() + "): ");
				String lp = readLineOrDefault(scanner, "");
				double loss = lp.isEmpty()
						? config.getLossProbability()
						: parseProbability(lp, config.getLossProbability());

				TcpClient client = new TcpClient();
				TcpServer server = new TcpServer(corruption, loss);

				System.out.println();
				System.out.println("--- Démarrage de la simulation ---");
				client.connect(server);
				TransferSummary summary = client.requestAllData(server, total, window);
				client.closeConnection(server);

				System.out.println();
				System.out.println("--- Résumé ---");
				System.out.println(summary == null ? "Aucun résumé disponible." : summary);
				System.out.println("--- Fin ---");

				System.out.print("Exporter le résumé en CSV ? (o/N): ");
				String exp = readLineOrDefault(scanner, "").trim().toLowerCase();
				if (exp.equals("o") || exp.equals("y")) {
					try {
						if (summary == null) {
							System.out.println("Aucun résumé disponible, export annulé.");
						} else {
							Path out = Path.of(config.getExportPath());
							String header = fr.uvsq.tcpsim.model.TransferSummary.csvHeader();
							String row = summary.toCsvRow();
							if (out.getParent() != null) {
								Files.createDirectories(out.getParent());
							}
							if (!Files.exists(out)) {
								Files.writeString(out, header + System.lineSeparator(), StandardOpenOption.CREATE,
										StandardOpenOption.WRITE);
							}
							Files.writeString(out, row + System.lineSeparator(), StandardOpenOption.APPEND);
							System.out.println("Exporté vers " + out.toAbsolutePath());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			} else if (choice.equals("2")) {
				System.out.println("Au revoir.");
				break;
			} else {
				System.out.println("Choix invalide.");
			}
		}

		scanner.close();
	}

	private static int parsePositiveInt(String s, int def) {
		try {
			int v = Integer.parseInt(s);
			return v > 0 ? v : def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static String readLineOrNull(Scanner scanner) {
		try {
			return scanner.nextLine().trim();
		} catch (Exception e) {
			return null;
		}
	}

	private static String readLineOrDefault(Scanner scanner, String defaultValue) {
		String value = readLineOrNull(scanner);
		return value == null ? defaultValue : value;
	}

	private static double parseProbability(String s, double def) {
		try {
			double value = Double.parseDouble(s);
			return (value >= 0.0 && value <= 1.0) ? value : def;
		} catch (NumberFormatException e) {
			return def;
		}
	}
}
