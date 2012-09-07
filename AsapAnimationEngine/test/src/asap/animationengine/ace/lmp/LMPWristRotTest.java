package asap.animationengine.ace.lmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.OrientConstraint;
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
 * Testcases for the WristRot LMP
 * @author hvanwelbergen
 * 
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class LMPWristRotTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    private double TIMING_PRECISION = 0.001;
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);

    private TimedAnimationUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        List<OrientConstraint> ocList = new ArrayList<>();
        ocList.add(new OrientConstraint("strokeStart"));
        ocList.add(new OrientConstraint("stroke1"));
        ocList.add(new OrientConstraint("stroke2"));
        ocList.add(new OrientConstraint("strokeEnd"));
        when(mockAniPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAniPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
        return new LMPWristRot("right_arm", ocList, bfm, bbPeg, bmlId, id, pegBoard, mockAniPlayer);
    }

    @Override
    protected TimedAnimationUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedAnimationUnit lmp = setupPlanUnit(bfm, bbPeg, bmlId, id);
        lmp.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return lmp;
    }

    @Test
    public void testUpdateTimingNoConstraints() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(5 + 0.8, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(0.4, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(5 + 0.4, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.4 + 5d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.4 + 5d / 3d + 5d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0.1, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.1 + 5 + 0.8, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(5 + 0.4 + 0.1, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.4 + 0.1 + 5d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.4 + 0.1 + 5d / 3d + 5d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartAndEndConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 2.5f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0.1, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.1 + 2 + 0.8, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2.5, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.4 + 0.1 + 2d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.4 + 0.1 + 2d / 3d + 2d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingEndConstraint() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);

        assertEquals(10, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(10 - 0.4 - 5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(10 - 0.4 - 0.4 - 5, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(10 - 0.4, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(10 - 0.4 - 5 + 5d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(10 - 0.4 - 5 + 5d / 3d + 5d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingEndConstraintFrontSkew() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 3f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);

        assertEquals(3, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(0, tau.getTime("start"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartConstraintSkew() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.3f));
        tau.setState(TimedPlanUnitState.LURKING);
        tau.updateTiming(0);
        assertEquals(0, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(-0.1 + 5 + 0.8, tau.getTime("end"), TIMING_PRECISION);
        assertEquals(0.3, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(5 + 0.4 - 0.1, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.4 - 0.1 + 5d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.4 - 0.1 + 5d / 3d + 5d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }
    
    @Test
    public void testAvailableSyncs() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        assertThat(tau.getAvailableSyncs(),
                IsIterableContainingInAnyOrder.containsInAnyOrder("strokeStart", "stroke1", "stroke2", "strokeEnd"));
    }
}
