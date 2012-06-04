package asap.asaplivemocapengine.planunit;

import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.asaplivemocapengine.inputs.EulerInput;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.utils.EulerHeadEmbodiment;

/**
 * Unit tests for the RemoteHeadTMU
 * @author welberge
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class RemoteHeadTMUTest extends AbstractTimedPlanUnitTest
{
    private EulerInput mockEulerInput = mock(EulerInput.class);
    private EulerHeadEmbodiment mockHead = mock(EulerHeadEmbodiment.class);

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        RemoteHeadTMU tmu = new RemoteHeadTMU(mockEulerInput, mockHead, bfm, bbPeg, bmlId, id);
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tmu.setStartPeg(start);
        return tmu;
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    }
    
    @Override
    @Test
    public void testSubsiding() throws TimedPlanUnitPlayException
    {
        
    }    
}
