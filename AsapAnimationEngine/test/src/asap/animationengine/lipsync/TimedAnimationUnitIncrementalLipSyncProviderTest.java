/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.lipsync;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;
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

/**
 * Unit tests for the TimedAnimationUnitIncrementalLipSyncProvider
 * @author hvanwelbergen
 */
public class TimedAnimationUnitIncrementalLipSyncProviderTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PlanManager<TimedAnimationUnit> animationPlanManager = new PlanManager<>();
    private PegBoard pegBoard = new PegBoard();
    private TimedPlanUnit mockSpeechUnit = mock(TimedPlanUnit.class);
    private BMLBlockPeg bbPeg = BMLBlockPeg.GLOBALPEG;
    private static final double TIMING_PRECISION = 0.0001;
    private SpeechBehaviour speechBehavior;
    private SpeechBinding speechBinding;
    private TimedAnimationUnitIncrementalLipSynchProvider provider;
    
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
        provider = new TimedAnimationUnitIncrementalLipSynchProvider(speechBinding, mockAnimationPlayer, animationPlanManager, pegBoard);
    }
    
    @Test
    public void test() throws IOException
    {
        provider.setLipSyncUnit(bbPeg, speechBehavior, 0, new Visime(1,5000,false), new Object());
        
        List<TimedAnimationUnit> animationUnits = animationPlanManager.getPlanUnits();
        assertEquals(0, animationUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(5, animationUnits.get(animationUnits.size() - 1).getEndTime(), TIMING_PRECISION);
        assertEquals("bml1", animationUnits.get(0).getBMLId());
        assertEquals("s1", animationUnits.get(0).getId());
    }
    
    @Test
    public void testReplace() throws IOException
    {
        Object identifier = new Object();
        provider.setLipSyncUnit(bbPeg, speechBehavior, 0, new Visime(1,5000,false), identifier);
        provider.setLipSyncUnit(bbPeg, speechBehavior, 0, new Visime(1,4000,false), identifier);
        
        List<TimedAnimationUnit> animationUnits = animationPlanManager.getPlanUnits();
        assertEquals(0, animationUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(4, animationUnits.get(animationUnits.size() - 1).getEndTime(), TIMING_PRECISION);
        assertEquals("bml1", animationUnits.get(0).getBMLId());
        assertEquals("s1", animationUnits.get(0).getId());
    }
    
    @Test
    public void testCoArticulation() throws IOException
    {
        provider.setLipSyncUnit(bbPeg, speechBehavior, 0, new Visime(1,1000,false), new Object());
        provider.setLipSyncUnit(bbPeg, speechBehavior, 1, new Visime(2,500,false), new Object());
        provider.setLipSyncUnit(bbPeg, speechBehavior, 1.5, new Visime(2,750,false), new Object());
        provider.setLipSyncUnit(bbPeg, speechBehavior, 2.25, new Visime(1,250,false), new Object());
        
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
