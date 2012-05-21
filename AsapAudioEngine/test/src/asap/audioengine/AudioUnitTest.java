package asap.audioengine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.bml.feedback.ListFeedbackListener;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;

/**
 * Test cases for TimedAbstractAudioUnit, executed by making a stub out of it and spying on some internal method calls
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class AudioUnitTest
{
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    
    private ListFeedbackListener feedbackListener;
    private List<BMLSyncPointProgressFeedback> feedbackList;
    
    @Before
    public void setup()
    {
        feedbackList = new ArrayList<BMLSyncPointProgressFeedback>(); 
        feedbackListener = new ListFeedbackListener(feedbackList);        
    }
    
    @Test
    public void testPrepState() throws TimedPlanUnitPlayException
    {
        StubAudioUnit stubAU = spy(new StubAudioUnit(fbManager,BMLBlockPeg.GLOBALPEG, null, "beh1","bml1"));
                
        fbManager.addFeedbackListener(feedbackListener);    
        
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);
        stubAU.setStart(tp);
        
        stubAU.start(1);
        stubAU.play(1);
        assertTrue(feedbackList.isEmpty());
        verify(stubAU,times(0)).playUnit(anyDouble());
        verify(stubAU,times(0)).startUnit(anyDouble());
    }
    
    @Test 
    public void testConstruct()
    {
        StubAudioUnit stubAU = new StubAudioUnit(fbManager,BMLBlockPeg.GLOBALPEG, null, "beh1","bml1");
        assertEquals(stubAU.getBMLId(),"bml1");
        assertEquals(stubAU.getId(),"beh1");
    }
    
    @Test 
    public void testGetEndTime()
    {
        StubAudioUnit stubAU = new StubAudioUnit(fbManager,BMLBlockPeg.GLOBALPEG, null, "beh1","bml1");
        assertEquals(TimePeg.VALUE_UNKNOWN,stubAU.getEndTime(),0.0001);
    }
    
    @Test
    public void testPlay() throws TimedPlanUnitPlayException
    {
        StubAudioUnit stubAU = spy(new StubAudioUnit(fbManager,BMLBlockPeg.GLOBALPEG, null, "beh1","bml1"));
        fbManager.addFeedbackListener(feedbackListener);    
        
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);
        stubAU.setStart(tp);
        
        stubAU.setState(TimedPlanUnitState.LURKING);
        stubAU.start(1);
        assertEquals(TimedPlanUnitState.IN_EXEC,stubAU.getState());        
        stubAU.play(1);
        assertTrue(feedbackList.isEmpty());        
        verify(stubAU,times(1)).playUnit(anyDouble());
        verify(stubAU,times(1)).startUnit(anyDouble());
    }
}
