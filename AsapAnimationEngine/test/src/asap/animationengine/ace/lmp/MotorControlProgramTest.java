package asap.animationengine.ace.lmp;

import static asap.testutil.bml.feedback.FeedbackAsserts.assertEqualSyncPointProgress;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.parser.Constraint;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.OrientConstraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the MotorControlProgram
 * @author hvanwelbergen
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class MotorControlProgramTest extends AbstractTimedPlanUnitTest
{
    private PegBoard globalPegBoard = new PegBoard();
    private PegBoard localPegboard = new PegBoard();

    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);

    private static double TIME_PRECISION = 0.001;
    private static final double LMP_PREPDUR = 0.4;
    private static final double LMP_STROKEDUR = 1;
    private static final double LMP_RETRACTIONDUR = 0.3;

    private BMLBlockPeg bml1Peg = new BMLBlockPeg("bml1", 0);
    private StubLMP stubTimedAnimationUnit = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal", localPegboard, new HashSet<String>(),
            new HashSet<String>(), LMP_PREPDUR, LMP_RETRACTIONDUR, LMP_STROKEDUR);

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        globalPegBoard.addBMLBlockPeg(bml1Peg);

        when(
                mockAnimationPlayer.createTransitionToRest(any(FeedbackManager.class), any(Set.class), any(TimePeg.class),
                        any(TimePeg.class), anyString(), anyString(), any(BMLBlockPeg.class), any(PegBoard.class))).thenReturn(
                stubTimedAnimationUnit);
    }
    
    private StubLMP createStub(String bmlId, String id, double prepDur, double retrDur, double strokeDur)
    {
        return new StubLMP(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, bmlId, id, localPegboard, 
                new HashSet<String>(), new HashSet<String>(), prepDur, retrDur, strokeDur);
    }

    private LMP createStub(String bmlId, String id, double prepDur, double retrDur, double strokeDur, boolean hasFixedStrokeDur)
    {
        return new StubLMP(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, bmlId, id, localPegboard, new HashSet<String>(),
                new HashSet<String>(), prepDur, retrDur, strokeDur, hasFixedStrokeDur);
    }

    private MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new MotorControlProgram(bfm, bbPeg, bmlId, id, globalPegBoard, localPegboard, mockAnimationPlayer, stubTimedAnimationUnit);
    }

    private MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, LMP lmp)
    {
        return new MotorControlProgram(bfm, bbPeg, bmlId, id, globalPegBoard, localPegboard, mockAnimationPlayer, lmp);
    }

    private MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime, LMP lmp)
    {
        MotorControlProgram mcp = setupPlanUnit(bfm, bbPeg, bmlId, id, lmp);
        mcp.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        try
        {
            mcp.resolveSynchs(bml1Peg);
        }
        catch (BehaviourPlanningException e)
        {
            throw new RuntimeException(e);
        }
        return mcp;
    }

    @Override
    protected MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        return setupPlanUnit(bfm, bbPeg, id, bmlId, startTime, stubTimedAnimationUnit);
    }

    @Test
    public void testUpdateTimingShorterPreparation() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, bml1Peg, "bml1", "beh1");
        mcp.setState(TimedPlanUnitState.LURKING);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createAbsoluteTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        final double LMP_PREPDUR_NEW = 0.3;
        stubTimedAnimationUnit.setPrepDuration(LMP_PREPDUR_NEW);
        mcp.updateTiming(0);

        assertEquals(2 - LMP_PREPDUR_NEW, mcp.getTime("start"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(2 + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(2 + LMP_STROKEDUR, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(2 + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getTime("end"), TIME_PRECISION);
    }

    @Test
    public void testStrokeFeedback() throws TimedPlanUnitPlayException, BehaviourPlanningException
    {
        MotorControlProgram mcp = (MotorControlProgram) setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0);
        mcp.setState(TimedPlanUnitState.LURKING);
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), new ArrayList<TimePegAndConstraint>());
        mcp.start(0);
        mcp.play(LMP_PREPDUR + 0.1);
        assertEquals("Received feedback: " + fbList, 4, fbList.size());
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 0, 0), fbList.get(0));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "ready", LMP_PREPDUR + 0.1, LMP_PREPDUR + 0.1),
                fbList.get(1));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "strokeStart", LMP_PREPDUR + 0.1, LMP_PREPDUR + 0.1),
                fbList.get(2));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", LMP_PREPDUR + 0.1, LMP_PREPDUR + 0.1),
                fbList.get(3));
    }

    @Test
    public void testEndFeedback() throws TimedPlanUnitPlayException
    {
        MotorControlProgram mcp = (MotorControlProgram) setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0);
        mcp.setState(TimedPlanUnitState.LURKING);
        mcp.updateTiming(0);
        mcp.start(0);
        mcp.stop(5);
        assertEquals(7, fbList.size());
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 0, 0), fbList.get(0));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "ready", 5, 5), fbList.get(1));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "strokeStart", 5, 5), fbList.get(2));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", 5, 5), fbList.get(3));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "strokeEnd", 5, 5), fbList.get(4));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "relax", 5, 5), fbList.get(5));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "end", 5, 5), fbList.get(6));
    }

    @Test
    public void testResolveSyncNoConstraints() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(LMP_PREPDUR, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(LMP_PREPDUR, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP_PREPDUR, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getTime("end"), TIME_PRECISION);
    }

    @Test
    public void testResolveSyncStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getEndTime(), TIME_PRECISION);
    }

    @Test
    public void testResolveSyncStartConstraintLMPWristRot() throws BehaviourPlanningException
    {

        List<OrientConstraint> ocVec = new ArrayList<>();
        ocVec.add(new OrientConstraint("strokeStart"));
        ocVec.add(new OrientConstraint("strokeEnd"));
        LMPWristRot lmpWristRot = new LMPWristRot("left_arm", ocVec, fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard,
                mockAnimationPlayer);
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmpWristRot);

        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION,
                mcp.getEndTime(), TIME_PRECISION);
    }

    @Test
    public void testResolveSyncStrokeConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("stroke", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(3 - LMP_PREPDUR, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(3, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(3 + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(3 + LMP_STROKEDUR, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(3 + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getEndTime(), TIME_PRECISION);
    }

    @Test
    public void testResolveSyncStrokeEndAndStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2 - LMP_PREPDUR, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(3 + LMP_RETRACTIONDUR, mcp.getEndTime(), TIME_PRECISION);
    }

    @Test
    public void testResolveSyncStrokeStartStarAndEndConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(10), new Constraint(), 2, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2 - LMP_PREPDUR, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(3, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(10 - 2, mcp.getEndTime(), TIME_PRECISION);
    }

    @Test
    public void testResolveSequenceNoConstraints() throws BehaviourPlanningException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);

        LMPSequence lmp = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1).add(stubTimedAnimationUnit2).build());
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmp);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        assertEquals(0, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIME_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2.getTime("strokeEnd"), TIME_PRECISION);
    }

    @Test
    public void testResolveSequenceInParallelNoConstraints() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1a = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1a", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit1b = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1b", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2a = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2a", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);
        StubLMP stubTimedAnimationUnit2b = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2b", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);
        LMPSequence lmpS1 = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1a).add(stubTimedAnimationUnit2a).build());
        LMPSequence lmpS2 = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1b).add(stubTimedAnimationUnit2b).build());
        LMPParallel lmp = new LMPParallel(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(lmpS1).add(lmpS2).build());

        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmp);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        assertEquals(0, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("relax"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIME_PRECISION);

        assertEquals(0, lmpS1.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, lmpS1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP, lmpS1.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, lmpS1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, lmpS2.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, lmpS2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP, lmpS2.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, lmpS2.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1a.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1a.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2a.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2a.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1b.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1b.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2b.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2b.getTime("strokeEnd"), TIME_PRECISION);
    }

    @Test
    public void testResolveSequenceInParallelAndHandmoveNoConstraints() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 3, 3, true);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2, false);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3, false);

        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", par);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        assertEquals(0, mcp.getStartTime(), TIME_PRECISION);

        assertEquals(0, par.getStartTime(), TIME_PRECISION);
        assertEquals(2, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, par.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(1, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(2, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, seq.getTime("start"), TIME_PRECISION);
        assertEquals(2, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, seq.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(2, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(3, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(3.5, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("strokeEnd"), TIME_PRECISION);
    }

    @Test
    public void testResolveSequenceInParallelAndHandmoveNoConstraintsUpdateTiming() throws BehaviourPlanningException,
            TimedPlanUnitPlayException
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 3, 3, true);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2, false);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3, false);

        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", par);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.updateTiming(0);

        assertEquals(0, mcp.getStartTime(), TIME_PRECISION);

        assertEquals(0, par.getStartTime(), TIME_PRECISION);
        assertEquals(2, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, par.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(1, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(2, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, seq.getTime("start"), TIME_PRECISION);
        assertEquals(2, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, seq.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(0, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(2, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(3, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(3.5, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("strokeEnd"), TIME_PRECISION);
    }

    @Test
    public void testSequenceDynamicTimingUpdate() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2", localPegboard, 
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);

        LMPSequence lmp = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1).add(stubTimedAnimationUnit2).build());
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmp);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.setState(TimedPlanUnitState.LURKING);
        mcp.start(0);
        mcp.play(0);
        final double LMP2_PREP_NEW = 3;
        stubTimedAnimationUnit2.setPrepDuration(LMP2_PREP_NEW);
        mcp.updateTiming(0.1);

        assertEquals(0, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, mcp.getTime("strokeEnd"), TIME_PRECISION);
        // assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, mcp.getTime("relax"), TIMING_PRECISION);
        // assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIMING_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2.getStartTime(), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW, stubTimedAnimationUnit2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, stubTimedAnimationUnit2.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testUpdateTimingSeq() throws TimedPlanUnitPlayException, BehaviourPlanningException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());

        
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", seq);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createAbsoluteTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.setState(TimedPlanUnitState.LURKING);
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        mcp.updateTiming(0);

        assertEquals(1, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, mcp.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(1, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, seq.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(1, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(9, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(11, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(14, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testSeqUpdateTimingWhileRunningInPrep() throws TimedPlanUnitPlayException, BehaviourPlanningException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", seq);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createAbsoluteTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.setState(TimedPlanUnitState.LURKING);        
        mcp.start(2);
        mcp.play(2);
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        mcp.updateTiming(2);

        assertEquals(2, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, mcp.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, seq.getTime("strokeEnd"), TIME_PRECISION);        

        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(9, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(11, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(14, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testSeqUpdateTimingWhileRunningInStroke() throws TimedPlanUnitPlayException, BehaviourPlanningException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        
        
        
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", seq);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createAbsoluteTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.setState(TimedPlanUnitState.LURKING);        
        mcp.start(2);
        mcp.play(2);
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        mcp.play(6.9);
        mcp.updateTiming(7);

        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(15, seq.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(8, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(10, tmu2.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(10, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(13, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(15, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testSeqUpdateStrokeTimingWhileRunningInStroke() throws TimedPlanUnitPlayException, BehaviourPlanningException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", seq);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createAbsoluteTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        mcp.setState(TimedPlanUnitState.LURKING);        
        mcp.start(2);
        mcp.play(2);

        tmu1.setStrokeDuration(4);
        tmu2.setStrokeDuration(3);
        tmu3.setStrokeDuration(3);
        mcp.play(6.9);
        mcp.updateTiming(7);

        assertEquals(2, mcp.getStartTime(), TIME_PRECISION);
        assertEquals(3, mcp.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, mcp.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, seq.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(8, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2.getTime("strokeEnd"), TIME_PRECISION);

        assertEquals(11, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(13, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
}
