package fr.uvsq.tcpsim;

import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.uvsq.tcpsim.client.TcpClient;
import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.model.TransferSummary;
import fr.uvsq.tcpsim.server.TcpServer;

public class FeatureTest extends TestCase {

    public void testServerAlwaysCorrupt() {
        TcpServer server = new TcpServer(1.0, 0.0); // corruption 100%
        TcpClient client = new TcpClient();
        client.connect(server);
        server.resetTransferCursor();
        TransferResult res = server.sendData(new TransferRequest(1, 1));
        List<Packet> packets = res.getSentPackets();
        assertTrue("Server should send at least one packet", packets.size() >= 1);
        assertTrue("Packet should be corrupted", packets.get(0).isCorrupted());
    }

    public void testTransferSummaryCsv() {
        TransferSummary s = new TransferSummary(5,5,2,2,3,2,true);
        String header = TransferSummary.csvHeader();
        String row = s.toCsvRow();
        assertTrue(header.contains("requestedPackets"));
        assertTrue(row.startsWith("5,5,2,2"));
    }

    public void testClientListenerNotified() {
        TcpServer server = new TcpServer(0.0, 0.0);
        TcpClient client = new TcpClient();
        client.connect(server);
        AtomicInteger last = new AtomicInteger(0);
        client.setTransferListener((received, requested) -> last.set(received));
        server.resetTransferCursor();
        TransferSummary summary = client.requestAllData(server, 3, 2);
        assertTrue("Client should have received packets", last.get() > 0);
        assertNotNull("Summary should not be null", summary);
    }
}
