/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;

/**
 * Unit test cases for the BMLTControllerBehaviour
 * @author hvanwelbergen
 * 
 */
public class BMLTControllerBehaviourTest extends AbstractBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;
    
    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:controller xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" name=\"controller1\" class=\"DummyController\" id=\"a1\" "+ 
                extraAttributeString+"/>";        
                return new BMLTControllerBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTControllerBehaviour(bmlId, new XMLTokenizer(bmlString));
    }
    
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
