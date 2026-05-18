package fr.uvsq.tcpsim;

import fr.uvsq.tcpsim.model.TransferRequest;
import junit.framework.TestCase;

public class TransferRequestTest extends TestCase {

    public void testConstructor() {
        TransferRequest request = new TransferRequest(5, 3);

        assertEquals(5, request.getNumberOfPacketsRequested());
        assertEquals(3, request.getReceiveWindow());
    }

    public void testSetNumberOfPacketsRequested() {
        TransferRequest request = new TransferRequest(5, 3);

        request.setNumberOfPacketsRequested(10);

        assertEquals(10, request.getNumberOfPacketsRequested());
    }

    public void testSetReceiveWindow() {
        TransferRequest request = new TransferRequest(5, 3);

        request.setReceiveWindow(7);

        assertEquals(7, request.getReceiveWindow());
    }

    public void testToStringContainsValues() {
        TransferRequest request = new TransferRequest(8, 4);

        String text = request.toString();

        assertTrue(text.contains("numberOfPacketsRequested=8"));
        assertTrue(text.contains("receiveWindow=4"));
    }

    public void testZeroValues() {
        TransferRequest request = new TransferRequest(0, 0);

        assertEquals(0, request.getNumberOfPacketsRequested());
        assertEquals(0, request.getReceiveWindow());
    }

    public void testNegativeValues() {
        TransferRequest request = new TransferRequest(-5, -2);

        assertEquals(-5, request.getNumberOfPacketsRequested());
        assertEquals(-2, request.getReceiveWindow());
    }

    public void testLargeValues() {
        TransferRequest request = new TransferRequest(1000, 500);

        assertEquals(1000, request.getNumberOfPacketsRequested());
        assertEquals(500, request.getReceiveWindow());
    }

    public void testModifyValuesSeveralTimes() {
        TransferRequest request = new TransferRequest(1, 1);

        request.setNumberOfPacketsRequested(2);
        request.setReceiveWindow(3);

        request.setNumberOfPacketsRequested(4);
        request.setReceiveWindow(5);

        assertEquals(4, request.getNumberOfPacketsRequested());
        assertEquals(5, request.getReceiveWindow());
    }
}