package fr.uvsq.tcpsim.client;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.server.TcpServer;

// Classe représentant un client TCP qui peut se connecter à un serveur, demander des données et gérer les paquets reçus
public class TcpClient {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    // Buffer temporaire de réception
    private List<Packet> receiveBuffer;

    // Mémoire finale des données reçues
    private List<Packet> receivedData;

    // Constructeur initialisant l'état du client et les buffers
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

    // Méthode pour établir une connexion avec le serveur en utilisant le processus de handshake TCP
    public void connect(TcpServer server) {
        System.out.println("[CLIENT] Début de la demande de connexion.");

        Packet synPacket = new Packet(
                PacketType.SYN,
                sequenceNumber,
                0,
                null
        );

        state = TcpState.SYN_SENT;
        System.out.println("[CLIENT] Envoi de SYN");

        Packet serverResponse = server.receivePacket(synPacket);

        // Si le serveur répond avec un SYN_ACK, le client envoie un ACK pour compléter le handshake
        if (serverResponse != null && serverResponse.getType() == PacketType.SYN_ACK) {
            System.out.println("[CLIENT] Réception de SYN_ACK");

            acknowledgementNumber = serverResponse.getSequenceNumber() + 1;

            Packet ackPacket = new Packet(
                    PacketType.ACK,
                    sequenceNumber + 1,
                    acknowledgementNumber,
                    null
            );

            state = TcpState.ESTABLISHED;
            System.out.println("[CLIENT] Envoi de ACK");

            server.receivePacket(ackPacket);

            System.out.println("[CLIENT] Connexion établie.");
        // Si la réponse du serveur n'est pas conforme au handshake, la connexion échoue
        } else {
            System.out.println("[CLIENT] Échec de l'ouverture de connexion.");
        }
    }

    // Méthode pour demander des données au serveur en respectant les contraintes de la fenêtre de réception
    public void requestAllData(TcpServer server, int totalPacketsRequested, int receiveWindow) {
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[CLIENT] Impossible de demander des données : connexion non établie.");
            return;
        }

        // Validation des paramètres de la demande de transfert
        if (totalPacketsRequested <= 0 || receiveWindow <= 0) {
            System.out.println("[CLIENT] Paramètres invalides pour le transfert.");
            return;
        }

        receiveBuffer.clear();
        receivedData.clear();
        server.resetTransferCursor();

        int remainingToRequest = totalPacketsRequested;
        int cycle = 1;

        // Boucle de transfert qui continue tant qu'il reste des paquets à demander
        while (remainingToRequest > 0) {
            System.out.println();
            System.out.println("========== Cycle de transfert " + cycle + " ==========");

            receiveBuffer.clear();

            TransferRequest request = new TransferRequest(remainingToRequest, receiveWindow);
            System.out.println("[CLIENT] Envoi d'une requête de transfert : " + request);

            TransferResult result = server.sendData(request);
            List<Packet> receivedPackets = result.getSentPackets();

            // Si aucun paquet n'est reçu, le transfert est considéré comme terminé
            if (receivedPackets.isEmpty()) {
                System.out.println("[CLIENT] Aucun paquet reçu. Arrêt du transfert.");
                break;
            }

            // Ajout des paquets reçus au buffer de réception et à la mémoire finale
            for (Packet packet : receivedPackets) {
                receiveBuffer.add(packet);
                receivedData.add(packet);
            }

            // Affichage des paquets reçus dans le buffer de réception
            System.out.println("[CLIENT] Paquets reçus dans le buffer de réception :");
            for (Packet packet : receiveBuffer) {
                System.out.println("    " + packet);
            }

            sendAcknowledgements();

            remainingToRequest = result.getRemainingPackets();
            System.out.println("[CLIENT] Nombre de paquets restant à demander : " + remainingToRequest);

            cycle++;
        }

        System.out.println();
        System.out.println("[CLIENT] Transfert terminé.");
        System.out.println("[CLIENT] Données totales reçues :");
        for (Packet packet : receivedData) {
            System.out.println("    " + packet);
        }
    }

    // Méthode pour envoyer des ACK pour les paquets reçus dans le buffer de réception
    private void sendAcknowledgements() {
        if (receiveBuffer.isEmpty()) {
            System.out.println("[CLIENT] Aucun paquet reçu, aucun ACK envoyé.");
            return;
        }

        System.out.println("[CLIENT] Envoi des ACK pour les paquets reçus.");
        for (Packet packet : receiveBuffer) {
            System.out.println("    ACK pour le paquet de séquence " + packet.getSequenceNumber());
        }
    }
}