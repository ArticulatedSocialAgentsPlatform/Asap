package asap.animationengine.ace.lmp;

import static asap.testutil.bml.feedback.FeedbackAsserts.assertEqualSyncPointProgress;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
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
    
    @Before
    public void setup()
    {
        when(mockTimedAnimationUnit.getEndTime()).thenReturn(4d);
        when(mockTimedAnimationUnit.getStartTime()).thenReturn(1d);
        when(mockTimedAnimationUnit.getTime("stroke")).thenReturn(2d);
        when(mockTimedAnimationUnit.getTimePeg("stroke")).thenReturn(TimePegUtil.createTimePeg(2));
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
    public void testSyncResolve() throws TMUPlayException
    {
        MotorControlProgram mcp = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        mcp.addLMP(mockTimedAnimationUnit);
        mcp.updateTiming(0);
        assertEquals(1, mcp.getTime("start"), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("ready"), TIMING_PRECISION);
        assertEquals(1, mcp.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2, mcp.getTime("stroke"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("relax"), TIMING_PRECISION);
        assertEquals(4, mcp.getTime("end"), TIMING_PRECISION);
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
}
