package asap.ext.murml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit test cases for MURMLFaceBehaviour
 * @author hvanwelbergen
 *
 */
public class MURMLFaceBehaviourTest
{
    @Test
    public void testReadKeyframe() throws IOException
    {
        String bmlString = "<murml:murmlface xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" " +
        		"id=\"a1\" start=\"nod1:end\">" +
                "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "+
                "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>"+
        		"</murml:murmlface>";
        MURMLFaceBehaviour beh = new MURMLFaceBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla",beh.getBmlId());
        assertEquals("a1",beh.id);
        
        assertNull(beh.getMurmlDefinition().getPosture());
        assertEquals(0, beh.getMurmlDefinition().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), 0.001);        
    }
}
