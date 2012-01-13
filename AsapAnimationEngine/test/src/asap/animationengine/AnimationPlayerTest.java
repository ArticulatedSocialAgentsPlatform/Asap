package asap.animationengine;

import static org.junit.Assert.assertEquals;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.bml.feedback.ListBMLExceptionListener;
import hmi.physics.PhysicalHumanoid;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.mixed.MixedSystem;
import asap.animationengine.motionunit.TimedMotionUnit;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.*;

/**
 * Test cases for the AnimationPlayer 
 * @author welberge
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TimedMotionUnit.class,BMLBlockManager.class})
public class AnimationPlayerTest
{
    private AnimationPlayer animationPlayer;
    private List<BMLExceptionFeedback> beList;

    private PhysicalHumanoid mockPhysicalHumanoid = mock(PhysicalHumanoid.class);
    private TimedMotionUnit mockTimedMotionUnit = mock(TimedMotionUnit.class);
    private BMLBlockManager mockBMLBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBMLBlockManager,"character1");
    
    private PlanManager planManager = new PlanManager();
    
    @Before
    public void setup()
    {
        ArrayList<MixedSystem> m = new ArrayList<MixedSystem>();
        float g[] = { 0, 0, 0 };
        MixedSystem mix = new MixedSystem(g, mockPhysicalHumanoid);
        m.add(mix);
        
        
        animationPlayer = new AnimationPlayer(HanimBody.getLOA1HanimBody(),
                HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody(),
                m, 0.01f,
                new SingleThreadedPlanPlayer(fbManager,planManager)                
                );
        beList = new ArrayList<BMLExceptionFeedback>();
        fbManager.addExceptionListener(new ListBMLExceptionListener(beList));
    }    

    @Test
    public void testPlanUnitException() throws TimedPlanUnitPlayException
    {
        
        when(mockTimedMotionUnit.getId()).thenReturn("id1");
        when(mockTimedMotionUnit.getBMLId()).thenReturn("id1");
        when(mockTimedMotionUnit.getStartTime()).thenReturn(0d);
        when(mockTimedMotionUnit.getEndTime()).thenReturn(1d);
        when(mockTimedMotionUnit.getState()).thenReturn(TimedPlanUnitState.IN_EXEC);
        when(mockTimedMotionUnit.isPlaying()).thenReturn(true);    
        doThrow(new TimedPlanUnitPlayException("", mockTimedMotionUnit)).when(mockTimedMotionUnit).play(anyDouble());       
        
        planManager.addPlanUnit(mockTimedMotionUnit);                    
        animationPlayer.play(0);
        verify(mockTimedMotionUnit,times(1)).play(anyDouble());
        assertEquals(1, beList.size());        
    }
}
