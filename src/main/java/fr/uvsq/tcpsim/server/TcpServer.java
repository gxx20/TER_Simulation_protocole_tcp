package fr.uvsq.tcpsim.server;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;

// Classe représentant un serveur TCP qui peut recevoir des demandes de connexion, envoyer des données et gérer les retransmissions
public class TcpServer {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    private List<String> sourceData;
    private List<Packet> sendBuffer;
    private int nextDataIndex;

    // Constructeur initialisant l'état du serveur, les buffers et la mémoire source de données
    public TcpServer() {
        this.state = TcpState.LISTEN;
        this.sequenceNumber = 500;
        this.acknowledgementNumber = 0;
        this.sourceData = new ArrayList<>();
        this.sendBuffer = new ArrayList<>();
        this.nextDataIndex = 0;

        initializeSourceData();
    }

    // Méthode pour initialiser la mémoire source de données avec des blocs de données simulés
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
        System.out.println("[SERVEUR] Paquet reçu : " + packet);

        // Si le serveur reçoit un SYN alors qu'il est en écoute, il répond avec un SYN_ACK et passe à l'état SYN_RECEIVED
        if (state == TcpState.LISTEN && packet.getType() == PacketType.SYN) {
            acknowledgementNumber = packet.getSequenceNumber() + 1;
            state = TcpState.SYN_RECEIVED;

            Packet response = new Packet(
                    PacketType.SYN_ACK,
                    sequenceNumber,
                    acknowledgementNumber,
                    null
            );

            System.out.println("[SERVEUR] Envoi de SYN_ACK");
            return response;
        }

        // Si le serveur reçoit un ACK après avoir envoyé un SYN_ACK, la connexion est établie
        if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
            state = TcpState.ESTABLISHED;
            System.out.println("[SERVEUR] Connexion établie.");
            return null;
        }

        System.out.println("[SERVEUR] Paquet inattendu dans l'état " + state);
        return null;
    }

    public void resetTransferCursor() {
        this.nextDataIndex = 0;
    }

    // Méthode pour envoyer des données au client en fonction de la demande de transfert reçue, en respectant la fenêtre de réception du client et en simulant une corruption de certains paquets
    public TransferResult sendData(TransferRequest request) {
        // Si la connexion n'est pas établie, le serveur ne peut pas envoyer de données
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[SERVEUR] Impossible d'envoyer des données : connexion non établie.");
            return new TransferResult(new ArrayList<>(), request.getNumberOfPacketsRequested());
        }

        sendBuffer.clear();

        int requestedPackets = request.getNumberOfPacketsRequested();
        int receiveWindow = request.getReceiveWindow();

        int availablePackets = sourceData.size() - nextDataIndex;
        int packetsToSend = Math.min(requestedPackets, receiveWindow);
        packetsToSend = Math.min(packetsToSend, availablePackets);

        System.out.println("[SERVEUR] Demande reçue : " + request);
        System.out.println("[SERVEUR] Index courant dans la mémoire source : " + nextDataIndex);
        System.out.println("[SERVEUR] Nombre de paquets pouvant être envoyés maintenant : " + packetsToSend);

        // Génération des paquets à envoyer en fonction de la demande et de la fenêtre de réception, avec une simulation de corruption pour les paquets dont le numéro de séquence est pair
        for (int i = 0; i < packetsToSend; i++) {
            String data = sourceData.get(nextDataIndex);

            Packet dataPacket = new Packet(
                    PacketType.DATA,
                    sequenceNumber + nextDataIndex + 1,
                    0,
                    data
            );

            // Simulation simple d'une corruption :
            // ici, on corrompt volontairement les paquets dont le numéro de séquence est pair
            if (dataPacket.getSequenceNumber() % 2 == 0) {
                dataPacket.setCorrupted(true);
            }

            sendBuffer.add(dataPacket);
            nextDataIndex++;
        }

        int remainingPackets = requestedPackets - packetsToSend;
        if (remainingPackets < 0) {
            remainingPackets = 0;
        }

        System.out.println("[SERVEUR] Paquets placés dans le buffer d'envoi :");
        for (Packet packet : sendBuffer) {
            System.out.println("    " + packet);
        }

        return new TransferResult(new ArrayList<>(sendBuffer), remainingPackets);
    }

    // Méthode pour retransmettre un paquet spécifique en fonction de son numéro de séquence, en vérifiant que le numéro de séquence est valide par rapport à la mémoire source de données
    public Packet retransmitPacket(int sequenceNumberToRetransmit) {
        int sourceIndex = sequenceNumberToRetransmit - sequenceNumber - 1;

        // Vérification que le numéro de séquence à retransmettre correspond à un index valide dans la mémoire source de données
        if (sourceIndex < 0 || sourceIndex >= sourceData.size()) {
            System.out.println("[SERVEUR] Impossible de retransmettre : numéro de séquence invalide.");
            return null;
        }

        String data = sourceData.get(sourceIndex);

        // Création d'un nouveau paquet de données pour la retransmission, en supposant que la retransmission réussit et que le paquet n'est plus corrompu
        Packet retransmittedPacket = new Packet(
                PacketType.DATA,
                sequenceNumberToRetransmit,
                0,
                data
        );

        // On considère qu'une retransmission réussit et n'est plus corrompue
        retransmittedPacket.setCorrupted(false);

        System.out.println("[SERVEUR] Retransmission du paquet : " + retransmittedPacket);
        return retransmittedPacket;
    }
}