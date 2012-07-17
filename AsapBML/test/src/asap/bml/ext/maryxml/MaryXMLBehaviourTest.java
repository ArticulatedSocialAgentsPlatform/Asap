package asap.bml.ext.maryxml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit test cases for the MaryXMLBehaviour
 * @author welberge
 *
 */
public class MaryXMLBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryXMLBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryXMLBehaviour beh = new MaryXMLBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("Hello world!", beh.getContent());        
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryXMLBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryXMLBehaviour behIn = new MaryXMLBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        MaryXMLBehaviour behOut = new MaryXMLBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("Hello world!", behOut.getContent().trim());
    }
}
