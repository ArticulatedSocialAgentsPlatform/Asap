/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeShiftBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import saiba.bml.parser.Constraint;
import asap.animationengine.gaze.GazeShiftTMU;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.gaze.StubGazeMU;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.gesturebinding.MURMLMUBuilder;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.StubAnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.PostureShiftTMU;
import asap.animationengine.restpose.RestPose;
import asap.bml.ext.bmlt.BMLTKeyframeBehaviour;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.hns.Hns;
import asap.hns.ShapeSymbols;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Test cases for the AnimationPlanner
 * @author welberge
 * 
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class AnimationPlannerTest
{
    private AnimationPlayer mockPlayer = mock(AnimationPlayer.class);
    private GestureBinding mockBinding = mock(GestureBinding.class);
    private RestPose mockRestPose = mock(RestPose.class);
    private RestGaze mockRestGaze = mock(RestGaze.class);

    private StubAnimationUnit stubUnit = new StubAnimationUnit();
    private StubGazeMU stubGazeUnit = new StubGazeMU();

    private Hns stubHns = new StubHns();
    private PegBoard pegBoard = new PegBoard();

    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    protected FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private AnimationPlanner animationPlanner;
    private PlannerTests<TimedAnimationUnit> plannerTests;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private final PlanManager<TimedAnimationUnit> planManager = new PlanManager<>();
    private static final double TIMING_PRECISION = 0.0001;

    private static final class StubHns extends Hns
    {
        @Override
        public boolean getHandLocation(String location, float pos[])
        {
            Vec3f.set(pos, 1, 1, 1);
            return true;
        }

        @Override
        public boolean getAbsoluteDirection(String dir, float dirVec[])
        {
            if (dir.startsWith("Palm")) return false;
            if (dir.startsWith("Dir")) return true;
            return true;
        }

        @Override
        public boolean isPalmOrientation(String orientation)
        {
            if (orientation.startsWith("Palm")) return true;
            return false;
        }

        @Override
        public double getPalmOrientation(String value, String scope)
        {
            return 0;
        }

        @Override
        public ShapeSymbols getElementShape(String shape)
        {
            return ShapeSymbols.LeftC;
        }
    }

    @Before
    public void setup() throws MUSetupException
    {
        animationPlanner = new AnimationPlanner(fbManager, mockPlayer, mockBinding, stubHns, null, planManager, pegBoard);
        plannerTests = new PlannerTests<TimedAnimationUnit>(animationPlanner, bbPeg);
        PostureShiftTMU tmups = new PostureShiftTMU(fbManager, bbPeg, BMLID, "pshift1", stubUnit, pegBoard, mockRestPose, mockPlayer);
        GazeShiftTMU tmugs = new GazeShiftTMU(fbManager, bbPeg, BMLID, "gshift1", stubGazeUnit, pegBoard, mockRestGaze, mockPlayer);
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbManager, bbPeg, BMLID, "nod1", stubUnit, pegBoard, mockPlayer);

        final List<TimedAnimationUnit> tmus = new ArrayList<>();
        tmus.add(tmu);
        when(mockBinding.getRestPose(any(PostureShiftBehaviour.class), eq(mockPlayer))).thenReturn(mockRestPose);
        when(mockBinding.getRestGaze(any(GazeShiftBehaviour.class), eq(mockPlayer))).thenReturn(mockRestGaze);
        when(mockBinding.getMotionUnit((BMLBlockPeg) any(), (Behaviour) any(), eq(mockPlayer), eq(pegBoard))).thenReturn(tmus);
        when(mockBinding.getMotionUnit((BMLBlockPeg) any(), (Behaviour) any(), eq(mockPlayer), eq(pegBoard), (MURMLMUBuilder) any()))
                .thenReturn(tmus);
        when(mockRestPose.copy(eq(mockPlayer))).thenReturn(mockRestPose);
        when(mockRestPose.createPostureShiftTMU(eq(fbManager), (BMLBlockPeg) any(), eq(BMLID), (String) any(), eq(pegBoard))).thenReturn(
                tmups);
        when(mockRestGaze.createGazeShiftTMU(eq(fbManager), (BMLBlockPeg) any(), eq(BMLID), (String) any(), eq(pegBoard)))
                .thenReturn(tmugs);

        when(mockPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockPlayer.getVCurrPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        when(mockPlayer.getVNextPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
    }

    public HeadBehaviour createHeadBehaviour() throws IOException
    {
        return new HeadBehaviour(BMLID, new XMLTokenizer(
                "<head xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"nod1\" lexeme=\"NOD\"/>"));
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createHeadBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createHeadBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createHeadBehaviour());
    }

    @Test
    public void testAddedToPlanManager() throws IOException, BehaviourPlanningException
    {
        HeadBehaviour beh = new HeadBehaviour(BMLID, new XMLTokenizer("<head xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"nod1\" lexeme=\"NOD\"/>"));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));
        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertThat(planManager.getBehaviours(BMLID), IsIterableContainingInOrder.contains("nod1"));
        assertEquals(7, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("ready", syncAndPegs.get(1).sync);
        assertEquals("strokeStart", syncAndPegs.get(2).sync);
        assertEquals("stroke", syncAndPegs.get(3).sync);
        assertEquals("strokeEnd", syncAndPegs.get(4).sync);
        assertEquals("relax", syncAndPegs.get(5).sync);
        assertEquals("end", syncAndPegs.get(6).sync);
        assertEquals(0.3, syncAndPegs.get(0).peg.getGlobalValue(), TIMING_PRECISION);
        for (int i = 1; i < 7; i++)
        {
            assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(i).peg.getGlobalValue(), TIMING_PRECISION);
        }
    }

    @Test
    public void testUnknownStart() throws BehaviourPlanningException, IOException
    {
        HeadBehaviour beh = new HeadBehaviour(BMLID, new XMLTokenizer("<head xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"nod1\" lexeme=\"NOD\"/>"));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);
        List<SyncAndTimePeg> syncAndPegs = animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, pu.getEndTime(), TIMING_PRECISION);
        assertEquals(7, syncAndPegs.size());
    }

    @Test
    public void testPostureShiftBehaviour() throws BehaviourPlanningException, IOException
    {
        String str = "<postureShift xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"shift1\"></postureShift>";
        PostureShiftBehaviour beh = new PostureShiftBehaviour(BMLID, new XMLTokenizer(str));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu, instanceOf(PostureShiftTMU.class));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, pu.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testGazeShiftBehaviour() throws BehaviourPlanningException, IOException
    {
        String str = "<gazeShift xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"shift1\"/>";
        GazeShiftBehaviour beh = new GazeShiftBehaviour(BMLID, new XMLTokenizer(str));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu, instanceOf(GazeShiftTMU.class));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, pu.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testGazeShiftBehaviourEndTimed() throws BehaviourPlanningException, IOException
    {
        String str = "<gazeShift xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"shift1\"/>";
        GazeShiftBehaviour beh = new GazeShiftBehaviour(BMLID, new XMLTokenizer(str));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        TimePeg ep = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("end", ep, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu, instanceOf(GazeShiftTMU.class));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(0.3 + stubGazeUnit.getPreferedReadyDuration(), pu.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testMurmlPalmOrient() throws IOException, BehaviourPlanningException
    {
        //@formatter:off
        String str = 
        "<murmlgesture id=\"gesture1\" xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "<murml-description>"+
        "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
          "<dynamicElement>"+
            "<value type=\"start\" name=\"DirLU\"/>"+
            "<value type=\"end\" name=\"DirU\"/>"+
          "</dynamicElement>"+
        "</dynamic>"+
        "</murml-description>"+
        "</murmlgesture>";
        //@formatter:on
        MURMLGestureBehaviour beh = new MURMLGestureBehaviour(BMLID, new XMLTokenizer(str));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);

        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
    }

    @Test
    public void testMurmlRelativePalmOrient() throws IOException, BehaviourPlanningException
    {
        //@formatter:off
        String str =
        "<murmlgesture id=\"gesture1\" xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "  <murml-description>"+
        "  <dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
        "    <dynamicElement>"+
        "      <value type=\"start\" name=\"PalmLU\"/>"+
        "      <value type=\"end\" name=\"PalmU\"/>"+
        "    </dynamicElement>"+
        "  </dynamic>"+
        "  </murml-description>"+
        "</murmlgesture>";
        //@formatter:on

        MURMLGestureBehaviour beh = new MURMLGestureBehaviour(BMLID, new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);

        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertTrue(pu.hasValidTiming());
    }

    @Test
    public void testMurmlHandLocation() throws IOException, BehaviourPlanningException
    {
        //@formatter:off
        String str =
        "<murmlgesture id=\"gesture1\" xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "  <murml-description>"+
        "  <dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
        "    <dynamicElement type=\"curve\">"+
        "      <value type=\"start\"  name=\"LocShoulder LocCenterLeft LocFar\"/>"+
        "      <value type=\"end\"    name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
        "      <value type=\"normal\" name=\"DirU\"/>"+
        "      <value type=\"shape\"  name=\"LeftC\"/>"+
        "      <value type=\"extension\" name=\"0.6\"/>"+
        "    </dynamicElement>"+
        "  </dynamic>"+
        "  </murml-description>"+
        "</murmlgesture>";
        //@formatter:on

        MURMLGestureBehaviour beh = new MURMLGestureBehaviour(BMLID, new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertThat(pu.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_elbow, Hanim.r_shoulder));
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);

        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertTrue(pu.hasValidTiming());
    }

    @Test
    public void testMURMLHandLocationSynchedStrokeStart() throws IOException, BehaviourPlanningException
    {
        //@formatter:off
        String str =
        "<murmlgesture  id=\"gesture_1\" xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "<murml-description>"+
            "<static slot=\"HandLocation\" value=\"LocShoulder LocCenterRight LocNorm\" />"+
        "</murml-description>"+
        "</murmlgesture>";
        //@formatter:on
        MURMLGestureBehaviour beh = new MURMLGestureBehaviour(BMLID, new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(4), new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(4, pu.getTime("strokeStart"), TIMING_PRECISION);
        assertThat(pu.getTime("strokeStart"), greaterThan(pu.getTime("start")));

        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(4, pu.getTime("strokeStart"), TIMING_PRECISION);
        assertThat(pu.getTime("strokeStart"), greaterThan(pu.getTime("start")));
        assertEquals(pu.getTime("strokeEnd"), pu.getTime("strokeStart"), TIMING_PRECISION);
    }

    @Test
    public void testMURMLHandLocationSynchedStrokeStartAndEnd() throws IOException, BehaviourPlanningException
    {
        //@formatter:off
        String str =
        "<murmlgesture  id=\"gesture_1\" xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "<murml-description>"+
            "<static slot=\"HandLocation\" value=\"LocShoulder LocCenterRight LocNorm\" />"+
        "</murml-description>"+
        "</murmlgesture>";
        //@formatter:on
        MURMLGestureBehaviour beh = new MURMLGestureBehaviour(BMLID, new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(4), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(6), new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(4, pu.getTime("strokeStart"), TIMING_PRECISION);
        assertThat(pu.getTime("strokeStart"), greaterThan(pu.getTime("start")));

        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(4, pu.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(6, pu.getTime("strokeEnd"), TIMING_PRECISION);
        assertThat(pu.getTime("strokeStart"), greaterThan(pu.getTime("start")));
    }

    @Test
    public void testKeyframeInternal() throws IOException, BehaviourPlanningException
    {
        String keyframe = "<SkeletonInterpolator xmlns=\"\" rotationEncoding=\"quaternions\" parts=\"skullbase\" encoding=\"R\">" + "0 0 1 0 1"
                + "</SkeletonInterpolator>";
        String str = "<bmlt:keyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + "id=\"kf1\">" + keyframe + "</bmlt:keyframe>";
        BMLTKeyframeBehaviour beh = new BMLTKeyframeBehaviour("bml1", new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedAnimationMotionUnit pu = (TimedAnimationMotionUnit)animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertThat(pu.getMotionUnit(), instanceOf(KeyframeMU.class));
        assertThat(pu.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.skullbase));
    }

    @Test
    public void testKeyframeInternalMirror() throws IOException, BehaviourPlanningException
    {
        String keyframe = "<SkeletonInterpolator xmlns=\"\" rotationEncoding=\"quaternions\" parts=\"r_shoulder\" encoding=\"R\">" + "0 0 1 0 1"
                + "</SkeletonInterpolator>";
        String str = "<bmlt:keyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + "id=\"kf1\">" + keyframe + 
                "<bmlt:parameter name=\"mirror\" value=\"true\"/></bmlt:keyframe>";
        BMLTKeyframeBehaviour beh = new BMLTKeyframeBehaviour("bml1", new XMLTokenizer(str));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedAnimationMotionUnit pu = (TimedAnimationMotionUnit)animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertThat(pu.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder));
    }
    
    @Test
    public void testInterrupt() // throws BehaviourPlanningException
    {
        /*
         * TODO: break up the animationplayer for better testability
         * AnimationPlanner ap = new AnimationPlanner(new AnimationPlayer(),mockBinding);
         * final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1",0.3);
         * HeadBehaviour beh = new HeadBehaviour();
         * beh.readXML("<head id=\"nod1\" action=\"ROTATION\" rotation=\"NOD\"/>");
         * beh.bmlId = "bml1";
         * final HeadBehaviour behF = beh;
         * TimedMotionUnit tmu = new TimedMotionUnit(bbPeg, "nod1", "bml1", mockUnit);
         * 
         * ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
         * TimePeg sp = new TimePeg(bbPeg);
         * sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));
         * 
         * final KeyPosition start = new KeyPosition("start",0,1);
         * final KeyPosition end = new KeyPosition("end",1,1);
         * final List<KeyPosition> keyPos = new ArrayList<KeyPosition>();
         * keyPos.add(start);
         * keyPos.add(end);
         * final List<TimedMotionUnit>tmus = new ArrayList<TimedMotionUnit>();
         * tmus.add(tmu);
         * new NonStrictExpectations()
         * {
         * {
         * mockBinding.getMotionUnit(bbPeg, behF, mockPlayer);returns(tmus);
         * mockUnit.getPreferedDuration();returns(3.0);
         * mockUnit.getKeyPositions();returns(keyPos);
         * mockUnit.getKeyPosition("start");returns(start);
         * mockUnit.getKeyPosition("end");returns(end);
         * }
         * };
         * 
         * PlanUnit pu = ap.resolveSynchs(bbPeg, beh, sacs);
         * assertEquals(0.3,sp.getGlobalValue(),0.0001);
         * ap.addBehaviour(bbPeg, beh, sacs, pu);
         * 
         * ap.interruptBehaviourBlock("nod1", 2);
         * assertTrue(0,getNumberOfTimedMotionUnits());
         */
    }
}
