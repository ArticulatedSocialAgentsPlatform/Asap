package hmi.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit test cases for the BMLTControllerBehaviour
 * @author hvanwelbergen
 * 
 */
public class BMLTControllerBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;
    
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:controller xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" "
                + "name=\"controller1\" class=\"DummyController\">" + "<bmlt:parameter name=\"k\" value=\"2\"/>" + "</bmlt:controller>";
        BMLTControllerBehaviour beh = new BMLTControllerBehaviour("bmla", new XMLTokenizer(bmlString));

        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);
        assertEquals("controller1", beh.name);
        assertEquals("DummyController", beh.className);
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
        assertEquals(2, beh.getFloatParameterValue("k"), PARAMETER_PRECISION);
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String bmlString = "<bmlt:controller xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" "
                + "name=\"controller1\" class=\"DummyController\">" + "<bmlt:parameter name=\"k\" value=\"2\"/>" + "</bmlt:controller>";
        BMLTControllerBehaviour behIn = new BMLTControllerBehaviour("bmla", new XMLTokenizer(bmlString));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTControllerBehaviour behOut = new BMLTControllerBehaviour("bmla", new XMLTokenizer(buf.toString()));

        assertEquals("bmla", behOut.getBmlId());
        assertEquals("a1", behOut.id);
        assertEquals("controller1", behOut.name);
        assertEquals("DummyController", behOut.className);
        assertEquals("nod1", behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", behOut.getSyncPoints().get(0).getRef().syncId);
        assertEquals(2, behOut.getFloatParameterValue("k"), PARAMETER_PRECISION);
    }
}
