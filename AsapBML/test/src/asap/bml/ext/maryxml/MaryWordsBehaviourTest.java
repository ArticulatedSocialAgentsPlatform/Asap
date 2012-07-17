package asap.bml.ext.maryxml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit test cases for the MaryWordsBehaviour
 * @author welberge
 *
 */
public class MaryWordsBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryWordsBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryWordsBehaviour beh = new MaryWordsBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("Hello world!", beh.getContent());        
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<maryxml xmlns=\""+MaryWordsBehaviour.NAMESPACE+"\" >"+"Hello world!"+"</maryxml>";
        MaryWordsBehaviour behIn = new MaryWordsBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        MaryWordsBehaviour behOut = new MaryWordsBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("Hello world!", behOut.getContent().trim());
    }
}
