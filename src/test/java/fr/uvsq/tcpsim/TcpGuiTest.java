package fr.uvsq.tcpsim;

import java.lang.reflect.Method;

import fr.uvsq.tcpsim.gui.TcpGui;
import junit.framework.TestCase;

public class TcpGuiTest extends TestCase {

    public void testParseIntOrDefaultWithValidValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseIntOrDefault",
                String.class,
                int.class
        );

        method.setAccessible(true);

        int result = (int) method.invoke(null, "10", 5);

        assertEquals(10, result);
    }

    public void testParseIntOrDefaultWithInvalidValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseIntOrDefault",
                String.class,
                int.class
        );

        method.setAccessible(true);

        int result = (int) method.invoke(null, "abc", 5);

        assertEquals(5, result);
    }

    public void testParseIntOrDefaultWithNegativeValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseIntOrDefault",
                String.class,
                int.class
        );

        method.setAccessible(true);

        int result = (int) method.invoke(null, "-3", 5);

        assertEquals(5, result);
    }

    public void testParseIntOrDefaultWithZeroValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseIntOrDefault",
                String.class,
                int.class
        );

        method.setAccessible(true);

        int result = (int) method.invoke(null, "0", 5);

        assertEquals(5, result);
    }

    public void testParseDoubleOrDefaultWithValidValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseDoubleOrDefault",
                String.class,
                double.class
        );

        method.setAccessible(true);

        double result = (double) method.invoke(null, "0.25", 0.5);

        assertEquals(0.25, result);
    }

    public void testParseDoubleOrDefaultWithInvalidValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseDoubleOrDefault",
                String.class,
                double.class
        );

        method.setAccessible(true);

        double result = (double) method.invoke(null, "abc", 0.5);

        assertEquals(0.5, result);
    }

    public void testParseDoubleOrDefaultWithNegativeValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseDoubleOrDefault",
                String.class,
                double.class
        );

        method.setAccessible(true);

        double result = (double) method.invoke(null, "-0.1", 0.5);

        assertEquals(0.5, result);
    }

    public void testParseDoubleOrDefaultWithZeroValue() throws Exception {
        Method method = TcpGui.class.getDeclaredMethod(
                "parseDoubleOrDefault",
                String.class,
                double.class
        );

        method.setAccessible(true);

        double result = (double) method.invoke(null, "0.0", 0.5);

        assertEquals(0.0, result);
    }
}