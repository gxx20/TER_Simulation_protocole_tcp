package fr.uvsq.tcpsim.client;

/**
 * Listener interface for transfer progress updates.
 */
public interface TransferListener {
    void onProgress(int receivedPackets, int requestedPackets);
}
