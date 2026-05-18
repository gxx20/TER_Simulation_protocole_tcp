package fr.uvsq.tcpsim;

import java.util.concurrent.atomic.AtomicInteger;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;
import junit.framework.TestCase;

public class TcpClientTest extends TestCase {

    public void testInitialState() {
        TcpClient client = new TcpClient();

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(0, client.getReceiveBuffer().size());
        assertEquals(0, client.getReceivedData().size());
        assertNull(client.getLastTransferSummary());
    }

    public void testConnectEstablishesConnection() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        assertEquals(TcpState.ESTABLISHED, client.getState());
        assertEquals(TcpState.ESTABLISHED, server.getState());
    }

    public void testRequestDataWithoutConnectionFails() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
        assertEquals(TcpState.CLOSED, client.getState());
    }

    public void testInvalidTotalPacketsFails() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 0, 2);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
    }

    public void testInvalidReceiveWindowFails() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 0);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
    }

    public void testCompleteTransferWithoutError() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 4, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(4, summary.getRequestedPackets());
        assertEquals(4, summary.getReceivedPackets());
        assertEquals(0, summary.getCorruptedPacketsDetected());
        assertEquals(2, summary.getCycles());
    }

    public void testTransferWithCorruptionStillCompletesByRetransmission() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(1.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(3, summary.getReceivedPackets());
        assertTrue(summary.getCorruptedPacketsDetected() > 0);
    }

    public void testReceiveBufferAndReceivedDataAfterTransfer() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);
        client.requestAllData(server, 3, 2);

        assertFalse(client.getReceiveBuffer().isEmpty());
        assertEquals(3, client.getReceivedData().size());
    }

    public void testLastTransferSummaryIsSaved() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(client.getLastTransferSummary());
        assertEquals(summary, client.getLastTransferSummary());
    }

    public void testListenerIsCalledDuringTransfer() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        AtomicInteger progressCalls = new AtomicInteger(0);

        client.setTransferListener((received, requested) -> progressCalls.incrementAndGet());

        client.connect(server);
        client.requestAllData(server, 4, 2);

        assertTrue(progressCalls.get() > 0);
    }

    public void testCancelTransferStopsImmediately() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);
        client.cancelTransfer();

        TransferSummary summary = client.requestAllData(server, 5, 2);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
    }

    public void testResetCancelAllowsTransfer() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);
        client.cancelTransfer();
        client.resetCancel();

        TransferSummary summary = client.requestAllData(server, 4, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(4, summary.getReceivedPackets());
    }

    public void testCloseConnectionWithoutConnectDoesNothing() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.closeConnection(server);

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.LISTEN, server.getState());
    }

    public void testCloseConnectionAfterConnect() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);
        client.closeConnection(server);

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.CLOSED, server.getState());
    }

    public void testTransferWithWindowOneUsesSeveralCycles() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 1);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(3, summary.getCycles());
        assertEquals(3, summary.getReceivedPackets());
    }
}