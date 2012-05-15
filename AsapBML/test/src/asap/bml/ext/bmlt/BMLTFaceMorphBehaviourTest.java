package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit testcases for the bmlt facemorph behavior
 * @author hvanwelbergen
 * 
 */
public class BMLTFaceMorphBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;
    
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:facemorph xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" "
                + "id=\"a1\" start=\"nod1:end\" targetname=\"target1\" intensity=\"0.5\"/>";
        BMLTFaceMorphBehaviour beh = new BMLTFaceMorphBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);
        assertEquals("target1", beh.getStringParameterValue("targetname"));
        assertEquals(0.5, beh.getFloatParameterValue("intensity"), PARAMETER_PRECISION);
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String bmlString = "<bmlt:facemorph xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" "
                + "id=\"a1\" start=\"nod1:end\" targetname=\"target1\" intensity=\"0.5\"/>";
        BMLTFaceMorphBehaviour behIn = new BMLTFaceMorphBehaviour("bmla", new XMLTokenizer(bmlString));

        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTFaceMorphBehaviour behOut = new BMLTFaceMorphBehaviour("bmla", new XMLTokenizer(buf.toString()));

        assertEquals("bmla", behOut.getBmlId());
        assertEquals("a1", behOut.id);
        assertEquals("target1", behOut.getStringParameterValue("targetname"));
        assertEquals(0.5, behOut.getFloatParameterValue("intensity"), PARAMETER_PRECISION);
        assertEquals("nod1", behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", behOut.getSyncPoints().get(0).getRef().syncId);
    }
}
