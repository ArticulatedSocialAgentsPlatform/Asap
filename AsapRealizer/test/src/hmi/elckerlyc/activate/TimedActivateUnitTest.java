package hmi.elckerlyc.activate;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for the TimedActivateUnit
 * @author welberge
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class,BMLBlockManager.class})
public class TimedActivateUnitTest extends AbstractTimedPlanUnitTest
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
        TimedActivateUnit tau = new TimedActivateUnit(bfm, bbPeg, bmlId, id, TARGET, mockScheduler);
        HashSet<String> behaviours = new HashSet<String>();
        behaviours.add("beh1");
        when(mockScheduler.getBehaviours(TARGET)).thenReturn(behaviours);
        when(mockScheduler.getSyncsPassed(TARGET, "beh1")).thenReturn(new ImmutableSet.Builder<String>().build());
        
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tau.setStartPeg(start);        
        return tau;
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    }   
    
    @Test
    public void testActivate() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnit(fbManager,BMLBlockPeg.GLOBALPEG,"a1","bml1",1);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(1.1);
        verify(mockScheduler,times(1)).activateBlock(TARGET, 1.1);
    }
}
