package asap.realizer.interrupt;

import java.util.HashSet;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.interrupt.TimedInterruptUnit;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;

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
