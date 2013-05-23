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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.parser.Constraint;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.OrientConstraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

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

    private static double TIMING_PRECISION = 0.001;
    private static final double LMP_PREPDUR = 0.4;
    private static final double LMP_STROKEDUR = 1;
    private static final double LMP_RETRACTIONDUR = 0.3;

    private BMLBlockPeg bml1Peg = new BMLBlockPeg("bml1", 0);
    private StubLMP stubTimedAnimationUnit = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal", localPegboard, globalPegBoard, new HashSet<String>(),
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
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        final double LMP_PREPDUR_NEW = 0.3;
        stubTimedAnimationUnit.setPrepDuration(LMP_PREPDUR_NEW);
        mcp.updateTiming(0);

        assertEquals(2 - LMP_PREPDUR_NEW, mcp.getTime("start"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(2 + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(2 + LMP_STROKEDUR, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(2 + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getTime("end"), TIMING_PRECISION);
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
        assertEquals(LMP_PREPDUR, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMP_PREPDUR, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP_PREPDUR, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(LMP_PREPDUR + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getTime("end"), TIMING_PRECISION);
    }

    @Test
    public void testResolveSyncStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(2 + LMP_PREPDUR + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testResolveSyncStartConstraintLMPWristRot() throws BehaviourPlanningException
    {

        List<OrientConstraint> ocVec = new ArrayList<>();
        ocVec.add(new OrientConstraint("strokeStart"));
        ocVec.add(new OrientConstraint("strokeEnd"));
        LMPWristRot lmpWristRot = new LMPWristRot("left_arm", ocVec, fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", localPegboard,
                globalPegBoard, mockAnimationPlayer);
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmpWristRot);

        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(LMPWristRot.TRANSITION_TIME + LMPWristRot.TRANSITION_TIME + 2 + LMPWristRot.DEFAULT_STROKEPHASE_DURATION,
                mcp.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testResolveSyncStrokeConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("stroke", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(3 - LMP_PREPDUR, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(3 + LMP_STROKEDUR, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(3 + LMP_STROKEDUR, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(3 + LMP_STROKEDUR + LMP_RETRACTIONDUR, mcp.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testResolveSyncStrokeEndAndStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2 - LMP_PREPDUR, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(3 + LMP_RETRACTIONDUR, mcp.getEndTime(), TIMING_PRECISION);
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
        assertEquals(2 - LMP_PREPDUR, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(10 - 2, mcp.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testResolveSequenceNoConstraints() throws BehaviourPlanningException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1", localPegboard, globalPegBoard,
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2", localPegboard, globalPegBoard,
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);

        LMPSequence lmp = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard,globalPegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1).add(stubTimedAnimationUnit2).build());
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmp);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        assertEquals(0, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIMING_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2.getTime("strokeEnd"), TIMING_PRECISION);
    }

    @Test
    public void testResolveSequenceInParallelNoConstraints() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1a = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1a", localPegboard, globalPegBoard, new HashSet<String>(),
                new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit1b = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1b", localPegboard, globalPegBoard, new HashSet<String>(),
                new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2a = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2a", localPegboard, globalPegBoard, new HashSet<String>(),
                new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);
        StubLMP stubTimedAnimationUnit2b = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2b", localPegboard, globalPegBoard, new HashSet<String>(),
                new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);
        LMPSequence lmpS1 = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard,globalPegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1a).add(stubTimedAnimationUnit2a).build());
        LMPSequence lmpS2 = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard,globalPegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(stubTimedAnimationUnit1b).add(stubTimedAnimationUnit2b).build());
        LMPParallel lmp = new LMPParallel(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard,globalPegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(lmpS1).add(lmpS2).build());

        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", lmp);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);

        assertEquals(0, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIMING_PRECISION);

        assertEquals(0, lmpS1.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, lmpS1.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, lmpS1.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, lmpS1.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(0, lmpS2.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, lmpS2.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, lmpS2.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, lmpS2.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1a.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1a.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1a.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2a.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2a.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2a.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1b.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1b.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1b.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2b.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP, stubTimedAnimationUnit2b.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP + LMP2_STROKE, stubTimedAnimationUnit2b.getTime("strokeEnd"), TIMING_PRECISION);
    }

    @Test
    public void testSequenceDynamicTimingUpdate() throws BehaviourPlanningException, TimedPlanUnitPlayException
    {
        final double LMP1_PREP = 1, LMP1_RETR = 1, LMP1_STROKE = 2;
        final double LMP2_PREP = 2, LMP2_RETR = 3, LMP2_STROKE = 4;
        StubLMP stubTimedAnimationUnit1 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal1", localPegboard, globalPegBoard,
                new HashSet<String>(), new HashSet<String>(), LMP1_PREP, LMP1_RETR, LMP1_STROKE);
        StubLMP stubTimedAnimationUnit2 = new StubLMP(fbManager, bml1Peg, "bml1", "beh1_internal2", localPegboard, globalPegBoard,
                new HashSet<String>(), new HashSet<String>(), LMP2_PREP, LMP2_RETR, LMP2_STROKE);

        LMPSequence lmp = new LMPSequence(fbManager, bml1Peg, "bml1", "beh1_seq", localPegboard, globalPegBoard,
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

        assertEquals(0, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        // assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, mcp.getTime("relax"), TIMING_PRECISION);
        //assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE + LMP2_RETR, mcp.getTime("end"), TIMING_PRECISION);

        assertEquals(0, stubTimedAnimationUnit1.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP, stubTimedAnimationUnit1.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit1.getTime("strokeEnd"), TIMING_PRECISION);

        assertEquals(LMP1_PREP + LMP1_STROKE, stubTimedAnimationUnit2.getStartTime(), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW, stubTimedAnimationUnit2.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP1_PREP + LMP1_STROKE + LMP2_PREP_NEW + LMP2_STROKE, stubTimedAnimationUnit2.getTime("strokeEnd"), TIMING_PRECISION);
    }
}
