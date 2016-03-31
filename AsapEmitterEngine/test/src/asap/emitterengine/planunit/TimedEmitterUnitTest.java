/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

/**
 * Unit test cases for the TimedEmitterUnitTest
 * @author welberge
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedEmitterUnitTest extends AbstractTimedPlanUnitTest
{
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedEmitterUnit tpu = new TimedEmitterUnit(bfm, bbPeg, bmlId, id, new StubEmitterUnit());
        tpu.resolveStartAndEndKeyPositions();
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tpu.setTimePeg("start", start);
        return tpu;
    }
    
    @Override
    @Ignore
    public void testSubsiding()
    {
        
    }
    
    @Override
    @Ignore
    public void testSetStrokePeg()
    {
        
    }
}
