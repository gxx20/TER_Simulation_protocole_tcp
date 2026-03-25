package fr.uvsq.tcpsim.server;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;

// Classe représentant un serveur TCP
public class TcpServer {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    // Données source du serveur
    private List<String> sourceData;

    // Buffer temporaire d'envoi
    private List<Packet> sendBuffer;

    /* Constructeur initialisant le serveur en état LISTEN avec des numéros de séquence et d'acquittement par défaut */
    public TcpServer() {
        this.state = TcpState.LISTEN;
        this.sequenceNumber = 500;
        this.acknowledgementNumber = 0;
        this.sourceData = new ArrayList<>();
        this.sendBuffer = new ArrayList<>();
        initializeSourceData();
    }

    // Méthode pour initialiser les données source du serveur
    private void initializeSourceData() {
        sourceData.add("Bloc-1");
        sourceData.add("Bloc-2");
        sourceData.add("Bloc-3");
        sourceData.add("Bloc-4");
        sourceData.add("Bloc-5");
        sourceData.add("Bloc-6");
        sourceData.add("Bloc-7");
        sourceData.add("Bloc-8");
    }

    public TcpState getState() {
        return state;
    }

    // Méthode pour recevoir un paquet du client et répondre en fonction de l'état actuel du serveur
    public Packet receivePacket(Packet packet) {
        System.out.println("[SERVEUR]: Paquet reçu : " + packet);

        // processus de handshake en trois étapes
        // Si le serveur reçoit un SYN en état LISTEN, il répond avec un SYN-ACK et passe à l'état SYN_RECEIVED
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

        // Si le serveur reçoit un ACK en état SYN_RECEIVED, il passe à l'état ESTABLISHED
        if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
            state = TcpState.ESTABLISHED;
            System.out.println("[SERVEUR]: Connexion établie.");
            return null;
        }

        System.out.println("[SERVEUR]: Paquet inattendu dans l'état " + state);
        return null;
    }

    // Méthode pour envoyer des données au client en fonction d'une demande de transfert et de l'état actuel du serveur
    public TransferResult sendData(TransferRequest request) {
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[SERVEUR]: Impossible d'envoyer des données : connexion non établie.");
            return new TransferResult(new ArrayList<>(), request.getNumberOfPacketsRequested());
        }

        sendBuffer.clear();

        int requestedPackets = request.getNumberOfPacketsRequested();
        int receiveWindow = request.getReceiveWindow();

        int packetsToSend = Math.min(requestedPackets, receiveWindow);
        packetsToSend = Math.min(packetsToSend, sourceData.size());

        System.out.println("[SERVEUR]: Demande reçue : " + request);
        System.out.println("[SERVEUR]: Nombre de paquets pouvant être envoyés maintenant : " + packetsToSend);

        // Préparer les paquets à envoyer en fonction de la demande du client et des données disponibles
        for (int i = 0; i < packetsToSend; i++) {
            String data = sourceData.get(i);

            Packet dataPacket = new Packet(
                    PacketType.DATA,
                    sequenceNumber + i + 1,
                    0,
                    data
            );

            sendBuffer.add(dataPacket);
        }

        int remainingPackets = requestedPackets - packetsToSend;
        if (remainingPackets < 0) {
            remainingPackets = 0;
        }

        System.out.println("[SERVEUR]: Paquets placés dans le buffer d'envoi :");
        for (Packet packet : sendBuffer) {
            System.out.println("    " + packet);
        }

        return new TransferResult(new ArrayList<>(sendBuffer), remainingPackets);
    }
}