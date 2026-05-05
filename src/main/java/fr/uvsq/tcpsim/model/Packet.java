package fr.uvsq.tcpsim.model;

/* Classe représentant un paquet dans le protocole TCP */
public class Packet {
    private PacketType type;
    private int sequenceNumber;
    private int acknowledgementNumber;
    private String payload;
    private boolean corrupted;

    /* Constructeur */
    public Packet(PacketType type, int sequenceNumber, int acknowledgementNumber, String payload) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.payload = payload;
        this.corrupted = false;
    }

    /* Getters et setters */
    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    /* Le numéro de séquence est utilisé pour ordonner les paquets et détecter les pertes */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /* Le numéro d'acquittement est utilisé pour confirmer la réception des paquets */
    public int getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(int acknowledgementNumber) {
        this.acknowledgementNumber = acknowledgementNumber;
    }

    /* Le payload contient les données transportées par le paquet */
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    /* Indique si le paquet est corrompu */
    public boolean isCorrupted() {
        return corrupted;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", sequenceNumber=" + sequenceNumber +
                ", acknowledgementNumber=" + acknowledgementNumber +
                ", payload='" + payload + '\'' +
                ", corrupted=" + corrupted +
                '}';
    }
}