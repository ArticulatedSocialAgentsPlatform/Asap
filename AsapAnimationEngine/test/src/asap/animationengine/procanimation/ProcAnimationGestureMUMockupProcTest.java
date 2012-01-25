package asap.animationengine.procanimation;

import java.util.Set;

import hmi.animation.VJoint;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
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
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockProcAnimation.copy(mockAnimationPlayer)).thenReturn(mockProcAnimation);
        when(mockProcAnimation.copy((VJoint)any())).thenReturn(mockProcAnimationCopy);
        
        pag = new ProcAnimationGestureMU();
        pag.setGestureUnit(mockProcAnimation);
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), 0.8, 1.0));
        pag.setAnimationPlayer(mockAnimationPlayer);
        pag.setupTransitionUnits();
        
        RestPose mockRestPose = mock(RestPose.class);
        MotionUnit mockRelaxMU = mock(MotionUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);        
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
        verify(mockProcAnimation,times(1)).play(eq(0.2,0.05));
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
        TimedMotionUnit tmu = pag.createTMU(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "bml1", "g1");
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg(BMLGestureSync.START.getId(), createTimePeg(0));
        tmu.setTimePeg(BMLGestureSync.END.getId(), createTimePeg(1));
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(0);
        tmu.play(0.79);
        verify(mockProcAnimation,times(1)).play(eq(0.8,0.05));
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
