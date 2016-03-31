/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * unit tests for the RemoteHeadBehaviour
 * @author welberge
 */
public class RemoteHeadBehaviourTest
{
    @Test
    public void testReadXML()
    {
        String str = "<remoteHead xmlns=\"http://asap-project.org/livemocap\" " + "id=\"rhead1\" input=\"input1\" output=\"output1\"/>";
        RemoteHeadBehaviour beh = new RemoteHeadBehaviour("bml1");
        beh.readXML(str);
        assertEquals("bml1", beh.getBmlId());
        assertEquals("rhead1", beh.id);
        assertEquals("output1", beh.getStringParameterValue("output"));
        assertEquals("input1", beh.getStringParameterValue("input"));
    }

    @Test
    public void writeXML() throws IOException
    {
        String str = "<remoteHead xmlns=\"http://asap-project.org/livemocap\" " + "id=\"rhead1\" input=\"input1\" output=\"output1\"/>";
        RemoteHeadBehaviour behIn = new RemoteHeadBehaviour("bml1");
        behIn.readXML(str);

        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "xmlns", "http://asap-project.org/livemocap");
        RemoteHeadBehaviour behOut = new RemoteHeadBehaviour("bml1", new XMLTokenizer(buf.toString()));

        assertEquals("bml1", behOut.getBmlId());
        assertEquals("rhead1", behOut.id);
        assertEquals("output1", behOut.getStringParameterValue("output"));
        assertEquals("input1", behOut.getStringParameterValue("input"));
    }
}
