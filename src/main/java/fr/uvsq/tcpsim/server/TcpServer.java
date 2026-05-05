package fr.uvsq.tcpsim.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;

/// Classe reprâ”œÂ®sentant le serveur TCP dans la simulation
public class TcpServer {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;
    private double corruptionProbability;
    private double lossProbability;
    private final Random random;

    private List<String> sourceData;
    private List<Packet> sendBuffer;
    private int nextDataIndex;

    // Constructeur du serveur TCP
    public TcpServer() {
        this(0.25, 0.0);
    }

    public TcpServer(double corruptionProbability, double lossProbability) {
        this.state = TcpState.LISTEN;
        this.sequenceNumber = 500;
        this.acknowledgementNumber = 0;
        this.sourceData = new ArrayList<>();
        this.sendBuffer = new ArrayList<>();
        this.nextDataIndex = 0;
        this.corruptionProbability = corruptionProbability;
        this.lossProbability = lossProbability;
        this.random = new Random();

        initializeSourceData();
    }

    // Initialisation de la mâ”œÂ®moire source avec des donnâ”œÂ®es fictives
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

    // Mâ”œÂ®thode pour recevoir un paquet du client et râ”œÂ®pondre en fonction de l'â”œÂ®tat actuel du serveur
    public Packet receivePacket(Packet packet) {
        System.out.println("[SERVEUR] Paquet reâ”œÂºu : " + packet);

        //si le serveur est en â”œÂ®coute et reâ”œÂºoit un SYN, il râ”œÂ®pond avec un SYN-ACK et passe â”œÃ¡ l'â”œÂ®tat SYN_RECEIVED
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

        //si le serveur est en SYN_RECEIVED et reâ”œÂºoit un ACK, il passe â”œÃ¡ l'â”œÂ®tat ESTABLISHED et la connexion est â”œÂ®tablie
        if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
            state = TcpState.ESTABLISHED;
            System.out.println("[SERVEUR] Connexion â”œÂ®tablie.");
            return null;
        }

        //si le serveur est en ESTABLISHED et reâ”œÂºoit un FIN, il râ”œÂ®pond avec un FIN-ACK et passe â”œÃ¡ l'â”œÂ®tat CLOSE_WAIT
        if (state == TcpState.ESTABLISHED && packet.getType() == PacketType.FIN) {
            acknowledgementNumber = packet.getSequenceNumber() + 1;
            state = TcpState.CLOSE_WAIT;

            Packet response = new Packet(
                    PacketType.FIN_ACK,
                    sequenceNumber + 100,
                    acknowledgementNumber,
                    null
            );

            System.out.println("[SERVEUR] Râ”œÂ®ception de FIN, passage â”œÃ¡ CLOSE_WAIT");
            System.out.println("[SERVEUR] Envoi de FIN_ACK");
            state = TcpState.LAST_ACK;
            return response;
        }

        //si le serveur est en LAST_ACK et reâ”œÂºoit un ACK, il passe â”œÃ¡ l'â”œÂ®tat CLOSED et la connexion est fermâ”œÂ®e
        if (state == TcpState.LAST_ACK && packet.getType() == PacketType.ACK) {
            state = TcpState.CLOSED;
            System.out.println("[SERVEUR] ACK final reâ”œÂºu. Connexion fermâ”œÂ®e.");
            return null;
        }

        System.out.println("[SERVEUR] Paquet inattendu dans l'â”œÂ®tat " + state);
        return null;
    }

    public void resetTransferCursor() {
        this.nextDataIndex = 0;
    }

    // Mâ”œÂ®thode pour envoyer des donnâ”œÂ®es au client en fonction de la demande de transfert
    public TransferResult sendData(TransferRequest request) {
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[SERVEUR] Impossible d'envoyer des donnâ”œÂ®es : connexion non â”œÂ®tablie.");
            return new TransferResult(new ArrayList<>(), request.getNumberOfPacketsRequested());
        }

        sendBuffer.clear();

        int requestedPackets = request.getNumberOfPacketsRequested();
        int receiveWindow = request.getReceiveWindow();

        int availablePackets = sourceData.size() - nextDataIndex;
        int packetsToSend = Math.min(requestedPackets, receiveWindow);
        packetsToSend = Math.min(packetsToSend, availablePackets);

        System.out.println("[SERVEUR] Demande reâ”œÂºue : " + request);
        System.out.println("[SERVEUR] Index courant dans la mâ”œÂ®moire source : " + nextDataIndex);
        System.out.println("[SERVEUR] Nombre de paquets pouvant â”œÂ¬tre envoyâ”œÂ®s maintenant : " + packetsToSend);

        // Gâ”œÂ®nâ”œÂ®ration des paquets de donnâ”œÂ®es â”œÃ¡ envoyer
        for (int i = 0; i < packetsToSend; i++) {
            String data = sourceData.get(nextDataIndex);

            Packet dataPacket = new Packet(
                    PacketType.DATA,
                    sequenceNumber + nextDataIndex + 1,
                    0,
                    data
            );

            // Simulate loss
            boolean lost = random.nextDouble() < lossProbability;
            if (lost) {
                // do not add packet to send buffer (simulates loss)
                nextDataIndex++;
                continue;
            }

            // Simulate corruption
            boolean corrupted = random.nextDouble() < corruptionProbability;
            dataPacket.setCorrupted(corrupted);

            sendBuffer.add(dataPacket);
            nextDataIndex++;
        }

        int remainingPackets = requestedPackets - packetsToSend;
        if (remainingPackets < 0) {
            remainingPackets = 0;
        }

        System.out.println("[SERVEUR] Paquets placâ”œÂ®s dans le buffer d'envoi :");
        for (Packet packet : sendBuffer) {
            System.out.println("    " + packet);
        }

        return new TransferResult(new ArrayList<>(sendBuffer), remainingPackets);
    }

    // Mâ”œÂ®thode pour retransmettre un paquet spâ”œÂ®cifique en cas de perte ou de corruption
    public Packet retransmitPacket(int sequenceNumberToRetransmit) {
        int sourceIndex = sequenceNumberToRetransmit - sequenceNumber - 1;

        // Vâ”œÂ®rification que le numâ”œÂ®ro de sâ”œÂ®quence â”œÃ¡ retransmettre correspond â”œÃ¡ un paquet valide dans la mâ”œÂ®moire source
        if (sourceIndex < 0 || sourceIndex >= sourceData.size()) {
            System.out.println("[SERVEUR] Impossible de retransmettre : numâ”œÂ®ro de sâ”œÂ®quence invalide.");
            return null;
        }

        String data = sourceData.get(sourceIndex);

        Packet retransmittedPacket = new Packet(
                PacketType.DATA,
                sequenceNumberToRetransmit,
                0,
                data
        );

        retransmittedPacket.setCorrupted(false);

        System.out.println("[SERVEUR] Retransmission du paquet : " + retransmittedPacket);
        return retransmittedPacket;
    }
}
