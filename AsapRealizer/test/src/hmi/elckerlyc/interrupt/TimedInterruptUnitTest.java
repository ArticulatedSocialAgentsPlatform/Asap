package hmi.elckerlyc.interrupt;

import java.util.HashSet;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.BMLScheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableSet;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test cases for the TimedInterruptUnit
 * @author welberge
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class, BMLBlockManager.class})
public class TimedInterruptUnitTest extends AbstractTimedPlanUnitTest
{
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private static final String TARGET="bml2";
    

    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedInterruptUnit tiu = new TimedInterruptUnit(bfm, bbPeg, bmlId, id, TARGET, mockScheduler);
        HashSet<String> behaviours = new HashSet<String>();
        behaviours.add("beh1");
        when(mockScheduler.getBehaviours(TARGET)).thenReturn(behaviours);
        when(mockScheduler.getSyncsPassed(TARGET, "beh1")).thenReturn(new ImmutableSet.Builder<String>().build());
        
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tiu.setStartPeg(start);
        
        //tiu.addInterruptSpec(new InterruptSpec("beh1","sync1",new HashSet<String>()));        
        return tiu;
    }

    @Test
    public void testInterrupt() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG,"id1","bml1",0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(0);
        tpu.play(0);        
        verify(mockScheduler,times(1)).interruptBehavior("bml2", "beh1");
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    }    
}
