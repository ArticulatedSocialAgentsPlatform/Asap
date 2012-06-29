package asap.bml.ext.ssml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit tests for the SSMLBehaviour
 * @author welberge
 */
public class SSMLBehaviourTest
{
    @Test
    public void readXML() throws IOException
    {
        String str ="<speak xmlns=\""+SSMLBehaviour.NAMESPACE+"\">Hello world!</speak>";
        SSMLBehaviour beh = new SSMLBehaviour("bml1",new XMLTokenizer(str));
        assertEquals("Hello world!",beh.getContent().trim());
    }
    
    @Test
    public void writeXML() throws IOException
    {
        String str ="<speak xmlns=\""+SSMLBehaviour.NAMESPACE+"\">Hello world!</speak>";
        SSMLBehaviour behIn = new SSMLBehaviour("bml1",new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        SSMLBehaviour behOut = new SSMLBehaviour("bml1",new XMLTokenizer(buf.toString())); 
        assertEquals("Hello world!",behOut.getContent().trim());
    }
}
