package fr.uvsq.tcpsim.server;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;

public class TcpServer {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    // Données source du serveur
    private List<String> sourceData;

    // Buffer temporaire d'envoi
    private List<Packet> sendBuffer;

    // Position du prochain bloc à envoyer dans la mémoire source
    private int nextDataIndex;

    public TcpServer() {
        this.state = TcpState.LISTEN;
        this.sequenceNumber = 500;
        this.acknowledgementNumber = 0;
        this.sourceData = new ArrayList<>();
        this.sendBuffer = new ArrayList<>();
        this.nextDataIndex = 0;

        initializeSourceData();
    }

    // Initialisation de la mémoire source avec des blocs de données fictifs
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

    // Méthode pour recevoir un paquet du client et répondre en fonction de l'état actuel du serveur et du type de paquet reçu
    public Packet receivePacket(Packet packet) {
        System.out.println("[SERVEUR]: Paquet recu : " + packet);

        // Si le serveur est en état LISTEN et reçoit un paquet SYN, il répond avec un SYN-ACK et passe à l'état SYN_RECEIVED
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

        // Si le serveur est en état SYN_RECEIVED et reçoit un paquet ACK, il passe à l'état ESTABLISHED et confirme l'établissement de la connexion
        if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
            state = TcpState.ESTABLISHED;
            System.out.println("[SERVEUR]: Connexion etablie.");
            return null;
        }

        System.out.println("[SERVEUR]: Paquet inattendu dans l'etat " + state);
        return null;
    }

    public void resetTransferCursor() {
        this.nextDataIndex = 0;
    }

    // Méthode pour traiter une demande de transfert de données du client en fonction de l'état actuel du serveur
    public TransferResult sendData(TransferRequest request) {
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[SERVEUR]: Impossible d'envoyer des donnees : connexion non etablie.");
            return new TransferResult(new ArrayList<>(), request.getNumberOfPacketsRequested());
        }

        sendBuffer.clear();

        int requestedPackets = request.getNumberOfPacketsRequested();
        int receiveWindow = request.getReceiveWindow();

        int availablePackets = sourceData.size() - nextDataIndex;
        int packetsToSend = Math.min(requestedPackets, receiveWindow);
        packetsToSend = Math.min(packetsToSend, availablePackets);

        System.out.println("[SERVEUR]: Demande recue : " + request);
        System.out.println("[SERVEUR]: Index courant dans la memoire source : " + nextDataIndex);
        System.out.println("[SERVEUR]: Nombre de paquets pouvant être envoyes maintenant : " + packetsToSend);

        // Placer les paquets à envoyer dans le buffer d'envoi
        for (int i = 0; i < packetsToSend; i++) {
            String data = sourceData.get(nextDataIndex);

            Packet dataPacket = new Packet(
                    PacketType.DATA,
                    sequenceNumber + nextDataIndex + 1,
                    0,
                    data
            );

            sendBuffer.add(dataPacket);
            nextDataIndex++;
        }

        int remainingPackets = requestedPackets - packetsToSend;
        if (remainingPackets < 0) {
            remainingPackets = 0;
        }

        System.out.println("[SERVEUR]: Paquets places dans le buffer d'envoi :");
        for (Packet packet : sendBuffer) {
            System.out.println("    " + packet);
        }

        return new TransferResult(new ArrayList<>(sendBuffer), remainingPackets);
    }
}