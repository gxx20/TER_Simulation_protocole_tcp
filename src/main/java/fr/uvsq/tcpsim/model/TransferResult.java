package fr.uvsq.tcpsim.model;

import java.util.List;

// Classe reprâ”œÂ®sentant le râ”œÂ®sultat d'un transfert de donnâ”œÂ®es, contenant les paquets envoyâ”œÂ®s et le nombre de paquets restants â”œÃ¡ envoyer
public class TransferResult {
    private List<Packet> sentPackets;
    private int remainingPackets;

    // Constructeur pour initialiser les paquets envoyâ”œÂ®s et le nombre de paquets restants
    public TransferResult(List<Packet> sentPackets, int remainingPackets) {
        this.sentPackets = sentPackets;
        this.remainingPackets = remainingPackets;
    }

    // Getters et setters pour les paquets envoyâ”œÂ®s
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
