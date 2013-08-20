package asap.faceengine.faceunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.Constraint;
import asap.faceengine.FacePlanner;
import asap.faceengine.StubFaceUnit;
import asap.faceengine.facebinding.FaceBinding;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.DefaultPlayer;
import asap.realizer.Player;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.PlannerTests;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit test cases for the FacePlanner
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, TimedFaceUnit.class })
public class FacePlannerTest
{
    private FacePlanner facePlanner;
    private FaceBinding mockFaceBinding = mock(FaceBinding.class);
    private FaceController mockFaceController = mock(FaceController.class);
    private EmotionConverter mockEmotionConverter = mock(EmotionConverter.class);
    private FACSConverter mockFACSConverter = mock(FACSConverter.class);
    private FaceUnit stubFaceUnit = new StubFaceUnit();
    private FaceLexemeBehaviour mockFaceBehaviour = mock(FaceLexemeBehaviour.class);

    private List<BMLWarningFeedback> beList;
    private Player facePlayer;
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();
    private PlannerTests<TimedFaceUnit> plannerTests;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);

    private FaceLexemeBehaviour createFaceLexemeBehaviour() throws IOException
    {
        return new FaceLexemeBehaviour(BMLID, new XMLTokenizer("<faceLexeme xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"f1\" lexeme=\"BLINK\" amount=\"0\"/>"));
    }

    @Before
    public void setup()
    {
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(fbManager, planManager));
        facePlanner = new FacePlanner(fbManager, mockFaceController, mockFACSConverter, mockEmotionConverter, mockFaceBinding, planManager);
        beList = new ArrayList<BMLWarningFeedback>();
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(beList).build());
        plannerTests = new PlannerTests<TimedFaceUnit>(facePlanner, bbPeg);        
        List<TimedFaceUnit> fmus = new ArrayList<TimedFaceUnit>();
        TimedFaceUnit tfu = new TimedFaceUnit(fbManager, bbPeg, BMLID, "f1", stubFaceUnit);
        fmus.add(tfu);
        when(
                mockFaceBinding.getFaceUnit((FeedbackManager) any(), (BMLBlockPeg) any(), (FaceLexemeBehaviour) any(),
                        eq(mockFaceController), eq(mockFACSConverter), eq(mockEmotionConverter))).thenReturn(fmus);
        
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
        when(mockTimedFaceUnit.getFaceUnit()).thenReturn(stubFaceUnit);
        when(mockTimedFaceUnit.getId()).thenReturn("fu1");
        when(mockTimedFaceUnit.getBMLId()).thenReturn("bml1");
        PowerMockito.doThrow(new TimedPlanUnitPlayException("failure!", mockTimedFaceUnit)).when(mockTimedFaceUnit).play(0);

        facePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, mockFaceBehaviour, sacs, mockTimedFaceUnit);
        facePlayer.play(0);
        assertEquals(1, beList.size());
    }
}
