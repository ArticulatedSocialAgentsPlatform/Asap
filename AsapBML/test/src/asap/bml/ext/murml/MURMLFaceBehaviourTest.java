/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.murml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.BMLInfo;
import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.bml.core.FaceLexemeBehaviour;

/**
 * Unit test cases for MURMLFaceBehaviour
 * @author hvanwelbergen
 * 
 */
public class MURMLFaceBehaviourTest extends AbstractBehaviourTest
{
    private static final double FRAME_PRECISION = 0.0001;

    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<murmlface xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" id=\"f1\" " + extraAttributeString+ ">"
                + "<murml-description><dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>" + "</murml:murmlface>";                
        return new MURMLFaceBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new MURMLFaceBehaviour(bmlId, new XMLTokenizer(bmlString));
    }

    @Test
    public void testReadKeyframe() throws IOException
    {
        String bmlString = "<murmlface xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" " + "id=\"a1\" start=\"nod1:end\">"
                + "<murml-description><dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>" + "</murml:murmlface>";
        MURMLFaceBehaviour beh = new MURMLFaceBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);
        
        assertEquals(0, beh.getMurmlDescription().getDynamic().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }

    @Test
    public void testExtension() throws IOException
    {
        BMLInfo.addDescriptionExtension(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
        BMLInfo.supportedExtensions.add(MURMLFaceBehaviour.class);
        String murmlString = "<murmlface xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" " + "id=\"a1\">"
                + "<murml-description><dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>" + "</murml:murmlface>";
        String bmlString = "<faceLexeme xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"a1\" lexeme=\"BLINK\">"
                + "<description priority=\"1\" type=\"murmlface\">" + murmlString + "</description></faceLexeme>";
        FaceLexemeBehaviour f = new FaceLexemeBehaviour("bmla", new XMLTokenizer(bmlString));
        assertThat(f.descBehaviour, instanceOf(MURMLFaceBehaviour.class));
        MURMLFaceBehaviour beh = (MURMLFaceBehaviour) f.descBehaviour;
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);

        assertEquals(0, beh.getMurmlDescription().getDynamic().getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), FRAME_PRECISION);
    }
}
