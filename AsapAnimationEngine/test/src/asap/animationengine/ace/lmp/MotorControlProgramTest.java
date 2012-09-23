package asap.animationengine.ace.lmp;

import static asap.testutil.bml.feedback.FeedbackAsserts.assertEqualSyncPointProgress;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.parser.Constraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
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
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class MotorControlProgramTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    private TimedAnimationUnit mockTimedAnimationUnit = mock(TimedAnimationUnit.class);
    private static double TIMING_PRECISION = 0.001;
    private static final double LMP_STROKE_TIME = 2d;
    private static final double LMP_END_TIME = 4d;
    private static final double LMP_START_TIME = 1d;
    
    @Before
    public void setup()
    {
        when(mockTimedAnimationUnit.getEndTime()).thenReturn(LMP_END_TIME);
        when(mockTimedAnimationUnit.getStartTime()).thenReturn(LMP_START_TIME);
        when(mockTimedAnimationUnit.getTime("stroke")).thenReturn(LMP_STROKE_TIME);
        when(mockTimedAnimationUnit.getTimePeg("stroke")).thenReturn(TimePegUtil.createTimePeg(LMP_STROKE_TIME));
    }
    
    private MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new MotorControlProgram(bfm, bbPeg, bmlId, id, pegBoard, new PegBoard());        
    }
    
    @Override
    protected MotorControlProgram setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        MotorControlProgram mcp = setupPlanUnit(bfm, bbPeg, bmlId,id);
        mcp.setTimePeg("start",  TimePegUtil.createTimePeg(bbPeg, startTime));
        return mcp;
    }
    
    @Test
    public void testUpdateTiming() throws TMUPlayException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        mcp.updateTiming(0);
        assertEquals(LMP_START_TIME, mcp.getTime("start"), TIMING_PRECISION);
        assertEquals(LMP_START_TIME, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(LMP_START_TIME, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(LMP_STROKE_TIME, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(LMP_END_TIME, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(LMP_END_TIME, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(LMP_END_TIME, mcp.getTime("end"), TIMING_PRECISION);
    }

    @Test
    public void testStrokeFeedback() throws TimedPlanUnitPlayException
    {
        MotorControlProgram mcp = (MotorControlProgram)setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "beh1","bml1",0);
        mcp.addLMP(mockTimedAnimationUnit);
        mcp.setState(TimedPlanUnitState.LURKING);
        mcp.updateTiming(0);
        mcp.start(0);
        mcp.play(2.1);
        assertEquals(4,fbList.size());
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 0, 0), fbList.get(0));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "ready", 2.1, 2.1), fbList.get(1));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "strokeStart", 2.1, 2.1), fbList.get(2));
        assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", 2.1, 2.1), fbList.get(3));
    }
    
    @Test
    public void testEndFeedback()throws TimedPlanUnitPlayException
    {
        MotorControlProgram mcp = (MotorControlProgram)setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "beh1","bml1",0);
        mcp.addLMP(mockTimedAnimationUnit);
        mcp.setState(TimedPlanUnitState.LURKING);
        mcp.updateTiming(0);
        mcp.start(0);
        mcp.stop(5);
        assertEquals(7,fbList.size());
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
        mcp.addLMP(mockTimedAnimationUnit);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(1, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(4, mcp.getEndTime(), TIMING_PRECISION);
    }
    
    @Test
    public void testResolveSyncStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(2, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(4, mcp.getEndTime(), TIMING_PRECISION);
    }
    
    @Test
    public void testResolveSyncStrokeConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("stroke", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(1, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(4, mcp.getEndTime(), TIMING_PRECISION);
    }
    
    @Test
    public void testResolveSyncStrokeEndAndStartConstraint() throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(1, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(4, mcp.getEndTime(), TIMING_PRECISION);
    }
    
    @Test
    public void testResolveSyncStrokeStartStarAndEndConstraint()throws BehaviourPlanningException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("strokeStart", TimePegUtil.createTimePeg(2), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("strokeEnd", TimePegUtil.createTimePeg(3), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(10), new Constraint(), 2, false));
        mcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, new MURMLGestureBehaviour("bml1"), sacs);
        assertEquals(1, mcp.getStartTime(), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(3, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(8, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(8, mcp.getEndTime(), TIMING_PRECISION);
    }
    
    
}
