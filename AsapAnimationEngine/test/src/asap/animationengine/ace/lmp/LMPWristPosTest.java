/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.math.Vec3f;
import hmi.neurophysics.BiologicalSwivelCostsEvaluator;
import hmi.testutil.animation.HanimBody;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.ShapeSymbols;
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
 * Unit tests for the LMPWristPos
 * @author hvanwelbergen
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class LMPWristPosTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    private double TIMING_PRECISION = 0.001;
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);

    @Before
    public void setup()
    {
        pegBoard.addBMLBlockPeg(new BMLBlockPeg("bml1", 0));
    }

    private LMPWristPos setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        GuidingSequence gSeq = new GuidingSequence();
        gSeq.addGuidingStroke(new CurvedGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(0.1f, 0, 0), Vec3f.getZero(), ShapeSymbols.LeftC,
                0, 0, 0));
        gSeq.addGuidingStroke(new CurvedGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(0.2f, 0, 0), Vec3f.getZero(), ShapeSymbols.LeftC,
                0, 0, 0));
        gSeq.getStroke(0).setEDt(0.7);
        gSeq.getStroke(1).setEDt(0.7);
        
        when(mockAniPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAniPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAniPlayer.getVCurrPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        when(mockAniPlayer.getVNextPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        
        return new LMPWristPos("right_arm", bfm, bbPeg, bmlId, id, pegBoard, gSeq, Hanim.HumanoidRoot, mockAniPlayer,
                new BiologicalSwivelCostsEvaluator(-2, 1, 0.1));
    }

    @Override
    protected LMPWristPos setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPWristPos lmp = setupPlanUnit(bfm, bbPeg, bmlId, id);
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
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.updateTiming(0);
        assertEquals(1 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);
        assertEquals(1, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartConstraints() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.8f));
        tau.updateTiming(0);

        assertEquals(0.8 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.8, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(0.8 + 0.2 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeStartAndEndConstraint() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 2.5f));

        tau.updateTiming(0);
        assertEquals(0, tau.getTime("start"), TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(2.5, tau.getTime("strokeEnd"), TIMING_PRECISION);
    }

    @Test
    public void testAvailableSyncs() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1");
        assertThat(tau.getAvailableSyncs(),
                IsIterableContainingInAnyOrder.containsInAnyOrder("start", "strokeStart", "strokeEnd"));
    }
}
