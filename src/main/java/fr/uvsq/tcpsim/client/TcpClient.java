package fr.uvsq.tcpsim.client;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.server.TcpServer;

// Classe représentant un client TCP qui peut initier des connexions, recevoir des données et envoyer des ACK/NACK en fonction de la réception des paquets
public class TcpClient {
    private TcpState state;
    private int sequenceNumber;
    private int acknowledgementNumber;

    private List<Packet> receiveBuffer;
    private List<Packet> receivedData;

    // Constructeur initialisant l'état du client, les buffers et les numéros de séquence
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

    // Méthode pour initier une connexion avec un serveur TCP en suivant le processus de handshake TCP (SYN, SYN-ACK, ACK)
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

        // Si le client reçoit un SYN_ACK du serveur, il répond avec un ACK pour établir la connexion
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
        // Si le client ne reçoit pas de SYN_ACK ou reçoit une réponse inattendue, la connexion échoue
        } else {
            System.out.println("[CLIENT] Échec de l'ouverture de connexion.");
        }
    }

    // Méthode pour demander des données au serveur en fonction du nombre de paquets souhaités et de la fenêtre de réception, en traitant les paquets reçus et en envoyant des ACK/NACK en conséquence
    public void requestAllData(TcpServer server, int totalPacketsRequested, int receiveWindow) {
        // Si la connexion n'est pas établie, le client ne peut pas demander de données
        if (state != TcpState.ESTABLISHED) {
            System.out.println("[CLIENT] Impossible de demander des données : connexion non établie.");
            return;
        }

        // Validation des paramètres de la demande de transfert avant de commencer le processus de demande de données au serveur
        if (totalPacketsRequested <= 0 || receiveWindow <= 0) {
            System.out.println("[CLIENT] Paramètres invalides pour le transfert.");
            return;
        }

        receiveBuffer.clear();
        receivedData.clear();
        server.resetTransferCursor();

        int remainingToRequest = totalPacketsRequested;
        int cycle = 1;

        // Boucle pour demander des données au serveur tant qu'il reste des paquets à demander, en respectant la fenêtre de réception et en traitant les paquets reçus à chaque cycle
        while (remainingToRequest > 0) {
            System.out.println();
            System.out.println("========== Cycle de transfert " + cycle + " ==========");

            receiveBuffer.clear();

            TransferRequest request = new TransferRequest(remainingToRequest, receiveWindow);
            System.out.println("[CLIENT] Envoi d'une requête de transfert : " + request);

            TransferResult result = server.sendData(request);
            List<Packet> receivedPackets = result.getSentPackets();

            // Si aucun paquet n'est reçu du serveur, le client arrête le processus de transfert et affiche un message d'erreur, sinon il ajoute les paquets reçus au buffer de réception et les affiche avant de les traiter
            if (receivedPackets.isEmpty()) {
                System.out.println("[CLIENT] Aucun paquet reçu. Arrêt du transfert.");
                break;
            }

            for (Packet packet : receivedPackets) {
                receiveBuffer.add(packet);
            }

            System.out.println("[CLIENT] Paquets reçus dans le buffer de réception :");
            for (Packet packet : receiveBuffer) {
                System.out.println("    " + packet);
            }

            processReceivedPackets(server);

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

    // Méthode pour analyser les paquets reçus dans le buffer de réception, envoyer des ACK pour les paquets non corrompus et des NACK pour les paquets corrompus, et gérer les retransmissions en fonction des réponses du serveur
    private void processReceivedPackets(TcpServer server) {
        if (receiveBuffer.isEmpty()) {
            System.out.println("[CLIENT] Aucun paquet reçu, aucun ACK/NACK envoyé.");
            return;
        }

        System.out.println("[CLIENT] Analyse des paquets reçus :");

        // Pour chaque paquet reçu, le client vérifie s'il est corrompu ou non, envoie un ACK pour les paquets non corrompus et un NACK pour les paquets corrompus, et gère les retransmissions en fonction des réponses du serveur
        for (Packet packet : receiveBuffer) {
            // Si le paquet n'est pas corrompu, le client envoie un ACK pour le paquet et l'ajoute aux données reçues, sinon il envoie un NACK et demande une retransmission du paquet spécifique au serveur, en vérifiant que la retransmission réussit et que le paquet retransmis n'est plus corrompu avant de l'ajouter aux données reçues
            if (!packet.isCorrupted()) {
                System.out.println("    ACK pour le paquet de séquence " + packet.getSequenceNumber());
                receivedData.add(packet);
            // Si le paquet est corrompu, le client envoie un NACK et demande une retransmission du paquet spécifique au serveur, en vérifiant que la retransmission réussit et que le paquet retransmis n'est plus corrompu avant de l'ajouter aux données reçues
            } else {
                System.out.println("    NACK pour le paquet de séquence " + packet.getSequenceNumber());

                Packet retransmittedPacket = server.retransmitPacket(packet.getSequenceNumber());

                // Si la retransmission réussit et que le paquet retransmis n'est plus corrompu, le client envoie un ACK pour le paquet retransmis et l'ajoute aux données reçues, sinon il affiche un message d'erreur pour indiquer l'échec de la retransmission
                if (retransmittedPacket != null && !retransmittedPacket.isCorrupted()) {
                    System.out.println("    ACK après retransmission pour le paquet de séquence "
                            + retransmittedPacket.getSequenceNumber());
                    receivedData.add(retransmittedPacket);
                // Si la retransmission échoue ou que le paquet retransmis est toujours corrompu, le client affiche un message d'erreur pour indiquer l'échec de la retransmission
                } else {
                    System.out.println("    Échec de retransmission pour le paquet de séquence "
                            + packet.getSequenceNumber());
                }
            }
        }
    }
}