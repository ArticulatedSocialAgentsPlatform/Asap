package hmi.elckerlyc.scheduler;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
/**
 * Unit tests for the SmartBodySchedulingStrategy
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLScheduler.class)
public class SmartBodySchedulingStrategyTest
{
    private SmartBodySchedulingStrategy schedulingStrategy = new SmartBodySchedulingStrategy(new PegBoard());
    private BMLBlockPeg bbPeg = new BMLBlockPeg("bml1",0);
    BMLScheduler mockScheduler = mock(BMLScheduler.class);
    
    @Test
    public void testScheduleEmpty()
    {
        BehaviourBlock bb = new BehaviourBlock();
        bb.id = "bml1";
        schedulingStrategy.schedule(CoreComposition.MERGE, bb, bbPeg, mockScheduler, 0);        
    }
}
