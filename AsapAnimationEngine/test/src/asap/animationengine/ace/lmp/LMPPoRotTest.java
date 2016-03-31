/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.PoConstraint;
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
import static org.mockito.Matchers.any;
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
    private PegBoard pegBoard = new PegBoard();    
    private double TIMING_PRECISION = 0.001;
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    
    @Before
    public void setup()
    {
        pegBoard.addBMLBlockPeg(new BMLBlockPeg("bml1",0));
    }
    
    @SuppressWarnings("unchecked")
    private LMPPoRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId)
    {
        List<PoConstraint> ocList = new ArrayList<>();
        ocList.add(new PoConstraint(10,GStrokePhaseID.STP_STROKE,"strokeStart"));
        ocList.add(new PoConstraint(11,GStrokePhaseID.STP_STROKE,"stroke1"));
        ocList.add(new PoConstraint(12,GStrokePhaseID.STP_STROKE,"stroke2"));
        ocList.add(new PoConstraint(13,GStrokePhaseID.STP_STROKE,"strokeEnd"));    
        when(mockAniPlayer.constructAdditiveBody(any(Set.class))).thenReturn(HanimBody.getLOA1HanimBody());
        LMPPoRot lmp = new LMPPoRot("right_arm", ocList, bfm, bbPeg, bmlId, id, pegBoard,mockAniPlayer);
        initializeForUpdateTiming(lmp);
        lmp.setState(TimedPlanUnitState.IN_PREP);
        return lmp;
    }
    
    @Override
    protected LMPPoRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPPoRot lmp = setupPlanUnit(bfm, bbPeg, id, bmlId);
        lmp.setTimePeg("start",  TimePegUtil.createTimePeg(bbPeg, startTime));
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
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1");
        initializeForUpdateTiming(tau);
        tau.updateTiming(0);
        
        assertEquals(1 - tau.getPreparationDuration(), tau.getTime("start"), TIMING_PRECISION);        
        assertEquals(1, tau.getTime("strokeStart"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration(), tau.getTime("strokeEnd"), TIMING_PRECISION);
        assertEquals(1 + tau.getStrokeDuration() / 3d, tau.getTime("stroke1"), TIMING_PRECISION);
        assertEquals(1 + 2 * tau.getStrokeDuration() / 3d, tau.getTime("stroke2"), TIMING_PRECISION);
    }
    
    @Test
    public void testUpdateTimingStrokeStartConstraint() throws TimedPlanUnitPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));        
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
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1","bml1");
        initializeForUpdateTiming(tau);
        tau.setTimePeg("strokeStart",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0.5f));
        tau.setTimePeg("strokeEnd",TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 2.5f));        
        tau.updateTiming(0);
        assertEquals(0.1, tau.getTime("start"),TIMING_PRECISION);
        assertEquals(0.5, tau.getTime("strokeStart"),TIMING_PRECISION);
        assertEquals(2+0.4+0.1, tau.getTime("strokeEnd"),TIMING_PRECISION);
        assertEquals(0.4+0.1+2d/3d, tau.getTime("stroke1"),TIMING_PRECISION);
        assertEquals(0.4+0.1+2d/3d+2d/3d, tau.getTime("stroke2"),TIMING_PRECISION);
    }
    
    @Test
    public void testAvailableSyncs() throws TMUPlayException
    {
        TimedAnimationUnit tau = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1");
        assertThat(tau.getAvailableSyncs(),containsInAnyOrder("start", "strokeStart", "stroke1", "stroke2", "strokeEnd", "end"));
    }

}
