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

// Classe représentant le client TCP dans la simulation
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

	// Méthode pour établir une connexion avec le serveur en suivant le processus de
	// handshake TCP
	public void connect(TcpServer server) {
		System.out.println("[CLIENT]: Début de la demande de connexion.");

		Packet synPacket = new Packet(PacketType.SYN, sequenceNumber, 0, null);

		state = TcpState.SYN_SENT;
		System.out.println("[CLIENT]: Envoi de SYN");

		Packet serverResponse = server.receivePacket(synPacket);

		// si le client reçoit un SYN-ACK du serveur, il répond avec un ACK et passe à
		// l'état ESTABLISHED, la connexion est établie
		if (serverResponse != null && serverResponse.getType() == PacketType.SYN_ACK) {
			System.out.println("[CLIENT]: Réception de SYN_ACK");

			acknowledgementNumber = serverResponse.getSequenceNumber() + 1;

			Packet ackPacket = new Packet(PacketType.ACK, sequenceNumber + 1, acknowledgementNumber, null);

			state = TcpState.ESTABLISHED;
			System.out.println("[CLIENT]: Envoi de ACK");

			server.receivePacket(ackPacket);

			System.out.println("[CLIENT]: Connexion établie.");
			// sinon, si le client ne reçoit pas de SYN-ACK ou reçoit un paquet inattendu,
			// la connexion échoue et le client reste en état CLOSED
		} else {
			System.out.println("[CLIENT]: Échec de l'ouverture de connexion.");
		}
	}

	// Méthode pour demander des données au serveur en fonction d'une demande de
	// transfert
	public TransferSummary requestAllData(TcpServer server, int totalPacketsRequested, int receiveWindow) {
		// Vérification que la connexion est établie avant de demander des données
		if (state != TcpState.ESTABLISHED) {
			System.out.println("[CLIENT]: Impossible de demander des données : connexion non établie.");
			lastTransferSummary = new TransferSummary(totalPacketsRequested, 0, 0, 0, 0, receiveWindow, false);
			return lastTransferSummary;
		}

		// Validation des paramètres de la demande de transfert
		if (totalPacketsRequested <= 0 || receiveWindow <= 0) {
			System.out.println("[CLIENT]: Paramètres invalides pour le transfert.");
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

		// Boucle de demande de données tant qu'il reste des paquets à demander
		while (!canceled && remainingToRequest > 0) {
			System.out.println();
			System.out.println("========== Cycle de transfert " + cycle + " ==========");

			receiveBuffer.clear();

			TransferRequest request = new TransferRequest(remainingToRequest, receiveWindow);
			System.out.println("[CLIENT]: Envoi d'une requête de transfert : " + request);

			TransferResult result = server.sendData(request);
			List<Packet> receivedPackets = result.getSentPackets();

			if (receivedPackets.isEmpty()) {
				System.out.println("[CLIENT]: Aucun paquet reçu. Arrêt du transfert.");
				break;
			}

			for (Packet packet : receivedPackets) {
				receiveBuffer.add(packet);
			}

			System.out.println("[CLIENT]: Paquets reçus dans le buffer de réception :");
			for (Packet packet : receiveBuffer) {
				System.out.println("    " + packet);
			}

			processReceivedPackets(server);

			if (transferListener != null) {
				transferListener.onProgress(receivedData.size(), totalPacketsRequested);
			}

			remainingToRequest = result.getRemainingPackets();
			System.out.println("[CLIENT]: Nombre de paquets restant à demander : " + remainingToRequest);

			cycle++;
		}

		System.out.println();
		System.out.println("[CLIENT]: Transfert terminé.");
		System.out.println("[CLIENT]: Données totales reçues :");
		for (Packet packet : receivedData) {
			System.out.println("    " + packet);
		}

		lastTransferSummary = new TransferSummary(totalPacketsRequested, receivedData.size(),
				transferRetransmissionsPerformed, transferCorruptedPacketsDetected, cycle - 1, receiveWindow,
				remainingToRequest == 0);

		System.out.println();
		System.out.println("[CLIENT]: Résumé du transfert :");
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

	// Méthode pour analyser les paquets reçus, envoyer des ACK pour les paquets
	// corrects et des NACK pour les paquets corrompus, et demander des
	// retransmissions si nécessaire
	private void processReceivedPackets(TcpServer server) {
		if (receiveBuffer.isEmpty()) {
			System.out.println("[CLIENT]: Aucun paquet reçu, aucun ACK/NACK envoyé.");
			return;
		}

		System.out.println("[CLIENT]: Analyse des paquets reçus :");

		// Parcours des paquets reçus pour déterminer les ACK/NACK à envoyer
		for (Packet packet : receiveBuffer) {
			if (!packet.isCorrupted()) {
				System.out.println("ACK pour le paquet de séquence " + packet.getSequenceNumber());
				receivedData.add(packet);
			} else {
				System.out.println("NACK pour le paquet de séquence " + packet.getSequenceNumber());
				transferCorruptedPacketsDetected++;

				Packet retransmittedPacket = server.retransmitPacket(packet.getSequenceNumber());

				if (retransmittedPacket != null && !retransmittedPacket.isCorrupted()) {
					System.out.println("ACK après retransmission pour le paquet de séquence "
							+ retransmittedPacket.getSequenceNumber());
					transferRetransmissionsPerformed++;
					receivedData.add(retransmittedPacket);
				} else {
					System.out.println(
							"Échec de retransmission pour le paquet de séquence " + packet.getSequenceNumber());
				}
			}
		}
	}

	// Méthode pour fermer la connexion avec le serveur en suivant le processus de
	// fermeture TCP
	public void closeConnection(TcpServer server) {
		if (state != TcpState.ESTABLISHED) {
			System.out.println("[CLIENT]: Impossible de fermer : connexion non établie.");
			return;
		}

		System.out.println();
		System.out.println("[CLIENT]: Début de la fermeture de connexion.");

		Packet finPacket = new Packet(PacketType.FIN, sequenceNumber + 999, 0, null);

		state = TcpState.FIN_WAIT;
		System.out.println("[CLIENT]: Envoi de FIN");

		Packet serverResponse = server.receivePacket(finPacket);

		// si le client reçoit un FIN-ACK du serveur, il répond avec un ACK final, passe
		// à l'état TIME_WAIT, temporise pour permettre au serveur de recevoir le ACK
		// final, puis passe à l'état CLOSED, la connexion est fermée
		if (serverResponse != null && serverResponse.getType() == PacketType.FIN_ACK) {
			System.out.println("[CLIENT]: Réception de FIN_ACK");

			Packet finalAck = new Packet(PacketType.ACK, finPacket.getSequenceNumber() + 1,
					serverResponse.getSequenceNumber() + 1, null);

			System.out.println("[CLIENT]: Envoi du ACK final");
			server.receivePacket(finalAck);

			state = TcpState.TIME_WAIT;
			System.out.println("[CLIENT]: Passage à TIME_WAIT");

			System.out.println("[CLIENT]: Temporisation de fin de connexion...");
			state = TcpState.CLOSED;
			System.out.println("[CLIENT]: Connexion fermée.");
		} else {
			System.out.println("[CLIENT]: Échec de la fermeture de connexion.");
		}
	}
}