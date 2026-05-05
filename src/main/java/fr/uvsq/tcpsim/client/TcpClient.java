package fr.uvsq.tcpsim.client;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;

import fr.uvsq.tcpsim.client.TransferListener;

// Classe reprâ”œÂ®sentant le client TCP dans la simulation
public class TcpClient {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    private List<Packet> receiveBuffer;
    private List<Packet> receivedData;
    private TransferSummary lastTransferSummary;
    private int transferCorruptedPacketsDetected;
    private int transferRetransmissionsPerformed;
    private volatile boolean canceled = false;
    private TransferListener transferListener;

    // Constructeur du client TCP
    public TcpClient() {
        this.state = TcpState.CLOSED;
        this.sequenceNumber = 100;
        this.acknowledgementNumber = 0;
        this.receiveBuffer = new ArrayList<>();
        this.receivedData = new ArrayList<>();
    }

    public TcpState getState() {
        return state;
    }

    public List<Packet> getReceiveBuffer() {
        return receiveBuffer;
    }

    public List<Packet> getReceivedData() {
        return receivedData;
    }

    public TransferSummary getLastTransferSummary() {
        return lastTransferSummary;
    }

    // Mâ”œÂ®thode pour â”œÂ®tablir une connexion avec le serveur en suivant le processus de handshake TCP
    public void connect(TcpServer server) {
        System.out.println("[CLIENT]: Dâ”œÂ®but de la demande de connexion.");

        Packet synPacket = new Packet(
                PacketType.SYN,
                sequenceNumber,
                0,
                null
        );

        state = TcpState.SYN_SENT;
        System.out.println("[CLIENT]: Envoi de SYN");

        Packet serverResponse = server.receivePacket(synPacket);

        //si le client reâ”œÂºoit un SYN-ACK du serveur, il râ”œÂ®pond avec un ACK et passe â”œÃ¡ l'â”œÂ®tat ESTABLISHED, la connexion est â”œÂ®tablie
        if (serverResponse != null && serverResponse.getType() == PacketType.SYN_ACK) {
            System.out.println("[CLIENT]: Râ”œÂ®ception de SYN_ACK");

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

            System.out.println("[CLIENT]: Connexion â”œÂ®tablie.");
        // sinon, si le client ne reâ”œÂºoit pas de SYN-ACK ou reâ”œÂºoit un paquet inattendu, la connexion â”œÂ®choue et le client reste en â”œÂ®tat CLOSED
        } else {
            System.out.println("[CLIENT]: â”œÃ«chec de l'ouverture de connexion.");
        }
    }

    // Mâ”œÂ®thode pour demander des donnâ”œÂ®es au serveur en fonction d'une demande de transfert
    public TransferSummary requestAllData(TcpServer server, int totalPacketsRequested, int receiveWindow) {
        // Vâ”œÂ®rification que la connexion est â”œÂ®tablie avant de demander des donnâ”œÂ®es
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[CLIENT]: Impossible de demander des donnâ”œÂ®es : connexion non â”œÂ®tablie.");
            lastTransferSummary = new TransferSummary(totalPacketsRequested, 0, 0, 0, 0, receiveWindow, false);
            return lastTransferSummary;
        }

        // Validation des paramâ”œÂ¿tres de la demande de transfert
        if (totalPacketsRequested <= 0 || receiveWindow <= 0) {
            System.out.println("[CLIENT]: Paramâ”œÂ¿tres invalides pour le transfert.");
            lastTransferSummary = new TransferSummary(totalPacketsRequested, 0, 0, 0, 0, receiveWindow, false);
            return lastTransferSummary;
        }

        receiveBuffer.clear();
        receivedData.clear();
        server.resetTransferCursor();
        transferCorruptedPacketsDetected = 0;
        transferRetransmissionsPerformed = 0;

        int remainingToRequest = totalPacketsRequested;
        int cycle = 1;

        // Boucle de demande de donnâ”œÂ®es tant qu'il reste des paquets â”œÃ¡ demander
        while (!canceled && remainingToRequest > 0) {
            System.out.println();
            System.out.println("========== Cycle de transfert " + cycle + " ==========");

            receiveBuffer.clear();

            TransferRequest request = new TransferRequest(remainingToRequest, receiveWindow);
            System.out.println("[CLIENT]: Envoi d'une requâ”œÂ¬te de transfert : " + request);

            TransferResult result = server.sendData(request);
            List<Packet> receivedPackets = result.getSentPackets();

            if (receivedPackets.isEmpty()) {
                System.out.println("[CLIENT]: Aucun paquet reâ”œÂºu. Arrâ”œÂ¬t du transfert.");
                break;
            }

            for (Packet packet : receivedPackets) {
                receiveBuffer.add(packet);
            }

            System.out.println("[CLIENT]: Paquets reâ”œÂºus dans le buffer de râ”œÂ®ception :");
            for (Packet packet : receiveBuffer) {
                System.out.println("    " + packet);
            }

            processReceivedPackets(server);

            if (transferListener != null) {
                transferListener.onProgress(receivedData.size(), totalPacketsRequested);
            }

            remainingToRequest = result.getRemainingPackets();
            System.out.println("[CLIENT]: Nombre de paquets restant â”œÃ¡ demander : " + remainingToRequest);

            cycle++;
        }

        System.out.println();
        System.out.println("[CLIENT]: Transfert terminâ”œÂ®.");
        System.out.println("[CLIENT]: Donnâ”œÂ®es totales reâ”œÂºues :");
        for (Packet packet : receivedData) {
            System.out.println("    " + packet);
        }

        lastTransferSummary = new TransferSummary(
                totalPacketsRequested,
                receivedData.size(),
            transferRetransmissionsPerformed,
            transferCorruptedPacketsDetected,
                cycle - 1,
                receiveWindow,
                remainingToRequest == 0
        );

        System.out.println();
        System.out.println("[CLIENT]: Râ”œÂ®sumâ”œÂ® du transfert :");
        System.out.println("    " + lastTransferSummary);

        return lastTransferSummary;
    }

    public void cancelTransfer() {
        this.canceled = true;
    }

    public void resetCancel() {
        this.canceled = false;
    }

    public void setTransferListener(TransferListener listener) {
        this.transferListener = listener;
    }

    // Mâ”œÂ®thode pour analyser les paquets reâ”œÂºus, envoyer des ACK pour les paquets corrects et des NACK pour les paquets corrompus, et demander des retransmissions si nâ”œÂ®cessaire
    private void processReceivedPackets(TcpServer server) {
        if (receiveBuffer.isEmpty()) {
            System.out.println("[CLIENT]: Aucun paquet reâ”œÂºu, aucun ACK/NACK envoyâ”œÂ®.");
            return;
        }

        System.out.println("[CLIENT]: Analyse des paquets reâ”œÂºus :");

        // Parcours des paquets reâ”œÂºus pour dâ”œÂ®terminer les ACK/NACK â”œÃ¡ envoyer
        for (Packet packet : receiveBuffer) {
            if (!packet.isCorrupted()) {
                System.out.println("ACK pour le paquet de sâ”œÂ®quence " + packet.getSequenceNumber());
                receivedData.add(packet);
            } else {
                System.out.println("NACK pour le paquet de sâ”œÂ®quence " + packet.getSequenceNumber());
                transferCorruptedPacketsDetected++;

                Packet retransmittedPacket = server.retransmitPacket(packet.getSequenceNumber());

                if (retransmittedPacket != null && !retransmittedPacket.isCorrupted()) {
                    System.out.println("ACK aprâ”œÂ¿s retransmission pour le paquet de sâ”œÂ®quence "
                            + retransmittedPacket.getSequenceNumber());
                    transferRetransmissionsPerformed++;
                    receivedData.add(retransmittedPacket);
                } else {
                    System.out.println("â”œÃ«chec de retransmission pour le paquet de sâ”œÂ®quence "
                            + packet.getSequenceNumber());
                }
            }
        }
    }

    // Mâ”œÂ®thode pour fermer la connexion avec le serveur en suivant le processus de fermeture TCP
    public void closeConnection(TcpServer server) {
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[CLIENT]: Impossible de fermer : connexion non â”œÂ®tablie.");
            return;
        }

        System.out.println();
        System.out.println("[CLIENT]: Dâ”œÂ®but de la fermeture de connexion.");

        Packet finPacket = new Packet(
                PacketType.FIN,
                sequenceNumber + 999,
                0,
                null
        );

        state = TcpState.FIN_WAIT;
        System.out.println("[CLIENT]: Envoi de FIN");

        Packet serverResponse = server.receivePacket(finPacket);

        //si le client reâ”œÂºoit un FIN-ACK du serveur, il râ”œÂ®pond avec un ACK final, passe â”œÃ¡ l'â”œÂ®tat TIME_WAIT, temporise pour permettre au serveur de recevoir le ACK final, puis passe â”œÃ¡ l'â”œÂ®tat CLOSED, la connexion est fermâ”œÂ®e
        if (serverResponse != null && serverResponse.getType() == PacketType.FIN_ACK) {
            System.out.println("[CLIENT]: Râ”œÂ®ception de FIN_ACK");

            Packet finalAck = new Packet(
                    PacketType.ACK,
                    finPacket.getSequenceNumber() + 1,
                    serverResponse.getSequenceNumber() + 1,
                    null
            );

            System.out.println("[CLIENT]: Envoi du ACK final");
            server.receivePacket(finalAck);

            state = TcpState.TIME_WAIT;
            System.out.println("[CLIENT]: Passage â”œÃ¡ TIME_WAIT");

            System.out.println("[CLIENT]: Temporisation de fin de connexion...");
            state = TcpState.CLOSED;
            System.out.println("[CLIENT]: Connexion fermâ”œÂ®e.");
        } else {
            System.out.println("[CLIENT]: â”œÃ«chec de la fermeture de connexion.");
        }
    }
}
