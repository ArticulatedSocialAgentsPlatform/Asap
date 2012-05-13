package asap.faceengine.faceunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.ListBMLExceptionListener;
import saiba.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.DefaultPlayer;
import hmi.elckerlyc.PlannerTests;
import hmi.elckerlyc.Player;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.faceengine.FacePlanner;
import asap.faceengine.facebinding.FaceBinding;

/**
 * Unit test cases for the FacePlanner
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class,TimedFaceUnit.class})
public class FacePlannerTest
{
    private FacePlanner facePlanner;
    private FaceBinding mockFaceBinding = mock(FaceBinding.class);
    private FaceController mockFaceController = mock(FaceController.class);
    private EmotionConverter mockEmotionConverter = mock(EmotionConverter.class);
    private FACSConverter mockFACSConverter = mock(FACSConverter.class);
    private FaceUnit mockFaceUnit = mock(FaceUnit.class);
    private FaceLexemeBehaviour mockFaceBehaviour = mock(FaceLexemeBehaviour.class);

    private List<BMLExceptionFeedback> beList;
    private Player facePlayer;
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();
    private PlannerTests<TimedFaceUnit> plannerTests;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);

    private FaceLexemeBehaviour createFaceLexemeBehaviour() throws IOException
    {
        //return new FaceLexemeBehaviour(BMLID, new XMLTokenizer("<faceLexeme id=\"f1\" type=\"FACS\" au=\"1\" amount=\"0\"/>"));
        return new FaceLexemeBehaviour(BMLID, new XMLTokenizer("<faceLexeme id=\"f1\" lexeme=\"BLINK\" amount=\"0\"/>"));
    }

    @Before
    public void setup()
    {
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(fbManager, planManager));
        facePlanner = new FacePlanner(fbManager, mockFaceController, mockFACSConverter, mockEmotionConverter, mockFaceBinding, planManager);
        beList = new ArrayList<BMLExceptionFeedback>();
        fbManager.addExceptionListener(new ListBMLExceptionListener(beList));
        plannerTests = new PlannerTests<TimedFaceUnit>(facePlanner, bbPeg);

        when(mockFaceUnit.hasValidParameters()).thenReturn(true);
        KeyPositionMocker.stubKeyPositions(mockFaceUnit, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        List<TimedFaceUnit> fmus = new ArrayList<TimedFaceUnit>();
        TimedFaceUnit tfu = new TimedFaceUnit(fbManager, bbPeg, BMLID, "f1", mockFaceUnit);
        fmus.add(tfu);
        when(mockFaceBinding.getFaceUnit((FeedbackManager) any(), (BMLBlockPeg) any(), (FaceLexemeBehaviour) any(), eq(mockFaceController),
                        eq(mockFACSConverter), eq(mockEmotionConverter))).thenReturn(fmus);
        when(mockFaceUnit.getPreferedDuration()).thenReturn(3.0);
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createFaceLexemeBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createFaceLexemeBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createFaceLexemeBehaviour());
    }

    @Test
    public void testException() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        List<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePegAndConstraint tp = new TimePegAndConstraint("start", TimePegUtil.createTimePeg(0), new Constraint(), 0, false);
        sacs.add(tp);
        TimedFaceUnit mockTimedFaceUnit = PowerMockito.mock(TimedFaceUnit.class);
        when(mockTimedFaceUnit.getStartTime()).thenReturn(0d);
        when(mockTimedFaceUnit.getEndTime()).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedFaceUnit.isLurking()).thenReturn(false);
        when(mockTimedFaceUnit.isPlaying()).thenReturn(true);
        when(mockTimedFaceUnit.getState()).thenReturn(TimedPlanUnitState.IN_EXEC);
        when(mockTimedFaceUnit.getFaceUnit()).thenReturn(mockFaceUnit);
        when(mockTimedFaceUnit.getId()).thenReturn("fu1");
        when(mockTimedFaceUnit.getBMLId()).thenReturn("bml1");
        PowerMockito.doThrow(new TimedPlanUnitPlayException("failure!", mockTimedFaceUnit)).when(mockTimedFaceUnit).play(0);
       

        facePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, mockFaceBehaviour, sacs, mockTimedFaceUnit);
        facePlayer.play(0);
        assertEquals(1, beList.size());
    }
}
