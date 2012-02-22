package asap.animationengine.procanimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.procanimation.ProcAnimationGestureMU;
import asap.animationengine.procanimation.ProcAnimationGestureTMU;
import asap.animationengine.procanimation.ProcAnimationMU;
import asap.animationengine.restpose.RestPose;

import hmi.animation.VJoint;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.BMLGestureSync;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.testutil.animation.HanimBody;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.*;

/**
 * Unit test cases for ProcAnimationGestureTMUs
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class ProcAnimationGestureTMUTest extends AbstractTimedPlanUnitTest
{
    private ProcAnimationMU mockProcAnimation = mock(ProcAnimationMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    
    @SuppressWarnings("unchecked")
    @Override
    protected ProcAnimationGestureTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        ProcAnimationGestureMU mu = new ProcAnimationGestureMU();
        mu.setGestureUnit(mockProcAnimation);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockProcAnimation.copy((VJoint)any())).thenReturn(mockProcAnimation);
        when(mockProcAnimation.getPreferedDuration()).thenReturn(1d);
        when(mockProcAnimation.getPrefDuration()).thenReturn(1d);
        KeyPositionMocker.stubKeyPositions(mockProcAnimation,new KeyPosition("start",0),new KeyPosition("ready",0.4),
                new KeyPosition("strokeStart",0.4),new KeyPosition("stroke",0.5),new KeyPosition("strokeEnd",0.8),
                new KeyPosition("relax",0.8), new KeyPosition("end",1));
        
        RestPose mockRestPose = mock(RestPose.class);
        MotionUnit mockRelaxMU = mock(MotionUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);        
        when(mockRestPose.createTransitionToRest((Set<String>)any())).thenReturn(mockRelaxMU);
        
        
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        mu.setAnimationPlayer(mockAnimationPlayer);
        
        ProcAnimationGestureTMU tmu = new ProcAnimationGestureTMU(bfm, bbPeg, bmlId, id,mu );
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }
    
    @Test
    public void resolveEmptySynchsTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
    }
    
    @Test
    public void resolveEndFromStartTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0.1);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0.1,tpStart.getGlobalValue(),0.0001);
        assertEquals(0.1+tpu.getPreferedDuration(),tpEnd.getGlobalValue(),0.0001);        
    }
    
    @Test
    public void resolveStartFromEndTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(10);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(10,tpEnd.getGlobalValue(),0.0001);
        assertEquals(10-tpu.getPreferedDuration(),tpStart.getGlobalValue(),0.0001);        
    }
    
    @Test
    public void resolveStartSynchTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0,tp.getGlobalValue(),0.0001);
    }
    
    @Test
    public void resolveStrokeFromStartAndEndTest()throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpStroke = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tpEnd.setGlobalValue(1);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("stroke", tpStroke, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0,tpStart.getGlobalValue(),0.0001);
        assertEquals(1,tpEnd.getGlobalValue(),0.0001);
        assertThat(tpStroke.getGlobalValue(),greaterThan(0d));
        assertThat(tpStroke.getGlobalValue(),lessThan(1d));
    }
}
