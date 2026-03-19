package fr.uvsq.tcpsim.model;


public class Packet {
    private PacketType type;
    private int sequenceNumber;
    private int acknowledgementNumber;
    private String payload;
    private boolean corrupted;

    public Packet(PacketType type, int sequenceNumber, int acknowledgementNumber, String payload) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.payload = payload;
        this.corrupted = false;
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(int acknowledgementNumber) {
        this.acknowledgementNumber = acknowledgementNumber;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

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