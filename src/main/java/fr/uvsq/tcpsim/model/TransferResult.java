package fr.uvsq.tcpsim.model;

import java.util.List;

// Classe représentant le résultat d'un transfert de données, contenant les paquets envoyés et le nombre de paquets restants à envoyer
public class TransferResult {
    private List<Packet> sentPackets;
    private int remainingPackets;

    // Constructeur pour initialiser les paquets envoyés et le nombre de paquets restants
    public TransferResult(List<Packet> sentPackets, int remainingPackets) {
        this.sentPackets = sentPackets;
        this.remainingPackets = remainingPackets;
    }

    // Getters et setters pour les paquets envoyés
    public List<Packet> getSentPackets() {
        return sentPackets;
    }

    public void setSentPackets(List<Packet> sentPackets) {
        this.sentPackets = sentPackets;
    } 

    // Getters et setters pour le nombre de paquets restants
    public int getRemainingPackets() {
        return remainingPackets;
    }

    public void setRemainingPackets(int remainingPackets) {
        this.remainingPackets = remainingPackets;
    }

    @Override
    public String toString() {
        return "TransferResult{" +
                "sentPackets=" + sentPackets +
                ", remainingPackets=" + remainingPackets +
                '}';
    }
}