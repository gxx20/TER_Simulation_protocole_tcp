package fr.uvsq.tcpsim;

import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TcpState;
import fr.uvsq.tcpsim.model.TransferRequest;
import fr.uvsq.tcpsim.model.TransferResult;
import fr.uvsq.tcpsim.server.TcpServer;
import junit.framework.TestCase;

public class TcpServerTest extends TestCase {

    public void testInitialStateIsListen() {
        TcpServer server = new TcpServer();

        assertEquals(TcpState.LISTEN, server.getState());
    }

    public void testReceiveSynReturnsSynAck() {
        TcpServer server = new TcpServer();

        Packet syn = new Packet(PacketType.SYN, 100, 0, null);
        Packet response = server.receivePacket(syn);

        assertNotNull(response);
        assertEquals(PacketType.SYN_ACK, response.getType());
        assertEquals(500, response.getSequenceNumber());
        assertEquals(101, response.getAcknowledgementNumber());
        assertEquals(TcpState.SYN_RECEIVED, server.getState());
    }

    public void testReceiveAckAfterSynAckEstablishesConnection() {
        TcpServer server = new TcpServer();

        Packet syn = new Packet(PacketType.SYN, 100, 0, null);
        server.receivePacket(syn);

        Packet ack = new Packet(PacketType.ACK, 101, 501, null);
        Packet response = server.receivePacket(ack);

        assertNull(response);
        assertEquals(TcpState.ESTABLISHED, server.getState());
    }

    public void testUnexpectedPacketReturnsNull() {
        TcpServer server = new TcpServer();

        Packet data = new Packet(PacketType.DATA, 1, 0, "test");
        Packet response = server.receivePacket(data);

        assertNull(response);
        assertEquals(TcpState.LISTEN, server.getState());
    }

    public void testCannotSendDataBeforeConnection() {
        TcpServer server = new TcpServer();

        TransferResult result = server.sendData(new TransferRequest(3, 2));

        assertNotNull(result);
        assertEquals(0, result.getSentPackets().size());
        assertEquals(3, result.getRemainingPackets());
    }

    public void testSendDataRespectsReceiveWindow() {
        TcpServer server = createEstablishedServer();

        TransferResult result = server.sendData(new TransferRequest(5, 2));

        assertEquals(2, result.getSentPackets().size());
        assertEquals(3, result.getRemainingPackets());
    }

    public void testSendDataWhenWindowGreaterThanRequest() {
        TcpServer server = createEstablishedServer();

        TransferResult result = server.sendData(new TransferRequest(3, 10));

        assertEquals(3, result.getSentPackets().size());
        assertEquals(0, result.getRemainingPackets());
    }

    public void testSendDataLimitedByAvailableSourceData() {
        TcpServer server = createEstablishedServer();

        TransferResult result = server.sendData(new TransferRequest(20, 20));

        assertEquals(8, result.getSentPackets().size());
        assertEquals(12, result.getRemainingPackets());
    }

    public void testSendDataSequenceNumbers() {
        TcpServer server = createEstablishedServer();

        TransferResult result = server.sendData(new TransferRequest(3, 3));
        List<Packet> packets = result.getSentPackets();

        assertEquals(3, packets.size());
        assertEquals(501, packets.get(0).getSequenceNumber());
        assertEquals(502, packets.get(1).getSequenceNumber());
        assertEquals(503, packets.get(2).getSequenceNumber());
    }

    public void testSendDataPayloads() {
        TcpServer server = createEstablishedServer();

        TransferResult result = server.sendData(new TransferRequest(3, 3));
        List<Packet> packets = result.getSentPackets();

        assertEquals("Bloc-1", packets.get(0).getPayload());
        assertEquals("Bloc-2", packets.get(1).getPayload());
        assertEquals("Bloc-3", packets.get(2).getPayload());
    }

    public void testAllPacketsCorruptedWhenProbabilityIsOne() {
        TcpServer server = createEstablishedServer(1.0, 0.0);

        TransferResult result = server.sendData(new TransferRequest(3, 3));

        assertEquals(3, result.getSentPackets().size());

        for (Packet packet : result.getSentPackets()) {
            assertTrue(packet.isCorrupted());
        }
    }

    public void testNoPacketCorruptedWhenProbabilityIsZero() {
        TcpServer server = createEstablishedServer(0.0, 0.0);

        TransferResult result = server.sendData(new TransferRequest(3, 3));

        assertEquals(3, result.getSentPackets().size());

        for (Packet packet : result.getSentPackets()) {
            assertFalse(packet.isCorrupted());
        }
    }

    public void testAllPacketsLostWhenLossProbabilityIsOne() {
        TcpServer server = createEstablishedServer(0.0, 1.0);

        TransferResult result = server.sendData(new TransferRequest(3, 3));

        assertEquals(0, result.getSentPackets().size());
        assertEquals(0, result.getRemainingPackets());
    }

    public void testRetransmitValidPacket() {
        TcpServer server = createEstablishedServer();

        Packet packet = server.retransmitPacket(501);

        assertNotNull(packet);
        assertEquals(PacketType.DATA, packet.getType());
        assertEquals(501, packet.getSequenceNumber());
        assertEquals("Bloc-1", packet.getPayload());
        assertFalse(packet.isCorrupted());
    }

    public void testRetransmitInvalidPacketTooSmall() {
        TcpServer server = createEstablishedServer();

        Packet packet = server.retransmitPacket(100);

        assertNull(packet);
    }

    public void testRetransmitInvalidPacketTooLarge() {
        TcpServer server = createEstablishedServer();

        Packet packet = server.retransmitPacket(999);

        assertNull(packet);
    }

    public void testResetTransferCursorRestartsDataFromBeginning() {
        TcpServer server = createEstablishedServer();

        TransferResult first = server.sendData(new TransferRequest(2, 2));
        assertEquals("Bloc-1", first.getSentPackets().get(0).getPayload());

        server.resetTransferCursor();

        TransferResult second = server.sendData(new TransferRequest(2, 2));
        assertEquals("Bloc-1", second.getSentPackets().get(0).getPayload());
    }

    public void testReceiveFinReturnsFinAck() {
        TcpServer server = createEstablishedServer();

        Packet fin = new Packet(PacketType.FIN, 1099, 0, null);
        Packet response = server.receivePacket(fin);

        assertNotNull(response);
        assertEquals(PacketType.FIN_ACK, response.getType());
        assertEquals(1100, response.getAcknowledgementNumber());
        assertEquals(TcpState.LAST_ACK, server.getState());
    }

    public void testReceiveFinalAckClosesConnection() {
        TcpServer server = createEstablishedServer();

        Packet fin = new Packet(PacketType.FIN, 1099, 0, null);
        server.receivePacket(fin);

        Packet ack = new Packet(PacketType.ACK, 1100, 601, null);
        Packet response = server.receivePacket(ack);

        assertNull(response);
        assertEquals(TcpState.CLOSED, server.getState());
    }

    private TcpServer createEstablishedServer() {
        return createEstablishedServer(0.0, 0.0);
    }

    private TcpServer createEstablishedServer(double corruptionProbability, double lossProbability) {
        TcpServer server = new TcpServer(corruptionProbability, lossProbability);

        Packet syn = new Packet(PacketType.SYN, 100, 0, null);
        server.receivePacket(syn);

        Packet ack = new Packet(PacketType.ACK, 101, 501, null);
        server.receivePacket(ack);

        return server;
    }
}