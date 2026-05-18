package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;
import junit.framework.TestCase;

public class MainTest extends TestCase {

    public void testSimulationComplete() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 5, 2);

        client.closeConnection(server);

        assertNotNull(summary);
        assertTrue(summary.isCompleted());

        assertEquals(5, summary.getRequestedPackets());
        assertEquals(5, summary.getReceivedPackets());

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.CLOSED, server.getState());
    }

    public void testSimulationWithCorruption() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(1.0, 0.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertTrue(summary.getCorruptedPacketsDetected() > 0);
    }

    public void testSimulationWithLoss() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer(0.0, 1.0);

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 3, 2);

        assertNotNull(summary);
        assertTrue(summary.getReceivedPackets() <= 3);
    }

    public void testConnectionLifecycle() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer();

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.LISTEN, server.getState());

        client.connect(server);

        assertEquals(TcpState.ESTABLISHED, client.getState());
        assertEquals(TcpState.ESTABLISHED, server.getState());

        client.closeConnection(server);

        assertEquals(TcpState.CLOSED, client.getState());
        assertEquals(TcpState.CLOSED, server.getState());
    }

    public void testInvalidTransferRequest() {
        TcpClient client = new TcpClient();
        TcpServer server = new TcpServer();

        client.connect(server);

        TransferSummary summary = client.requestAllData(server, 0, 0);

        assertNotNull(summary);
        assertFalse(summary.isCompleted());
    }
}