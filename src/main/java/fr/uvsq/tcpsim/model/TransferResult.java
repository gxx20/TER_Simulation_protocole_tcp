package fr.uvsq.tcpsim.model;

import java.util.List;

/* Classe représentant le résultat d'un transfert de données */
public class TransferResult {
    private List<Packet> sentPackets;
    private int remainingPackets;

    /* Constructeur initialisant le résultat d'un transfert */
    public TransferResult(List<Packet> sentPackets, int remainingPackets) {
        this.sentPackets = sentPackets;
        this.remainingPackets = remainingPackets;
    }

    /* Getter pour les paquets envoyés */
    public List<Packet> getSentPackets() {
        return sentPackets;
    }

    public void setSentPackets(List<Packet> sentPackets) {
        this.sentPackets = sentPackets;
    }

    /* Getter pour les paquets restants */
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