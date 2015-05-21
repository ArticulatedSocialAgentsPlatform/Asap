/*******************************************************************************
 *******************************************************************************/
package asap.realizer.activate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

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
