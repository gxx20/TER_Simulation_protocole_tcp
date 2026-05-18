package fr.uvsq.tcpsim;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;
import junit.framework.TestCase;

public class FeatureTest extends TestCase {

    public void testInitialStates() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer();

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.LISTEN, server.getState());
    }

    public void testConnectionEstablished() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        assertEquals(TcpState.ESTABLISHED, client.getState());
        assertEquals(TcpState.ESTABLISHED, server.getState());
    }

    public void testServerRespectsReceiveWindow() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferRequest request = new TransferRequest(5, 2);
        TransferResult result = server.sendData(request);

        assertEquals(2, result.getSentPackets().size());
        assertEquals(3, result.getRemainingPackets());
    }

    public void testServerCannotSendDataBeforeConnection() {
        TcpServer server = new TcpServer(0.0, 0.0);

        TransferResult result = server.sendData(new TransferRequest(3, 2));

        assertNotNull(result);
        assertEquals(0, result.getSentPackets().size());
        assertEquals(3, result.getRemainingPackets());
    }

    public void testTransferWithWindowGreaterThanRequest() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferResult result = server.sendData(new TransferRequest(3, 10));

        assertEquals(3, result.getSentPackets().size());
        assertEquals(0, result.getRemainingPackets());
    }

    public void testTransferLimitedByAvailableData() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferResult result = server.sendData(new TransferRequest(20, 20));

        assertEquals(8, result.getSentPackets().size());
        assertEquals(12, result.getRemainingPackets());
    }

    public void testServerAlwaysCorrupt() {
        TcpServer server = new TcpServer(1.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferResult result = server.sendData(new TransferRequest(1, 1));
        List<Packet> packets = result.getSentPackets();

        assertTrue(packets.size() >= 1);
        assertTrue(packets.get(0).isCorrupted());
    }

    public void testRetransmissionReturnsCleanPacket() {
        TcpServer server = new TcpServer(1.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferResult result = server.sendData(new TransferRequest(1, 1));
        Packet corruptedPacket = result.getSentPackets().get(0);

        Packet retransmitted = server.retransmitPacket(corruptedPacket.getSequenceNumber());

        assertNotNull(retransmitted);
        assertEquals(corruptedPacket.getSequenceNumber(), retransmitted.getSequenceNumber());
        assertFalse(retransmitted.isCorrupted());
    }

    public void testCompleteTransferWithoutError() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 4, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(4, summary.getRequestedPackets());
        assertEquals(4, summary.getReceivedPackets());
        assertEquals(0, summary.getCorruptedPacketsDetected());
    }

    public void testCompleteTransferWithCorruptionAndRetransmission() {
        TcpServer server = new TcpServer(1.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(3, summary.getRequestedPackets());
        assertEquals(3, summary.getReceivedPackets());
        assertTrue(summary.getCorruptedPacketsDetected() > 0);
    }

    public void testCompleteTransferInSeveralCycles() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 6, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(6, summary.getReceivedPackets());
        assertEquals(3, summary.getCycles());
    }

    public void testInvalidTransferParameters() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 0, 2);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
    }

    public void testClientListenerNotified() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);

        AtomicInteger lastReceived = new AtomicInteger(0);
        client.setTransferListener((received, requested) -> lastReceived.set(received));

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertTrue(lastReceived.get() > 0);
    }

    public void testCloseConnection() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);
        client.closeConnection(server);

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.CLOSED, server.getState());
    }

    public void testClientCannotCloseBeforeConnection() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.closeConnection(server);

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.LISTEN, server.getState());
    }

    public void testCancelTransferStopsBeforeCompletion() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);
        client.cancelTransfer();

        TransferSummary summary = client.requestAllData(server, 5, 2);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
        assertEquals(0, summary.getReceivedPackets());
    }

    public void testResetCancelAllowsTransferAgain() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();

        client.connect(server);
        client.cancelTransfer();
        client.resetCancel();

        TransferSummary summary = client.requestAllData(server, 4, 2);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());
        assertEquals(4, summary.getReceivedPackets());
    }

    public void testTransferSummaryCsv() {
        TransferSummary summary = new TransferSummary(5, 5, 2, 2, 3, 2, true);

        String header = TransferSummary.csvHeader();
        String row = summary.toCsvRow();

        assertTrue(header.contains("requestedPackets"));
        assertTrue(row.startsWith("5,5,2,2"));
    }
}