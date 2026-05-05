package fr.uvsq.tcpsim;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
	private double corruptionProbability = 0.25;
	private double lossProbability = 0.0;
	private int defaultPackets = 7;
	private int defaultWindow = 3;
	private String exportPath = "transfer_summary.csv";

	public Config() {
		Path p = Path.of("config.properties");
		if (Files.exists(p)) {
			Properties props = new Properties();
			try (FileInputStream in = new FileInputStream(p.toFile())) {
				props.load(in);
				corruptionProbability = Double.parseDouble(props.getProperty("corruptionProbability", "0.25"));
				lossProbability = Double.parseDouble(props.getProperty("lossProbability", "0.0"));
				defaultPackets = Integer.parseInt(props.getProperty("defaultPackets", "7"));
				defaultWindow = Integer.parseInt(props.getProperty("defaultWindow", "3"));
				exportPath = props.getProperty("exportPath", exportPath);
			} catch (IOException | NumberFormatException e) {
				// ignore and use defaults
			}
		}
	}

	public double getCorruptionProbability() {
		return corruptionProbability;
	}
	public double getLossProbability() {
		return lossProbability;
	}
	public int getDefaultPackets() {
		return defaultPackets;
	}
	public int getDefaultWindow() {
		return defaultWindow;
	}
	public String getExportPath() {
		return exportPath;
	}
}
