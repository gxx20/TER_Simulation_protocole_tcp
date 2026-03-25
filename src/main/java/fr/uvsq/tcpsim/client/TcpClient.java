package fr.uvsq.tcpsim.client;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.server.TcpServer;

/* Classe représentant un client TCP */
public class TcpClient {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    /* Constructeur initialisant le client en état CLOSED avec des numéros de séquence et d'acquittement par défaut */
    public TcpClient() {
        this.state = TcpState.CLOSED;
        this.sequenceNumber = 100;
        this.acknowledgementNumber = 0;
    }

    public TcpState getState() {
        return state;
    }


    /* Méthode pour établir une connexion avec un serveur TCP en suivant le processus de handshake en trois étapes */
    public void connect(TcpServer server) {
        System.out.println("[CLIENT]: Début de la demande de connexion.");

        /* Le client commence en état CLOSED et envoie un paquet SYN pour initier la connexion */
        Packet synPacket = new Packet(
                PacketType.SYN,
                sequenceNumber,
                0,
                null
        );

        state = TcpState.SYN_SENT;
        System.out.println("[CLIENT]: Envoi de SYN");

        Packet serverResponse = server.receivePacket(synPacket);

        /* Si le client reçoit un SYN-ACK, il répond avec un ACK pour établir la connexion */
        if (serverResponse != null && serverResponse.getType() == PacketType.SYN_ACK) {
            System.out.println("[CLIENT]: Réception de SYN_ACK");

            acknowledgementNumber = serverResponse.getSequenceNumber() + 1;

            Packet ackPacket = new Packet(
                    PacketType.ACK,
                    sequenceNumber + 1,
                    acknowledgementNumber,
                    null
            );

            state = TcpState.ESTABLISHED;
            System.out.println("[CLIENT]: Envoi de ACK");

            server.receivePacket(ackPacket);

            System.out.println("[CLIENT]: Connexion établie.");
        } else {
            System.out.println("[CLIENT]: Échec de l'ouverture de connexion.");
        }
    }
}