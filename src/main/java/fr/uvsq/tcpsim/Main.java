package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.server.TcpServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Simulation du protocole TCP");

        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer();

        System.out.println("Etat initial du client : " + client.getState());
        System.out.println("Etat initial du serveur : " + server.getState());
        System.out.println();

        client.connect(server);

        System.out.println();
        System.out.println("Etat final du client : " + client.getState());
        System.out.println("Etat final du serveur : " + server.getState());
    }
}