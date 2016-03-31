/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;
import hmi.util.Resources;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * Test cases for the SpeechBinding
 * @author welberge
 */
public class SpeechBindingTest
{
    private SpeechBinding speechBinding;
    AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private PegBoard pegBoard = new PegBoard();
    private VJoint human;
    
    @Before
    public void setup()
    {
        speechBinding = new SpeechBinding(new Resources(""));
        human = HanimBody.getLOA1HanimBody();        
    }
    
    @Test
    public void testReadXML() throws ParameterException, MUSetupException
    {
        String str = "<speechbinding>"+
                     "<VisimeSpec visime=\"0\">"+
                     "<parameterdefaults>"+
                         "<parameterdefault name=\"a\" value=\"0\"/>"+          
                     "</parameterdefaults>"+
                     "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/speech/speakjaw.xml\"/>"+
                     "</VisimeSpec>"+   
                     "<VisimeSpec visime=\"1\">"+
                     "<parameterdefaults>"+
                         "<parameterdefault name=\"a\" value=\"1\"/>"+          
                     "</parameterdefaults>"+
                     "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/speech/speakjaw.xml\"/>"+
                     "</VisimeSpec>"+  
                     "</speechbinding>";
        speechBinding.readXML(str);
        
        when(mockAniPlayer.getVNext()).thenReturn(human);
        
        TimedAnimationMotionUnit tmu = speechBinding.getMotionUnit(0, BMLBlockPeg.GLOBALPEG, "bml1", "speech1", mockAniPlayer, pegBoard);
        assertEquals("bml1",tmu.getBMLId());
        assertEquals("speech1",tmu.getId());
        assertEquals(0f,Float.parseFloat(tmu.getMotionUnit().getParameterValue("a")),0.001f);
        
        tmu = speechBinding.getMotionUnit(mockBmlFeedbackManager,1, BMLBlockPeg.GLOBALPEG, "bml1", "speech1", mockAniPlayer, pegBoard);
        assertEquals("bml1",tmu.getBMLId());
        assertEquals("speech1",tmu.getId());
        assertEquals(1f,Float.parseFloat(tmu.getMotionUnit().getParameterValue("a")),0.001f);
    }
}
