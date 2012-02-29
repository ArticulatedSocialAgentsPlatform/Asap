package asap.animationengine.procanimation;

import java.util.Set;

import hmi.animation.VJoint;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.animationengine.procanimation.ProcAnimationGestureMU;
import asap.animationengine.procanimation.ProcAnimationMU;
import asap.animationengine.restpose.RestPose;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;
import static hmi.elckerlyc.util.TimePegUtil.*;

/**
 * Unit test cases for ProcAnimationGestureMU
 * @author Herwin
 *
 */
public class ProcAnimationGestureMUMockupProcTest
{
    private ProcAnimationMU mockProcAnimation = mock(ProcAnimationMU.class);
    private ProcAnimationMU mockProcAnimationCopy = mock(ProcAnimationMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private VJoint vCurr;
    private ProcAnimationGestureMU pag;
    private PegBoard pegBoard = new PegBoard();
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockProcAnimation.getPrefDuration()).thenReturn(1d);
        when(mockProcAnimation.getPreferedDuration()).thenReturn(1d);
        when(mockProcAnimation.copy(mockAnimationPlayer)).thenReturn(mockProcAnimation);
        when(mockProcAnimation.copy((VJoint)any())).thenReturn(mockProcAnimationCopy);
        KeyPositionMocker.stubKeyPositions(mockProcAnimation,new KeyPosition("start",0),new KeyPosition("ready",0.4),
                new KeyPosition("strokeStart",0.4),new KeyPosition("stroke",0.5),new KeyPosition("strokeEnd",0.8),
                new KeyPosition("relax",0.8), new KeyPosition("end",1));
        
        pag = new ProcAnimationGestureMU();
        pag.setGestureUnit(mockProcAnimation);
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.START.getId(), 0, 1.0));        
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE.getId(), 0.8, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), 0.8, 1.0));        
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.END.getId(), 1, 1.0));
        pag.setAnimationPlayer(mockAnimationPlayer);
        pag.setupTransitionUnits();
        
        RestPose mockRestPose = mock(RestPose.class);
        MotionUnit mockRelaxMU = mock(MotionUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);
        when(mockRestPose.getTransitionToRestDuration((VJoint)any(), (Set<String>)any())).thenReturn(0.5d);
        when(mockRestPose.createTransitionToRest((Set<String>)any())).thenReturn(mockRelaxMU);
        pag.setupRelaxUnit();
    }
    
    @Test
    public void testPlayPrep() throws MUPlayException
    {
        pag.play(0);
        verify(mockProcAnimation,times(0)).play(anyDouble());
    }
    
    @Test
    public void testPlayStrokeStart() throws MUPlayException
    {
        pag.play(0.2001);
        verify(mockProcAnimation,times(1)).play(eq(0.4,0.05));
    }
    
    @Test
    public void testPlayStrokeEnd() throws MUPlayException
    {
        pag.play(0.79);
        verify(mockProcAnimation,times(1)).play(eq(0.8,0.05));
    }
    
    @Test
    public void testPlayRetract() throws MUPlayException
    {
        pag.play(0.9);
        verify(mockProcAnimation,times(0)).play(anyDouble());
    }
   
    @Test
    public void testCreateTMU() throws TimedPlanUnitPlayException
    {
        TimedMotionUnit tmu = pag.createTMU(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "bml1", "g1",pegBoard);
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg(BMLGestureSync.START.getId(), createTimePeg(0));
        tmu.setTimePeg(BMLGestureSync.END.getId(), createTimePeg(2));
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(0);
        tmu.play(1);
        verify(mockProcAnimation,times(1)).play(anyDouble());
    }
    
    @Test
    public void testCopy() throws MUPlayException
    {
        ProcAnimationGestureMU mu = pag.copy(mockAnimationPlayer);
        assertEquals(0.2,mu.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time,0.001);
        assertEquals(0.8,mu.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time,0.001);
        mu.setupTransitionUnits();
        mu.play(0.79);
        verify(mockProcAnimation,times(1)).play(eq(0.8,0.05));
    }
}
