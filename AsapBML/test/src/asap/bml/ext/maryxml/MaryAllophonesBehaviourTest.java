package asap.bml.ext.maryxml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit test cases for the MaryAllophonesBehaviour
 * @author welberge
 *
 */
public class MaryAllophonesBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryAllophonesBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryAllophonesBehaviour beh = new MaryAllophonesBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("Hello world!", beh.getContent());        
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryAllophonesBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryAllophonesBehaviour behIn = new MaryAllophonesBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        MaryAllophonesBehaviour behOut = new MaryAllophonesBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("Hello world!", behOut.getContent().trim());
    }
}
