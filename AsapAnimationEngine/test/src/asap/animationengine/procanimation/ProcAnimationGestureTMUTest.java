package asap.animationengine.procanimation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.bml.BMLGestureSync;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TMUPlayException;
import asap.animationengine.motionunit.TimedMotionUnitTest;
import asap.animationengine.restpose.RestPose;

/**
 * Unit test cases for ProcAnimationGestureTMUs
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class ProcAnimationGestureTMUTest extends TimedMotionUnitTest
{
    private ProcAnimationMU mockProcAnimation = mock(ProcAnimationMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    private final double STROKE_DURATION = 1;
    
    @SuppressWarnings("unchecked")
	protected ProcAnimationGestureTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId)
    {
    	if(pegBoard.getBMLBlockPeg(bmlId)==null)
        {
            pegBoard.addBMLBlockPeg(new BMLBlockPeg(bmlId,0));
        }
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        ProcAnimationGestureMU mu = new ProcAnimationGestureMU();
        mu.setGestureUnit(mockProcAnimation);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockProcAnimation.copy((VJoint) any())).thenReturn(mockProcAnimation);
        when(mockProcAnimation.getPreferedDuration()).thenReturn(STROKE_DURATION);
        when(mockProcAnimation.getPrefDuration()).thenReturn(STROKE_DURATION);
        KeyPositionMocker.stubKeyPositions(mockProcAnimation, new KeyPosition("start", 0), new KeyPosition("ready", 0.4), new KeyPosition(
                "strokeStart", 0.4), new KeyPosition("stroke", 0.5), new KeyPosition("strokeEnd", 0.8), new KeyPosition("relax", 0.8),
                new KeyPosition("end", 1));

        RestPose mockRestPose = mock(RestPose.class);
        when(mockRestPose.getTransitionToRestDuration((VJoint) any(), (Set<String>) any())).thenReturn(1d);

        AnimationUnit mockRelaxMU = mock(AnimationUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);
        when(mockRestPose.createTransitionToRest((Set<String>) any())).thenReturn(mockRelaxMU);

        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        mu.setAnimationPlayer(mockAnimationPlayer);

        ProcAnimationGestureTMU tmu = new ProcAnimationGestureTMU(bfm, bbPeg, bmlId, id, mu, pegBoard);
        tmu.resolveDefaultBMLKeyPositions();
        return tmu;
    }
    
    @Override
    protected ProcAnimationGestureTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
    	ProcAnimationGestureTMU tmu = setupPlanUnit(bfm, bbPeg, id, bmlId);
    	TimePeg tp = TimePegUtil.createTimePeg(bbPeg, startTime);
        tmu.setTimePeg("start", tp);        
        pegBoard.addTimePeg("bml1", "id1", "start", tp);
        return tmu;
    }

    @Test
    public void resolveEmptySynchsTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
    }

    @Test
    public void resolveEndFromStartTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0.1);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0.1, tpStart.getGlobalValue(), 0.0001);
        assertEquals(0.1 + tpu.getPreferedDuration(), tpEnd.getGlobalValue(), 0.0001);
    }

    @Test
    public void resolveStartFromEndTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(10);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(10, tpEnd.getGlobalValue(), 0.0001);
        assertEquals(10 - tpu.getPreferedDuration(), tpStart.getGlobalValue(), 0.0001);
    }

    @Test
    public void resolveStartSynchTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tp.getGlobalValue(), 0.0001);
    }

    @Test
    public void resolveStrokeFromStartAndEndTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpStroke = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tpEnd.setGlobalValue(3);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("stroke", tpStroke, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tpStart.getGlobalValue(), 0.0001);
        assertEquals(3, tpEnd.getGlobalValue(), 0.0001);
        assertThat(tpStroke.getGlobalValue(), greaterThan(0d));
        assertThat(tpStroke.getGlobalValue(), lessThan(3d));
    }

    @Test
    public void resolveEndFromStartAndRelaxTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpRelax = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tpRelax.setGlobalValue(2);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("relax", tpRelax, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tpStart.getGlobalValue(), 0.0001);
        assertEquals(2, tpRelax.getGlobalValue(), 0.0001);
        assertThat(tpEnd.getGlobalValue(), greaterThan(2d));
    }
    
    @Test
    public void testUpdateTimingNonSetTTPUAtT0() throws TMUPlayException
    {
    	ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
    	tpu.updateTiming(0);
    	assertEquals(0, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
    }
    
    @Test
    public void testUpdateTimingNonSetTTPUAtT1() throws TMUPlayException
    {
    	ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
    	tpu.updateTiming(1);
    	assertEquals(1, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
    }
    
    @Test
    public void testUpdateTimingSetStart() throws TMUPlayException
    {
    	ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1",1);
    	assertEquals(1, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	tpu.updateTiming(0);
    	assertEquals(0, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
    }
    
    @Test
    public void testUpdateTimingFixedStart() throws TMUPlayException
    {
    	ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1",1);
    	TimePeg tp = pegBoard.getTimePeg("bml1", "id1", "start");
    	pegBoard.addTimePeg("bml1", "id2", "start", tp);
    	tpu.updateTiming(0);
    	assertEquals(1, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
    }
    
    @Test
    public void testUpdateTimingPassedStart() throws TMUPlayException
    {
    	ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1",0);
    	tpu.updateTiming(1);
    	assertEquals(0, tpu.getTimePeg("start").getGlobalValue(),0.0001);
    	assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
    }
    
    @Test
    public void testUpdateTimingStrokeSet() throws TMUPlayException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        
        TimePeg tp = TimePegUtil.createTimePeg( BMLBlockPeg.GLOBALPEG, 1);
        tpu.setTimePeg("stroke", tp);        
        pegBoard.addTimePeg("bml1", "id1", "stroke", tp);
        pegBoard.addTimePeg("bml1", "id2", "start", tp);        
        
        tpu.updateTiming(0);
        assertEquals(1, tpu.getTimePeg("stroke").getGlobalValue(),0.0001);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd")-tpu.getTime("strokeStart"),0.0001);
        assertThat(tpu.getTimePeg("start").getLocalValue(),greaterThanOrEqualTo(0d));
        assertThat(tpu.getTimePeg("start").getGlobalValue(),greaterThanOrEqualTo(0d));
    }
}
