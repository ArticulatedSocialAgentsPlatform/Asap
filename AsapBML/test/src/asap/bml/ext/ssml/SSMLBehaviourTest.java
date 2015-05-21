/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.ssml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.BMLInfo;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.bmlt.BMLTInfo;

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
        SSMLBehaviour beh = new SSMLBehaviour("bml1","beh1", new XMLTokenizer(str));
        assertEquals("Hello world!",beh.getContent().trim());
    }
    
    @Test
    public void writeXML() throws IOException
    {
        String str ="<speak xmlns=\""+SSMLBehaviour.NAMESPACE+"\">Hello world!</speak>";
        SSMLBehaviour behIn = new SSMLBehaviour("bml1","beh1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        SSMLBehaviour behOut = new SSMLBehaviour("bml1",new XMLTokenizer(buf.toString())); 
        assertEquals("Hello world!",behOut.getContent().trim());        
    }
    
    @Test
    public void testWriteXMLDescription() throws IOException
    {
        final String SSML_TEXT = "Hello! <break time=\"3s\"/> <prosody pitch=\"high\">This is a basic SSML BML test</prosody>.";
        BMLTInfo.init();
        BMLInfo.supportedExtensions.add(SSMLBehaviour.class);
        String bml = "<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"speech1\" start=\"1\">"+
                    "<text>Hello! This is a basic core BML test!</text>" +                    	
                    "<description priority=\"2\" type=\"application/ssml+xml\">"+
                    "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\">"+
                    SSML_TEXT+
                    "</speak>"+
                    "</description>"+
                    "</speech>";
        SpeechBehaviour behIn = new SpeechBehaviour("bml1",new XMLTokenizer(bml));
        assertNotNull(behIn.descBehaviour); 
        
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        
        SpeechBehaviour behOut = new SpeechBehaviour("bml1",new XMLTokenizer(buf.toString())); 
        assertEquals(SSML_TEXT,((SSMLBehaviour)behOut.descBehaviour).getContent().trim());        
    }
}
