package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.server.TcpServer;
import fr.uvsq.tcpsim.model.TransferSummary;

public class Main {
    public static void main(String[] args) {
        System.out.println("  Simulation du protocole TCP");

        fr.uvsq.tcpsim.Config config = new fr.uvsq.tcpsim.Config();

        int totalPacketsRequested = args != null && args.length > 0 ? parseArgument(args, 0, config.getDefaultPackets()) : config.getDefaultPackets();
        int receiveWindow = args != null && args.length > 1 ? parseArgument(args, 1, config.getDefaultWindow()) : config.getDefaultWindow();
        System.out.println("Paramâ”œÂ¿tres de transfert : " + totalPacketsRequested + " paquet(s), fenâ”œÂ¬tre=" + receiveWindow);

        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(config.getCorruptionProbability(), config.getLossProbability());

        System.out.println("Etat initial du client : " + client.getState());
        System.out.println("Etat initial du serveur : " + server.getState());
        System.out.println();

        client.connect(server);

        System.out.println();
        System.out.println("Etat aprâ”œÂ¿s ouverture - client : " + client.getState());
        System.out.println("Etat aprâ”œÂ¿s ouverture - serveur : " + server.getState());

        System.out.println();
        TransferSummary summary = client.requestAllData(server, totalPacketsRequested, receiveWindow);

        System.out.println();
        client.closeConnection(server);

        System.out.println();
        System.out.println("Etat final du client : " + client.getState());
        System.out.println("Etat final du serveur : " + server.getState());
    }

    private static int parseArgument(String[] args, int index, int defaultValue) {
        if (args == null || args.length <= index) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException exception) {
            System.out.println("Argument invalide â”œÃ¡ l'index " + index + ", valeur par dâ”œÂ®faut utilisâ”œÂ®e : " + defaultValue);
            return defaultValue;
        }
    }
}
