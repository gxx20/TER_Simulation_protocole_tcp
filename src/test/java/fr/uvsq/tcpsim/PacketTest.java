package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.model.Packet;
import fr.uvsq.tcpsim.model.PacketType;
import junit.framework.TestCase;

public class PacketTest extends TestCase {

    public void testConstructor() {
        Packet packet = new Packet(
                PacketType.DATA,
                10,
                20,
                "Bonjour"
        );

        assertEquals(PacketType.DATA, packet.getType());
        assertEquals(10, packet.getSequenceNumber());
        assertEquals(20, packet.getAcknowledgementNumber());
        assertEquals("Bonjour", packet.getPayload());
        assertFalse(packet.isCorrupted());
    }

    public void testSetType() {
        Packet packet = new Packet(PacketType.SYN, 1, 0, null);

        packet.setType(PacketType.ACK);

        assertEquals(PacketType.ACK, packet.getType());
    }

    public void testSetSequenceNumber() {
        Packet packet = new Packet(PacketType.DATA, 1, 0, "A");

        packet.setSequenceNumber(99);

        assertEquals(99, packet.getSequenceNumber());
    }

    public void testSetAcknowledgementNumber() {
        Packet packet = new Packet(PacketType.DATA, 1, 0, "A");

        packet.setAcknowledgementNumber(55);

        assertEquals(55, packet.getAcknowledgementNumber());
    }

    public void testSetPayload() {
        Packet packet = new Packet(PacketType.DATA, 1, 0, "A");

        packet.setPayload("Bloc");

        assertEquals("Bloc", packet.getPayload());
    }

    public void testSetCorruptedTrue() {
        Packet packet = new Packet(PacketType.DATA, 1, 0, "A");

        packet.setCorrupted(true);

        assertTrue(packet.isCorrupted());
    }

    public void testSetCorruptedFalse() {
        Packet packet = new Packet(PacketType.DATA, 1, 0, "A");

        packet.setCorrupted(true);
        packet.setCorrupted(false);

        assertFalse(packet.isCorrupted());
    }

    public void testToStringContainsValues() {
        Packet packet = new Packet(
                PacketType.DATA,
                15,
                30,
                "Message"
        );

        packet.setCorrupted(true);

        String text = packet.toString();

        assertTrue(text.contains("DATA"));
        assertTrue(text.contains("sequenceNumber=15"));
        assertTrue(text.contains("acknowledgementNumber=30"));
        assertTrue(text.contains("payload='Message'"));
        assertTrue(text.contains("corrupted=true"));
    }

    public void testPacketWithNullPayload() {
        Packet packet = new Packet(
                PacketType.SYN,
                1,
                0,
                null
        );

        assertNull(packet.getPayload());
    }

    public void testPacketWithNegativeNumbers() {
        Packet packet = new Packet(
                PacketType.DATA,
                -1,
                -5,
                "Erreur"
        );

        assertEquals(-1, packet.getSequenceNumber());
        assertEquals(-5, packet.getAcknowledgementNumber());
    }

    public void testModifyPacketSeveralTimes() {
        Packet packet = new Packet(
                PacketType.DATA,
                1,
                1,
                "A"
        );

        packet.setSequenceNumber(2);
        packet.setAcknowledgementNumber(3);
        packet.setPayload("B");
        packet.setCorrupted(true);

        assertEquals(2, packet.getSequenceNumber());
        assertEquals(3, packet.getAcknowledgementNumber());
        assertEquals("B", packet.getPayload());
        assertTrue(packet.isCorrupted());
    }
}