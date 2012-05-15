package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit tests for the parametervaluechange behavior
 * @author welberge
 * 
 */
public class BMLTParameterValueChangeBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:parametervaluechange xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" "
                + "paramId=\"volume\" target=\"bml1:speech1\">"
                + "<bmlt:trajectory type=\"linear\" targetValue=\"100\" initialValue=\"0\"/>" + "</bmlt:parametervaluechange>";
        BMLTParameterValueChangeBehaviour beh = new BMLTParameterValueChangeBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertEquals("linear", beh.getStringParameterValue("type"));
        assertEquals("0", beh.getStringParameterValue("initialValue"));
        assertEquals("100", beh.getStringParameterValue("targetValue"));
        assertEquals("bml1:speech1", beh.getStringParameterValue("target"));
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String bmlString = "<bmlt:parametervaluechange xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" "
                + "paramId=\"volume\" target=\"bml1:speech1\">"
                + "<bmlt:trajectory type=\"linear\" targetValue=\"100\" initialValue=\"0\"/>" + "</bmlt:parametervaluechange>";
        BMLTParameterValueChangeBehaviour behIn = new BMLTParameterValueChangeBehaviour("bmla", new XMLTokenizer(bmlString));

        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTParameterValueChangeBehaviour behOut = new BMLTParameterValueChangeBehaviour("bmla", new XMLTokenizer(buf.toString()));

        assertEquals("bmla", behOut.getBmlId());
        assertEquals("a1", behOut.id);
        assertEquals("linear", behOut.getStringParameterValue("type"));
        assertEquals("0", behOut.getStringParameterValue("initialValue"));
        assertEquals("100", behOut.getStringParameterValue("targetValue"));
        assertEquals("bml1:speech1", behOut.getStringParameterValue("target"));
        assertEquals("nod1", behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", behOut.getSyncPoints().get(0).getRef().syncId);
    }
}
