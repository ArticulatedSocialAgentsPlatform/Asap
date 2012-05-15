package asap.audioengine;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.ListBMLExceptionListener;

import org.junit.Before;
import org.junit.Test;

import asap.audioengine.TimedAbstractAudioUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;


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
