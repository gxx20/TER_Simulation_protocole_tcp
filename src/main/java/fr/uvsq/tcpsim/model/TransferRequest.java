package fr.uvsq.tcpsim.model;

/* Classe représentant une demande de transfert de données */
public class TransferRequest {
    private int numberOfPacketsRequested;
    private int receiveWindow;

    /* Constructeur initialisant une demande de transfert */
    public TransferRequest(int numberOfPacketsRequested, int receiveWindow) {
        this.numberOfPacketsRequested = numberOfPacketsRequested;
        this.receiveWindow = receiveWindow;
    }

    /* Getter pour le nombre de paquets demandés */
    public int getNumberOfPacketsRequested() {
        return numberOfPacketsRequested;
    }

    public void setNumberOfPacketsRequested(int numberOfPacketsRequested) {
        this.numberOfPacketsRequested = numberOfPacketsRequested;
    }

    /* Getter pour la fenêtre de réception */
    public int getReceiveWindow() {
        return receiveWindow;
    }

    public void setReceiveWindow(int receiveWindow) {
        this.receiveWindow = receiveWindow;
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "numberOfPacketsRequested=" + numberOfPacketsRequested +
                ", receiveWindow=" + receiveWindow +
                '}';
    }
}