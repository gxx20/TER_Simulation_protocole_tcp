package fr.uvsq.tcpsim;

import java.util.ArrayList;
import java.util.List;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import fr.uvsq.tcpsim.model.TransferResult;
import junit.framework.TestCase;

public class TransferResultTest extends TestCase {

    public void testConstructor() {
        List<Packet> packets = new ArrayList<>();

        packets.add(new Packet(PacketType.DATA, 1, 0, "Bloc-1"));

        TransferResult result = new TransferResult(packets, 3);

        assertEquals(1, result.getSentPackets().size());
        assertEquals(3, result.getRemainingPackets());
    }

    public void testGetSentPackets() {
        List<Packet> packets = new ArrayList<>();

        packets.add(new Packet(PacketType.DATA, 1, 0, "A"));
        packets.add(new Packet(PacketType.DATA, 2, 0, "B"));

        TransferResult result = new TransferResult(packets, 0);

        assertEquals(2, result.getSentPackets().size());
    }

    public void testSetSentPackets() {
        TransferResult result = new TransferResult(new ArrayList<>(), 0);

        List<Packet> packets = new ArrayList<>();
        packets.add(new Packet(PacketType.DATA, 10, 0, "Test"));

        result.setSentPackets(packets);

        assertEquals(1, result.getSentPackets().size());
        assertEquals(10, result.getSentPackets().get(0).getSequenceNumber());
    }

    public void testGetRemainingPackets() {
        TransferResult result = new TransferResult(new ArrayList<>(), 5);

        assertEquals(5, result.getRemainingPackets());
    }

    public void testSetRemainingPackets() {
        TransferResult result = new TransferResult(new ArrayList<>(), 5);

        result.setRemainingPackets(2);

        assertEquals(2, result.getRemainingPackets());
    }

    public void testToStringContainsValues() {
        List<Packet> packets = new ArrayList<>();
        packets.add(new Packet(PacketType.DATA, 1, 0, "Bloc"));

        TransferResult result = new TransferResult(packets, 4);

        String text = result.toString();

        assertTrue(text.contains("remainingPackets=4"));
        assertTrue(text.contains("sentPackets"));
    }

    public void testEmptyPacketList() {
        TransferResult result = new TransferResult(new ArrayList<>(), 0);

        assertEquals(0, result.getSentPackets().size());
        assertEquals(0, result.getRemainingPackets());
    }

    public void testLargeRemainingPackets() {
        TransferResult result = new TransferResult(new ArrayList<>(), 100);

        assertEquals(100, result.getRemainingPackets());
    }

    public void testModifySeveralTimes() {
        TransferResult result = new TransferResult(new ArrayList<>(), 1);

        result.setRemainingPackets(5);
        result.setRemainingPackets(10);

        assertEquals(10, result.getRemainingPackets());
    }
}