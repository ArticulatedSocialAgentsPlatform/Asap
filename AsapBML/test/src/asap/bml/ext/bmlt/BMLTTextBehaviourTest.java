/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.utils.TestUtil;

/**
 * Unit tests for the BMLTTextBehaviour
 * @author hvanwelbergen
 *
 */
public class BMLTTextBehaviourTest extends AbstractBehaviourTest
{
    static
    {
        BMLTInfo.init();
    }

    @Override
    protected BMLTTextBehaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:text xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"t1\" "+ extraAttributeString +">Hello world.</bmlt:text>";
        return new BMLTTextBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected BMLTTextBehaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTTextBehaviour(bmlId, new XMLTokenizer(bmlString));
    }
    
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlt:text xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"t1\">Hello world.</bmlt:text>";
        BMLTTextBehaviour beh = new BMLTTextBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("bml1", beh.getBmlId());
        assertEquals("t1", beh.id);
        assertEquals("Hello world.", beh.getContent());
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlt:text xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"t1\">Hello world.</bmlt:text>";
        BMLTTextBehaviour behIn = new BMLTTextBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");        
        
        BMLTTextBehaviour behOut = new BMLTTextBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("bml1", behOut.getBmlId());
        assertEquals("t1", behOut.id);
        assertEquals("Hello world.", behOut.getContent().trim());
    }
}
