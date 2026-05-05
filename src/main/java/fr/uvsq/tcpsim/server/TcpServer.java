package fr.uvsq.tcpsim.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;

/// Classe représentant le serveur TCP dans la simulation
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

	// Initialisation de la mémoire source avec des données fictives
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

	// Méthode pour recevoir un paquet du client et répondre en fonction de l'état
	// actuel du serveur
	public Packet receivePacket(Packet packet) {
		System.out.println("[SERVEUR] Paquet reçu : " + packet);

		// si le serveur est en écoute et reçoit un SYN, il répond avec un SYN-ACK et
		// passe à l'état SYN_RECEIVED
		if (state == TcpState.LISTEN && packet.getType() == PacketType.SYN) {
			acknowledgementNumber = packet.getSequenceNumber() + 1;
			state = TcpState.SYN_RECEIVED;

			Packet response = new Packet(PacketType.SYN_ACK, sequenceNumber, acknowledgementNumber, null);

			System.out.println("[SERVEUR] Envoi de SYN_ACK");
			return response;
		}

		// si le serveur est en SYN_RECEIVED et reçoit un ACK, il passe à l'état
		// ESTABLISHED et la connexion est établie
		if (state == TcpState.SYN_RECEIVED && packet.getType() == PacketType.ACK) {
			state = TcpState.ESTABLISHED;
			System.out.println("[SERVEUR] Connexion établie.");
			return null;
		}

		// si le serveur est en ESTABLISHED et reçoit un FIN, il répond avec un FIN-ACK
		// et passe à l'état CLOSE_WAIT
		if (state == TcpState.ESTABLISHED && packet.getType() == PacketType.FIN) {
			acknowledgementNumber = packet.getSequenceNumber() + 1;
			state = TcpState.CLOSE_WAIT;

			Packet response = new Packet(PacketType.FIN_ACK, sequenceNumber + 100, acknowledgementNumber, null);

			System.out.println("[SERVEUR] Réception de FIN, passage à CLOSE_WAIT");
			System.out.println("[SERVEUR] Envoi de FIN_ACK");
			state = TcpState.LAST_ACK;
			return response;
		}

		// si le serveur est en LAST_ACK et reçoit un ACK, il passe à l'état CLOSED et
		// la connexion est fermée
		if (state == TcpState.LAST_ACK && packet.getType() == PacketType.ACK) {
			state = TcpState.CLOSED;
			System.out.println("[SERVEUR] ACK final reçu. Connexion fermée.");
			return null;
		}

		System.out.println("[SERVEUR] Paquet inattendu dans l'état " + state);
		return null;
	}

	public void resetTransferCursor() {
		this.nextDataIndex = 0;
	}

	// Méthode pour envoyer des données au client en fonction de la demande de
	// transfert
	public TransferResult sendData(TransferRequest request) {
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

		// Génération des paquets de données à envoyer
		for (int i = 0; i < packetsToSend; i++) {
			String data = sourceData.get(nextDataIndex);

			Packet dataPacket = new Packet(PacketType.DATA, sequenceNumber + nextDataIndex + 1, 0, data);

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

		System.out.println("[SERVEUR] Paquets placés dans le buffer d'envoi :");
		for (Packet packet : sendBuffer) {
			System.out.println("    " + packet);
		}

		return new TransferResult(new ArrayList<>(sendBuffer), remainingPackets);
	}

	// Méthode pour retransmettre un paquet spécifique en cas de perte ou de
	// corruption
	public Packet retransmitPacket(int sequenceNumberToRetransmit) {
		int sourceIndex = sequenceNumberToRetransmit - sequenceNumber - 1;

		// Vérification que le numéro de séquence à retransmettre correspond à un paquet
		// valide dans la mémoire source
		if (sourceIndex < 0 || sourceIndex >= sourceData.size()) {
			System.out.println("[SERVEUR] Impossible de retransmettre : numéro de séquence invalide.");
			return null;
		}

		String data = sourceData.get(sourceIndex);

		Packet retransmittedPacket = new Packet(PacketType.DATA, sequenceNumberToRetransmit, 0, data);

		retransmittedPacket.setCorrupted(false);

		System.out.println("[SERVEUR] Retransmission du paquet : " + retransmittedPacket);
		return retransmittedPacket;
	}
}