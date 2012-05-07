package hmi.bml.ext.bmlt;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit testcases for the BMLTAudioFileBehaviour
 * @author hvanwelbergen
 *
 */
public class BMLTAudioFileBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" fileName=\"blah.wav\"/>";
        BMLTAudioFileBehaviour beh = new BMLTAudioFileBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla",beh.getBmlId());
        assertEquals("a1",beh.id);
        assertEquals("blah.wav",beh.getStringParameterValue("fileName"));
        assertEquals("nod1",beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end",beh.getSyncPoints().get(0).getRef().syncId);
    }
    
    @Test
    public void writeXML() throws IOException
    {
        String bmlString = "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" fileName=\"blah.wav\"/>";
        BMLTAudioFileBehaviour behIn = new BMLTAudioFileBehaviour("bmla", new XMLTokenizer(bmlString));
        StringBuilder buf = new StringBuilder();        
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTAudioFileBehaviour behOut = new BMLTAudioFileBehaviour("bmla", new XMLTokenizer(buf.toString()));
        
        assertEquals("bmla",behOut.getBmlId());
        assertEquals("a1",behOut.id);
        assertEquals("blah.wav",behOut.getStringParameterValue("fileName"));
        assertEquals("nod1",behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end",behOut.getSyncPoints().get(0).getRef().syncId);
    }
}
