package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;


public class Main {
    public static void main(String[] args) {
        System.out.println("Simulation du protocole TCP");

        TcpState clientState = TcpState.CLOSED;
        Packet synPacket = new Packet(PacketType.SYN, 100, 0, null);

        System.out.println("Etat initial du client : " + clientState);
        System.out.println("Paquet créé : " + synPacket);
    }
}