package asap.ext.murml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hmi.bml.BMLInfo;
import hmi.bml.core.FaceBehaviour;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit test cases for the MURML gesture behaviour
 * @author hvanwelbergen
 *
 */
public class MURMLGestureBehaviourTest
{
    private static final double FRAME_PRECISION = 0.0001;
    
    @Test
    public void testReadKeyframe() throws IOException
    {
        String bmlString = "<murml:murmlgesture xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" "
                + "id=\"a1\" start=\"nod1:end\">" + "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 70 0 0)</posture></frame></phase></keyframing></definition>" + "</murml:murmlgesture>";
        MURMLGestureBehaviour beh = new MURMLGestureBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertNull(beh.getMurmlDefinition().getPosture());
        assertEquals(0, beh.getMurmlDefinition().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }
    
    @Test
    public void testExtension() throws IOException
    {
        BMLInfo.addDescriptionExtension(MURMLGestureBehaviour.xmlTag(), MURMLGestureBehaviour.class);
        BMLInfo.supportedExtensions.add(MURMLGestureBehaviour.class);
        String murmlString = "<murml:murmlgesture xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" "
                + "id=\"a1\">" + "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>" + "</murml:murmlgesture>";
        String bmlString = "<face id=\"a1\" type=\"BLINK\"><description priority=\"1\" type=\"murmlgesture\">"+murmlString+"</description></face>";
        FaceBehaviour f = new FaceBehaviour("bmla",new XMLTokenizer(bmlString));
        assertThat(f.descBehaviour, instanceOf(MURMLGestureBehaviour.class));
        MURMLGestureBehaviour beh = (MURMLGestureBehaviour)f.descBehaviour;
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertNull(beh.getMurmlDefinition().getPosture());
        assertEquals(0, beh.getMurmlDefinition().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }
}
