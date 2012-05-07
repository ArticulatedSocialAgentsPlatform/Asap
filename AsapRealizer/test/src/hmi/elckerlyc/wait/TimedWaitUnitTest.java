package hmi.elckerlyc.wait;

import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;

/**
 * Unit test cases for TimedWaitUnit
 * @author welberge
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedWaitUnitTest extends AbstractTimedPlanUnitTest
{

    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }

    @Override
    public void testSetStrokePeg() // XXX not valid for this behavior
    {

    }

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedWaitUnit twu = new TimedWaitUnit(bfm, bbPeg, bmlId, id);
        TimePeg startPeg = new TimePeg(bbPeg);
        startPeg.setGlobalValue(startTime);
        twu.setStartPeg(startPeg);
        return twu;
    }

}
