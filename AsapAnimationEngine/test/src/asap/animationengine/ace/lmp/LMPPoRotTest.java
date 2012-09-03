package asap.animationengine.ace.lmp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.PoConstraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit tests for LMPPoRot
 * @author Herwin
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class LMPPoRotTest extends AbstractTimedPlanUnitTest
{
    //private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    private double TIMING_PRECISION = 0.001;
    
    private LMPPoRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId)
    {
        List<PoConstraint> ocList = new ArrayList<>();
        ocList.add(new PoConstraint(10,GStrokePhaseID.STP_STROKE,"strokeStart"));
        ocList.add(new PoConstraint(11,GStrokePhaseID.STP_STROKE,"stroke1"));
        ocList.add(new PoConstraint(12,GStrokePhaseID.STP_STROKE,"stroke2"));
        ocList.add(new PoConstraint(13,GStrokePhaseID.STP_STROKE,"strokeEnd"));
        LMPPoRot lmp = new LMPPoRot("right_arm", ocList, bfm, bbPeg, bmlId, id, pegBoard);
        return lmp;
    }
    
    @Override
    protected LMPPoRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPPoRot lmp = setupPlanUnit(bfm, bbPeg, id, bmlId);
        lmp.setTimePeg("start",  TimePegUtil.createTimePeg(bbPeg, startTime));
        return lmp;
    }
    
    @Test
    public void testUpdateTimingNoConstraints() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0, tau.getTime("start"),TIMING_PRECISION);
        assertEquals(5+0.8, tau.getTime("end"),TIMING_PRECISION);
        assertEquals(0.4, tau.getTime("strokeStart"),TIMING_PRECISION);
        assertEquals(5+0.4, tau.getTime("strokeEnd"),TIMING_PRECISION);
        assertEquals(0.4+5d/3d, tau.getTime("stroke1"),TIMING_PRECISION);
        assertEquals(0.4+5d/3d+5d/3d, tau.getTime("stroke2"),TIMING_PRECISION);
    }
    
    @Test
    public void testUpdateTimingStrokeStartConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("strokeStart",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0.1, tau.getTime("start"),TIMING_PRECISION);
        assertEquals(0.1+5+0.8, tau.getTime("end"),TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"),TIMING_PRECISION);
        assertEquals(5+0.4+0.1, tau.getTime("strokeEnd"),TIMING_PRECISION);
        assertEquals(0.4+0.1+5d/3d, tau.getTime("stroke1"),TIMING_PRECISION);
        assertEquals(0.4+0.1+5d/3d+5d/3d, tau.getTime("stroke2"),TIMING_PRECISION);
    }
    
    @Test
    public void testUpdateTimingStrokeStartAndEndConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("strokeStart",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setTimePeg("strokeEnd",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 2.5f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0.1, tau.getTime("start"),TIMING_PRECISION);
        assertEquals(0.1+2+0.8, tau.getTime("end"),TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"),TIMING_PRECISION);
        assertEquals(2+0.4+0.1, tau.getTime("strokeEnd"),TIMING_PRECISION);
        assertEquals(0.4+0.1+2d/3d, tau.getTime("stroke1"),TIMING_PRECISION);
        assertEquals(0.4+0.1+2d/3d+2d/3d, tau.getTime("stroke2"),TIMING_PRECISION);
    }
    
    @Test
    public void testUpdateTimingEndConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("end",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        
        
        assertEquals(10, tau.getTime("end"),TIMING_PRECISION);
        assertEquals(10-0.4-5, tau.getTime("strokeStart"),TIMING_PRECISION);
        assertEquals(10-0.4-0.4-5, tau.getTime("start"),TIMING_PRECISION);
        assertEquals(10-0.4, tau.getTime("strokeEnd"),TIMING_PRECISION);
        assertEquals(10-0.4-5+5d/3d, tau.getTime("stroke1"),TIMING_PRECISION);
        assertEquals(10-0.4-5+5d/3d+5d/3d, tau.getTime("stroke2"),TIMING_PRECISION);
    }
    
    @Test
    public void testUpdateTimingEndConstraintFrontSkew() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("end",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 3f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        
        assertEquals(3, tau.getTime("end"),TIMING_PRECISION);
        assertEquals(0, tau.getTime("start"),TIMING_PRECISION);        
    }

}
