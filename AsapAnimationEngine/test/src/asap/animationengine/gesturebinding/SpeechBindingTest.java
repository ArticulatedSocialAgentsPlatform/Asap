package asap.animationengine.gesturebinding;

import static org.junit.Assert.*;
import hmi.animation.VJoint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.testutil.animation.HanimBody;
import hmi.util.Resources;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedMotionUnit;

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
        
        TimedMotionUnit tmu = speechBinding.getMotionUnit(0, BMLBlockPeg.GLOBALPEG, "bml1", "speech1", mockAniPlayer, pegBoard);
        assertEquals("bml1",tmu.getBMLId());
        assertEquals("speech1",tmu.getId());
        assertEquals(0f,Float.parseFloat(tmu.getMotionUnit().getParameterValue("a")),0.001f);
        
        tmu = speechBinding.getMotionUnit(mockBmlFeedbackManager,1, BMLBlockPeg.GLOBALPEG, "bml1", "speech1", mockAniPlayer, pegBoard);
        assertEquals("bml1",tmu.getBMLId());
        assertEquals("speech1",tmu.getId());
        assertEquals(1f,Float.parseFloat(tmu.getMotionUnit().getParameterValue("a")),0.001f);
    }
}
