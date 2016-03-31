package asap.ipaacaeventengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitSetupException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

/**
 * Unit tests for TimedIpaacaMessageUnit
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class,BMLBlockManager.class})
public class TimedIpaacaMessageUnitTest extends AbstractTimedPlanUnitTest
{
    private MessageManager mockMessageManager = mock(MessageManager.class);
    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    }

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
            throws TimedPlanUnitSetupException
    {
        TimedIpaacaMessageUnit timu = new TimedIpaacaMessageUnit(bfm, bbPeg, bmlId, id, mockMessageManager, "cat1", new HashMap<String,String>());
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        timu.setStartPeg(start);        
        return timu;        
    }
}
