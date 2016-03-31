/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.OrientConstraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
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
    private AnimationPlayer mockAniPlayer = AnimationPlayerMock.createAnimationPlayerMock();

    @Before
    public void setup()
    {
        pegBoard.addBMLBlockPeg(new BMLBlockPeg("bml1", 0));
    }

    private LMPWristRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        List<OrientConstraint> ocList = new ArrayList<>();
        ocList.add(new OrientConstraint("strokeStart", GStrokePhaseID.STP_STROKE));
        ocList.add(new OrientConstraint("stroke1", GStrokePhaseID.STP_STROKE));
        ocList.add(new OrientConstraint("stroke2", GStrokePhaseID.STP_STROKE));
        ocList.add(new OrientConstraint("strokeEnd", GStrokePhaseID.STP_RETRACT));

        LMPWristRot wr = new LMPWristRot("right_arm", ocList, bfm, bbPeg, bmlId, id, pegBoard, mockAniPlayer);
        initializeForUpdateTiming(wr);
        wr.setState(TimedPlanUnitState.IN_PREP);
        return wr;
    }

    @Override
    protected LMPWristRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPWristRot lmp = setupPlanUnit(bfm, bbPeg, bmlId, id);
        lmp.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return lmp;
    }

    private void initializeForUpdateTiming(TimedAnimationUnit tau)
    {
        tau.setState(TimedPlanUnitState.LURKING);
        tau.setTimePeg("start", new TimePeg(BMLBlockPeg.GLOBALPEG));
        tau.setTimePeg("end", new TimePeg(BMLBlockPeg.GLOBALPEG));
        tau.getTimePeg("strokeStart").setGlobalValue(1);
        tau.getTimePeg("strokeEnd").setGlobalValue(1 + tau.getStrokeDuration());
    }

    @Test
    public void testUpdateTimingNoConstraints() throws TimedPlanUnitPlayException
    {
        LMPWristRot tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);

        tau.updateTiming(0);
        assertEquals(LMPWristRot.TRANSITION_TIME * 3, tau.getStrokeDuration(), TIMING_PRECISION);
        assertEquals(1 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);
        assertEquals(1, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration() / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(1 + 2 * tau.getStrokeDuration() / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartConstraint() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));

        tau.updateTiming(0);
        assertEquals(0.5 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
        double strokeDur = 0.5 + tau.getStrokeDuration();
        assertEquals(0.5 + strokeDur / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.5 + 2 * strokeDur / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartAndEndConstraint() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 2.5f));
        tau.updateTiming(0);

        assertEquals(0.5 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2.5, tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.5 + 2d / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.5 + 2d / 3d + 2d / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartConstraintSkew() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.3f));
        tau.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.3f + tau.getStrokeDuration()));
        tau.updateTiming(0);

        assertEquals(0, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.3, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(0.3 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(0.3 + tau.getStrokeDuration() / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(0.3 + 2 * tau.getStrokeDuration() / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }

    @Test
    public void testAvailableSyncs() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        assertThat(tau.getAvailableSyncs(),
                IsIterableContainingInAnyOrder.containsInAnyOrder("start", "strokeStart", "stroke1", "stroke2", "strokeEnd", "end"));
    }
}
