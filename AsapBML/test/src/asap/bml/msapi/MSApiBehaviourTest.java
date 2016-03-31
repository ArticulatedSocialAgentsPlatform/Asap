/*******************************************************************************
 *******************************************************************************/
package asap.bml.msapi;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.bml.ext.msapi.MSApiBehaviour;

/**
 * Unit test cases for the MSApiBehaviour
 * @author welberge
 */
public class MSApiBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<sapi>Hello world!</sapi>";
        MSApiBehaviour beh = new MSApiBehaviour("bml1",new XMLTokenizer(str));
        assertEquals("Hello world!",beh.getContent().trim());
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<sapi>Hello world!</sapi>";
        MSApiBehaviour behIn = new MSApiBehaviour("bml1",new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        MSApiBehaviour behOut = new MSApiBehaviour("bml1",new XMLTokenizer(buf.toString()));
        assertEquals("Hello world!",behOut.getContent().trim());        
    }
}
