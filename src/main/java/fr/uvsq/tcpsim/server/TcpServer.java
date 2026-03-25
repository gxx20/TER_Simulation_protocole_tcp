package fr.uvsq.tcpsim.server;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;

/* Classe représentant un serveur TCP */
public class TcpServer {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    /* Constructeur initialisant le serveur en état LISTEN avec des numéros de séquence et d'acquittement par défaut */
    public TcpServer() {
        this.state = TcpState.LISTEN;
        this.sequenceNumber = 500;
        this.acknowledgementNumber = 0;
    }

    /* Getter pour l'état actuel du serveur */
    public TcpState getState() {
        return state;
    }

    /* Méthode pour recevoir un paquet et répondre en fonction de l'état actuel du serveur */
    public Packet receivePacket(Packet packet) {
        System.out.println("[SERVEUR]: Paquet reçu : " + packet);

        /* Si le serveur est en état LISTEN et reçoit un paquet SYN, il répond avec un paquet SYN-ACK */
        if (state == TcpState.LISTEN && packet.getType() == PacketType.SYN) {
            acknowledgementNumber = packet.getSequenceNumber() + 1;
            state = TcpState.SYN_RECEIVED;

            Packet response = new Packet(
                    PacketType.SYN_ACK,
                    sequenceNumber,
                    acknowledgementNumber,
                    null
            );

            System.out.println("[SERVEUR]: Envoi de SYN_ACK");
            return response;
        }

        /* Si le serveur est en état SYN_RECEIVED et reçoit un paquet ACK, la connexion est établie */
        if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
            state = TcpState.ESTABLISHED;
            System.out.println("[SERVEUR]: Connexion établie.");
            return null;
        }

        System.out.println("[SERVEUR]: Paquet inattendu dans l'état " + state);
        return null;
    }
}