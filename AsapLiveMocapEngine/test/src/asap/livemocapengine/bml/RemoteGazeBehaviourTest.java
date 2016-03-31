/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

public class RemoteGazeBehaviourTest
{
    @Test
    public void testReadXML()
    {
        String str = "<remoteGaze xmlns=\"http://asap-project.org/livemocap\" " +
                "id=\"rgaze1\" input=\"input1\" output=\"output1\"/>";
        RemoteGazeBehaviour beh = new RemoteGazeBehaviour("bml1");
        beh.readXML(str);
        assertEquals("bml1", beh.getBmlId());
        assertEquals("rgaze1",beh.id);
        assertEquals("output1", beh.getStringParameterValue("output"));
        assertEquals("input1", beh.getStringParameterValue("input"));
    }
    
    @Test
    public void writeXML() throws IOException
    {
        String str = "<remoteGaze xmlns=\"http://asap-project.org/livemocap\" " + "id=\"rgaze1\" input=\"input1\" output=\"output1\"/>";
        RemoteGazeBehaviour behIn = new RemoteGazeBehaviour("bml1");
        behIn.readXML(str);

        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "xmlns", "http://asap-project.org/livemocap");
        RemoteGazeBehaviour behOut = new RemoteGazeBehaviour("bml1", new XMLTokenizer(buf.toString()));

        assertEquals("bml1", behOut.getBmlId());
        assertEquals("rgaze1", behOut.id);
        assertEquals("output1", behOut.getStringParameterValue("output"));
        assertEquals("input1", behOut.getStringParameterValue("input"));
    }
}
