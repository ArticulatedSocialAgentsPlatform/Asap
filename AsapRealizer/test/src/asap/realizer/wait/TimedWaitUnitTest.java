/*******************************************************************************
 *******************************************************************************/
package asap.realizer.wait;

import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

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
    
    @Override //behavior does not subside
    public void testSubsiding()
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
