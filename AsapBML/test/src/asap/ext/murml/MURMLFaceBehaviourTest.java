package asap.ext.murml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import saiba.bml.BMLInfo;
import saiba.bml.core.FaceLexemeBehaviour;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
/**
 * Unit test cases for MURMLFaceBehaviour
 * @author hvanwelbergen
 * 
 */
public class MURMLFaceBehaviourTest
{
    private static final double FRAME_PRECISION = 0.0001;
    
    @Test
    public void testReadKeyframe() throws IOException
    {
        String bmlString = "<murml:murmlface xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" "
                + "id=\"a1\" start=\"nod1:end\">" + "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>" + "</murml:murmlface>";
        MURMLFaceBehaviour beh = new MURMLFaceBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertNull(beh.getMurmlDefinition().getPosture());
        assertEquals(0, beh.getMurmlDefinition().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }

    @Test
    public void testExtension() throws IOException
    {
        BMLInfo.addDescriptionExtension(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
        BMLInfo.supportedExtensions.add(MURMLFaceBehaviour.class);
        String murmlString = "<murml:murmlface xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" "
                + "id=\"a1\">" + "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>" + "</murml:murmlface>";
        String bmlString = "<faceLexeme id=\"a1\" lexeme=\"BLINK\"><description priority=\"1\" type=\"murmlface\">"+murmlString+"</description></faceLexeme>";
        FaceLexemeBehaviour f = new FaceLexemeBehaviour("bmla",new XMLTokenizer(bmlString));
        assertThat(f.descBehaviour, instanceOf(MURMLFaceBehaviour.class));
        MURMLFaceBehaviour beh = (MURMLFaceBehaviour)f.descBehaviour;
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertNull(beh.getMurmlDefinition().getPosture());
        assertEquals(0, beh.getMurmlDefinition().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }
}
