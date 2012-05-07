package asap.audioengine;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.MultiThreadedPlanPlayer;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.bml.feedback.ListBMLExceptionListener;

import org.junit.Before;
import org.junit.Test;

import asap.audioengine.TimedAbstractAudioUnit;


public class AudioPlanPlayerAudioUnitStubTest
{
    private PlanManager<TimedAbstractAudioUnit> planManager = new PlanManager<TimedAbstractAudioUnit>();
    private MultiThreadedPlanPlayer<TimedAbstractAudioUnit> app; 
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    
    @Before
    public void setup()
    {
        app = new MultiThreadedPlanPlayer<TimedAbstractAudioUnit>(mockFeedbackManager,planManager);        
    }
    
    @Test
    public void testPlayAudioUnit() throws InterruptedException, TimedPlanUnitPlayException
    {
        List<BMLExceptionFeedback> beList = new ArrayList<BMLExceptionFeedback>();
        
        StubAudioUnit auStub = new StubAudioUnit(mockFeedbackManager,BMLBlockPeg.GLOBALPEG,null,"id1","bml1");
        
        auStub.setState(TimedPlanUnitState.LURKING);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        auStub.setStart(tpStart);
        
        app.addExceptionListener(new ListBMLExceptionListener(beList));        
        planManager.addPlanUnit(auStub);
        app.play(0);
        
        Thread.sleep(100);
        assertTrue(beList.size()==0);
        app.shutdown();
    }
}
