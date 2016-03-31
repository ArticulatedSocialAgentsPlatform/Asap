/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.lipsync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;

import com.google.common.collect.ImmutableList;

/**
 * Unit test cases for TimedAnimationUnitLipSynchProvider
 * @author Herwin
 * 
 */
public class TimedAnimationUnitLipSynchProviderTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PlanManager<TimedAnimationUnit> animationPlanManager = new PlanManager<>();
    private PegBoard pegBoard = new PegBoard();
    private TimedPlanUnit mockSpeechUnit = mock(TimedPlanUnit.class);
    private BMLBlockPeg bbPeg = BMLBlockPeg.GLOBALPEG;
    private static final double TIMING_PRECISION = 0.0001;
    private SpeechBehaviour speechBehavior;
    private SpeechBinding speechBinding;
    private TimedAnimationUnitLipSynchProvider provider;

    @Before
    public void setup() throws IOException
    {
        String str = "<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"s1\">" + "<text>Hello world</text></speech>";
        speechBehavior = new SpeechBehaviour("bml1", new XMLTokenizer(str));

        TimePeg startPeg = new TimePeg(bbPeg);
        startPeg.setLocalValue(0);
        TimePeg endPeg = new TimePeg(bbPeg);
        endPeg.setLocalValue(10);
        when(mockSpeechUnit.getTimePeg("start")).thenReturn(startPeg);
        when(mockSpeechUnit.getTimePeg("end")).thenReturn(endPeg);
        VJoint vNext = HanimBody.getLOA1HanimBody();
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);

        speechBinding = new SpeechBinding(new Resources(""));
        str = "<speechbinding>" + "<VisimeSpec visime=\"0\">" + "<parameterdefaults>" + "<parameterdefault name=\"a\" value=\"0\"/>"
                + "</parameterdefaults>"
                + "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/speech/speakjaw.xml\"/>" + "</VisimeSpec>"
                + "<VisimeSpec visime=\"1\">" + "<parameterdefaults>" + "<parameterdefault name=\"a\" value=\"1\"/>"
                + "</parameterdefaults>"
                + "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/speech/speakjaw.xml\"/>" + "</VisimeSpec>"
                + "</speechbinding>";
        speechBinding.readXML(str);
        provider = new TimedAnimationUnitLipSynchProvider(speechBinding, mockAnimationPlayer, animationPlanManager, pegBoard);

    }

    @Test
    public void test() throws IOException
    {
        TTSTiming mockTiming = mock(TTSTiming.class);
        when(mockTiming.getVisimes()).thenReturn(ImmutableList.of(new Visime(1, 5000, false), new Visime(2, 5000, false)));
        provider.addLipSyncMovement(bbPeg, speechBehavior, mockSpeechUnit,mockTiming);
        List<TimedAnimationUnit> animationUnits = animationPlanManager.getPlanUnits();
        assertEquals(0, animationUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(10, animationUnits.get(animationUnits.size() - 1).getEndTime(), TIMING_PRECISION);

        for (TimedAnimationUnit mu : animationUnits)
        {
            assertThat("FaceUnit with 0 duration found at index " + animationUnits.indexOf(mu) + "Face Unit list: " + animationUnits,
                    mu.getEndTime() - mu.getStartTime(), greaterThan(0d));
        }
        assertEquals("bml1", animationUnits.get(0).getBMLId());
        assertEquals("s1", animationUnits.get(0).getId());
    }

    @Test
    public void testCoArticulation() throws IOException
    {
        TTSTiming mockTiming = mock(TTSTiming.class);
        when(mockTiming.getVisimes()).thenReturn(ImmutableList.of(new Visime(1, 1000, false), new Visime(2, 500,
                false), new Visime(2, 750, false), new Visime(1, 250, false)));
        provider.addLipSyncMovement(bbPeg, speechBehavior, mockSpeechUnit, mockTiming);
        List<TimedAnimationUnit> animationUnits = animationPlanManager.getPlanUnits();
        
        assertEquals(0, animationUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(2.5, animationUnits.get(3).getEndTime(), TIMING_PRECISION);
        
        assertEquals(1.25, animationUnits.get(0).getEndTime(), TIMING_PRECISION);
        assertEquals(0.5, animationUnits.get(1).getStartTime(), TIMING_PRECISION);
        assertEquals(1.875, animationUnits.get(1).getEndTime(), TIMING_PRECISION);
        assertEquals(1.25, animationUnits.get(2).getStartTime(), TIMING_PRECISION);
        assertEquals(2.375, animationUnits.get(2).getEndTime(), TIMING_PRECISION);
        assertEquals(1.875, animationUnits.get(3).getStartTime(), TIMING_PRECISION);      
    }
}
