package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.BehaviourBlock;

/**
 * Unit tests for the activate behavior
 * @author welberge
 *
 */
public class BMLTActivateBehaviourTest
{
    static
    {
        BMLTInfo.init();
    }
    
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:activate xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" target=\"bml1\"/>";
        BMLTActivateBehaviour beh = new BMLTActivateBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla",beh.getBmlId());
        assertEquals("a1",beh.id);
        assertEquals("bml1",beh.getTarget());
        assertEquals("bml1",beh.getStringParameterValue("target"));
        assertEquals("nod1",beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end",beh.getSyncPoints().get(0).getRef().syncId);
    }
    
    @Test
    public void testActivateInBML() throws IOException
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\">"+
                            "<bmlt:activate xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" target=\"bml1\"/>"+
                           "</bml>";
        BehaviourBlock bb = new BehaviourBlock(new XMLTokenizer(bmlString));
        assertEquals(1,bb.behaviours.size());
        BMLTActivateBehaviour beh = (BMLTActivateBehaviour)bb.behaviours.get(0);
        assertEquals("bml1",beh.getBmlId());
        assertEquals("bml1",beh.getTarget());        
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String bmlString = "<bmlt:activate xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" target=\"bml1\"/>";
        BMLTActivateBehaviour behIn = new BMLTActivateBehaviour("bmla", new XMLTokenizer(bmlString));
        StringBuilder buf = new StringBuilder();        
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTActivateBehaviour behOut = new BMLTActivateBehaviour("bmla", new XMLTokenizer(buf.toString()));
        
        assertEquals("bmla",behOut.getBmlId());
        assertEquals("a1",behOut.id);
        assertEquals("bml1",behOut.getTarget());
        assertEquals("bml1",behOut.getStringParameterValue("target"));
        assertEquals("nod1",behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end",behOut.getSyncPoints().get(0).getRef().syncId);
    }
}
