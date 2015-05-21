/*******************************************************************************
 *******************************************************************************/
package asap.faceengine;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import saiba.bml.core.Behaviour;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.Constraint;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTFaceKeyframeBehaviour;
import asap.bml.ext.murml.MURMLFaceBehaviour;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.faceengine.faceunit.MURMLKeyframeMorphFU;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.DefaultPlayer;
import asap.realizer.Player;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
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
 * Unit testcases for the FacePlanner
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, TimedFaceUnit.class })
public class FacePlannerTest
{
    private PlannerTests<TimedFaceUnit> plannerTests;
    private FacePlanner facePlanner;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private FaceController mockFaceController = mock(FaceController.class);
    private final PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();
    private FaceBinding mockFaceBinding = mock(FaceBinding.class);
    private static final float PLAN_PRECISION = 0.00001f;
    private FaceUnit stubFaceUnit = new StubFaceUnit();
    private FaceLexemeBehaviour mockFaceBehaviour = mock(FaceLexemeBehaviour.class);
    private List<BMLWarningFeedback> beList = new ArrayList<BMLWarningFeedback>();
    private Player facePlayer;
    private final PegBoard pegBoard = new PegBoard();

    @Before
    public void setup()
    {
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(fbManager, planManager));
        facePlanner = new FacePlanner(fbManager, mockFaceController, null, null, mockFaceBinding, planManager, pegBoard);
        plannerTests = new PlannerTests<TimedFaceUnit>(facePlanner, bbPeg);
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(beList).build());
        TimedFaceUnit tmu = new TimedFaceUnit(fbManager, bbPeg, BMLID, "nod1", stubFaceUnit, pegBoard);
        final List<TimedFaceUnit> tmus = new ArrayList<TimedFaceUnit>();
        tmus.add(tmu);
        when(
                mockFaceBinding.getFaceUnit((FeedbackManager) any(), (BMLBlockPeg) any(), (Behaviour) any(), (FaceController) any(),
                        (FACSConverter) any(), (EmotionConverter) any(),eq(pegBoard))).thenReturn(tmus);
    }

    public FaceLexemeBehaviour createFaceBehaviour() throws IOException
    {
        return new FaceLexemeBehaviour(BMLID, new XMLTokenizer("<faceLexeme xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"face1\" lexeme=\"BLINK\"/>"));
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createFaceBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createFaceBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createFaceBehaviour());
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

    @Test
    public void testResolveMorphKeyframe() throws IOException, BehaviourPlanningException
    {
        String bmlString = "<facekeyframe xmlns=\""+BMLTBehaviour.BMLTNAMESPACE+"\" id=\"kf1\">"+
                           "<FaceInterpolator parts=\"morph1 morph2\">"+
                           "2 0.8 0.7\n"+
                           "3 0.6 0.5"+
                           "</FaceInterpolator>"+
                           "</facekeyframe>";
        BMLTFaceKeyframeBehaviour b =new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(bmlString));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        
        TimedFaceUnit tfu = facePlanner.resolveSynchs(bbPeg, b, sacs);
        assertNotNull(tfu);
        assertThat(tfu.getFaceUnit(), instanceOf(KeyframeMorphFU.class));
        assertEquals(1, tfu.getPreferedDuration(),PLAN_PRECISION);
    }
    
    @Test
    public void testResolveMURML() throws IOException, BehaviourPlanningException
    {
        String bmlString = "<murmlface xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" " + "id=\"a1\" start=\"nod1:end\">"
                + "<murml-description><dynamic><keyframing><phase>" + "<frame ftime=\"0\">"
                + "<posture>Humanoid (dB_Smile 3 70 0 0)</posture>" + "</frame>" + "<frame ftime=\"2\">"
                + "<posture>Humanoid (dB_Smile 3 80 0 0)</posture>" + "</frame>" + "</phase></keyframing></dynamic></murml-description>"
                + "</murml:murmlface>";
        MURMLFaceBehaviour b = new MURMLFaceBehaviour(BMLID, new XMLTokenizer(bmlString));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = TimePegUtil.createTimePeg(0);
        TimePeg endPeg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0, false));

        TimedFaceUnit tfu = facePlanner.resolveSynchs(bbPeg, b, sacs);
        assertNotNull(tfu);
        assertThat(tfu.getFaceUnit(), instanceOf(MURMLKeyframeMorphFU.class));
        assertEquals(0, tfu.getStartTime(), PLAN_PRECISION);
        assertEquals(2, tfu.getEndTime(), PLAN_PRECISION);
    }

    @Test
    public void testAdd() throws IOException, BehaviourPlanningException
    {
        FaceLexemeBehaviour beh = createFaceBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedFaceUnit tfu = facePlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = facePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, tfu);
        assertEquals(4, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("attackPeak", syncAndPegs.get(1).sync);
        assertEquals("relax", syncAndPegs.get(2).sync);
        assertEquals("end", syncAndPegs.get(3).sync);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(2).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(3).peg.getGlobalValue(), PLAN_PRECISION);
    }

    @Test
    public void testAddWithStartConstraint() throws IOException, BehaviourPlanningException
    {
        FaceLexemeBehaviour beh = createFaceBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", new TimePeg(BMLBlockPeg.GLOBALPEG), new Constraint(), 0));
        TimedFaceUnit tfu = facePlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = facePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, tfu);
        assertEquals(4, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("attackPeak", syncAndPegs.get(1).sync);
        assertEquals("relax", syncAndPegs.get(2).sync);
        assertEquals("end", syncAndPegs.get(3).sync);
        assertEquals(0, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(2).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(3).peg.getGlobalValue(), PLAN_PRECISION);
    }
}
