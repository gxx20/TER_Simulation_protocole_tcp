package fr.uvsq.tcpsim.client;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.server.TcpServer;

// Classe représentant un client TCP
public class TcpClient {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    // Buffer temporaire de réception
    private List<Packet> receiveBuffer;

    // Constructeur initialisant le client en état CLOSED avec des numéros de séquence et d'acquittement par défaut
    public TcpClient() {
        this.state = TcpState.CLOSED;
        this.sequenceNumber = 100;
        this.acknowledgementNumber = 0;
        this.receiveBuffer = new ArrayList<>();
    }

    public TcpState getState() {
        return state;
    }

    public List<Packet> getReceiveBuffer() {
        return receiveBuffer;
    }

    // Méthode pour initier une connexion avec le serveur en suivant le processus de handshake en trois étapes
    public void connect(TcpServer server) {
        System.out.println("[CLIENT]: Debut de la demande de connexion.");

        Packet synPacket = new Packet(
                PacketType.SYN,
                sequenceNumber,
                0,
                null
        );

        state = TcpState.SYN_SENT;
        System.out.println("[CLIENT]: Envoi de SYN");

        Packet serverResponse = server.receivePacket(synPacket);

        // Si le client reçoit un SYN-ACK du serveur, il répond avec un ACK et passe à l'état ESTABLISHED
        if (serverResponse != null && serverResponse.getType() == PacketType.SYN_ACK) {
            System.out.println("[CLIENT]: Reception de SYN_ACK");

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

            System.out.println("[CLIENT]: Connexion etablie.");
        // Si le client ne reçoit pas de SYN-ACK ou reçoit une réponse inattendue, la connexion échoue
        } else {
            System.out.println("[CLIENT]: Echec de l'ouverture de connexion.");
        }
    }

    // Méthode pour demander des données au serveur en fonction de l'état actuel du client
    public void requestData(TcpServer server, int numberOfPackets, int receiveWindow) {
        // Vérifier que la connexion est établie avant de faire une demande de transfert
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[CLIENT]: Impossible de demander des donnees : connexion non etablie.");
            return;
        }

        receiveBuffer.clear();

        // Créer une demande de transfert avec le nombre de paquets souhaité et la fenêtre de réception
        TransferRequest request = new TransferRequest(numberOfPackets, receiveWindow);
        System.out.println("[CLIENT]: Envoi d'une requete de transfert : " + request);

        TransferResult result = server.sendData(request);

        List<Packet> receivedPackets = result.getSentPackets();

        // Placer les paquets reçus dans le buffer de réception du client
        for (Packet packet : receivedPackets) {
            receiveBuffer.add(packet);
        }

        System.out.println("[CLIENT]: Paquets reçus dans le buffer de reception :");
        for (Packet packet : receiveBuffer) {
            System.out.println("    " + packet);
        }

        System.out.println("[CLIENT]: Nombre de paquets restant a recevoir plus tard : " + result.getRemainingPackets());

        sendAcknowledgements();
    }

    private void sendAcknowledgements() {
        if (receiveBuffer.isEmpty()) {
            System.out.println("[CLIENT]: Aucun paquet recu, aucun ACK envoye.");
            return;
        }

        System.out.println("[CLIENT] Envoi des ACK pour les paquets recus.");
        for (Packet packet : receiveBuffer) {
            System.out.println("ACK pour le paquet de sequence " + packet.getSequenceNumber());
        }
    }
}