package fr.uvsq.tcpsim.model;

// Résumé global d'un transfert de données dans la simulation TCP.
public class TransferSummary {
    private final int requestedPackets;
    private final int receivedPackets;
    private final int retransmissions;
    private final int corruptedPacketsDetected;
    private final int cycles;
    private final int receiveWindow;
    private final boolean completed;

    public TransferSummary(int requestedPackets,
                           int receivedPackets,
                           int retransmissions,
                           int corruptedPacketsDetected,
                           int cycles,
                           int receiveWindow,
                           boolean completed) {
        this.requestedPackets = requestedPackets;
        this.receivedPackets = receivedPackets;
        this.retransmissions = retransmissions;
        this.corruptedPacketsDetected = corruptedPacketsDetected;
        this.cycles = cycles;
        this.receiveWindow = receiveWindow;
        this.completed = completed;
    }

    public int getRequestedPackets() {
        return requestedPackets;
    }

    public int getReceivedPackets() {
        return receivedPackets;
    }

    public int getRetransmissions() {
        return retransmissions;
    }

    public int getCorruptedPacketsDetected() {
        return corruptedPacketsDetected;
    }

    public int getCycles() {
        return cycles;
    }

    public int getReceiveWindow() {
        return receiveWindow;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return "TransferSummary{" +
                "requestedPackets=" + requestedPackets +
                ", receivedPackets=" + receivedPackets +
                ", retransmissions=" + retransmissions +
                ", corruptedPacketsDetected=" + corruptedPacketsDetected +
                ", cycles=" + cycles +
                ", receiveWindow=" + receiveWindow +
                ", completed=" + completed +
                '}';
    }

    public static String csvHeader() {
        return "requestedPackets,receivedPackets,retransmissions,corruptedPacketsDetected,cycles,receiveWindow,completed";
    }

    public String toCsvRow() {
        return requestedPackets + "," + receivedPackets + "," + retransmissions + "," + corruptedPacketsDetected + "," + cycles + "," + receiveWindow + "," + completed;
    }
}