/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.lipsync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.faceanimation.FaceController;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.VisemeBinding;
import asap.faceengine.viseme.VisemeToMorphMapping;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;

import com.google.common.collect.ImmutableList;

/**
 * Unit test cases for TimedFaceUnitLipSyncProvider
 * @author Herwin
 * 
 */
public class TimedFaceUnitLipSynchProviderTest
{
    private PlanManager<TimedFaceUnit> faceManager = new PlanManager<TimedFaceUnit>();
    private VisemeBinding visemeBinding;
    private FaceController mockFaceController = mock(FaceController.class);
    private TimedPlanUnit mockSpeechUnit = mock(TimedPlanUnit.class);
    private SpeechBehaviour speechBehavior;
    private BMLBlockPeg bbPeg = BMLBlockPeg.GLOBALPEG;
    private static final double TIMING_PRECISION = 0.0001;
    private TimedFaceUnitLipSynchProvider provider;
    private final PegBoard pegBoard = new PegBoard();
    private TTSTiming mockTiming = mock(TTSTiming.class);

    @Before
    public void before() throws IOException
    {
        String visemeBindingXML = "<VisemeToMorphMapping>" + "<Mapping viseme=\"0\" target=\"\"/>"
                + "<Mapping viseme=\"1\" target=\"morph1\"/>" + "<Mapping viseme=\"2\" target=\"morph2\"/>" + "</VisemeToMorphMapping>";
        VisemeToMorphMapping mapping = new VisemeToMorphMapping();
        mapping.readXML(visemeBindingXML);
        visemeBinding = new MorphVisemeBinding(mapping);
        String str = "<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"s1\"><text>Hello world</text></speech>";
        speechBehavior = new SpeechBehaviour("bml1", new XMLTokenizer(str));

        TimePeg startPeg = new TimePeg(bbPeg);
        startPeg.setLocalValue(0);
        TimePeg endPeg = new TimePeg(bbPeg);
        endPeg.setLocalValue(10);
        when(mockSpeechUnit.getTimePeg("start")).thenReturn(startPeg);
        when(mockSpeechUnit.getTimePeg("end")).thenReturn(endPeg);
        provider = new TimedFaceUnitLipSynchProvider(visemeBinding, mockFaceController, faceManager, pegBoard);
    }

    @Test
    public void test()
    {
        when(mockTiming.getVisimes()).thenReturn(ImmutableList.of(new Visime(1, 5000, false), new Visime(2, 5000, false)));
        provider.addLipSyncMovement(bbPeg, speechBehavior, mockSpeechUnit, mockTiming);
        List<TimedFaceUnit> faceUnits = faceManager.getPlanUnits();
        assertEquals(0, faceUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(10, faceUnits.get(faceUnits.size() - 1).getEndTime(), TIMING_PRECISION);

        for (TimedFaceUnit fu : faceUnits)
        {
            assertThat("FaceUnit with 0 duration found at index " + faceUnits.indexOf(fu) + "Face Unit list: " + faceUnits, fu.getEndTime()
                    - fu.getStartTime(), greaterThan(0d));
        }
    }

    @Test
    public void testCoArticulation()
    {
        when(mockTiming.getVisimes()).thenReturn(
                ImmutableList.of(new Visime(1, 1000, false), new Visime(2, 500, false), new Visime(2, 750, false),
                        new Visime(1, 250, false)));
        provider.addLipSyncMovement(bbPeg, speechBehavior, mockSpeechUnit, mockTiming);
        List<TimedFaceUnit> faceUnits = faceManager.getPlanUnits();
        assertEquals(0, faceUnits.get(0).getStartTime(), TIMING_PRECISION);
        assertEquals(2.5, faceUnits.get(3).getEndTime(), TIMING_PRECISION);

        assertEquals(1.25, faceUnits.get(0).getEndTime(), TIMING_PRECISION);
        assertEquals(0.5, faceUnits.get(1).getStartTime(), TIMING_PRECISION);
        assertEquals(1.875, faceUnits.get(1).getEndTime(), TIMING_PRECISION);
        assertEquals(1.25, faceUnits.get(2).getStartTime(), TIMING_PRECISION);
        assertEquals(2.375, faceUnits.get(2).getEndTime(), TIMING_PRECISION);
        assertEquals(1.875, faceUnits.get(3).getStartTime(), TIMING_PRECISION);
    }
}
