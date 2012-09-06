package asap.animationengine.ace.lmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.TPConstraint;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.ShapeSymbols;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
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
    
    private LMPWristPos setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        GuidingSequence gSeq = new GuidingSequence();
        gSeq.addGuidingStroke(new CurvedGStroke(GStrokePhaseID.STP_STROKE, new TPConstraint(0), Vec3f.getVec3f(0.1f,0,0), Vec3f.getZero(),ShapeSymbols.LeftC,0,0,0));
        gSeq.addGuidingStroke(new CurvedGStroke(GStrokePhaseID.STP_STROKE, new TPConstraint(0), Vec3f.getVec3f(0.2f,0,0), Vec3f.getZero(),ShapeSymbols.LeftC,0,0,0));
        when(mockAniPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAniPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
        return new LMPWristPos("right_arm", bfm, bbPeg, bmlId, id, pegBoard, gSeq, mockAniPlayer);
    }
    @Override
    protected LMPWristPos setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPWristPos lmp = setupPlanUnit(bfm, bbPeg, bmlId, id);
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
        assertEquals(0, tau.getTime("ready"),TIMING_PRECISION);
        assertThat(tau.getTime("strokeStart"),greaterThan(tau.getTime("start")));
        assertThat(tau.getTime("stroke"),greaterThan(tau.getTime("start")));
        assertThat(tau.getTime("strokeEnd"),greaterThan(tau.getTime("strokeStart")));        
        assertThat(tau.getTime("end"),greaterThan(tau.getTime("strokeEnd")));
        assertEquals(tau.getTime("relax"),tau.getTime("end"), TIMING_PRECISION);
    }

}
