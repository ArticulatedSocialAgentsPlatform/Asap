package hmi.elckerlyc.planunit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;

import org.junit.Test;

/**
 * Tests the abstract class TimedAbstractPlanUnit through a stub that extends it.
 * @author Herwin
 */
public class TimedAbstractPlanUnitTest
{
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    
    private StubPlanUnit createStubPlanUnit(String behId, String bmlId)
    {
        return new StubPlanUnit(mockBmlFeedbackManager, BMLBlockPeg.GLOBALPEG, behId, bmlId);
    }
    
    @Test
    public void testSetup()
    {
        TimedAbstractPlanUnit apu = spy(createStubPlanUnit("id1", "bml1"));
        when(apu.getEndTime()).thenReturn(2d);
        
        apu.setState(TimedPlanUnitState.LURKING);
        assertTrue(apu.isLurking());        
        assertTrue(apu.getEndTime()==2);
        verify(apu,times(0)).getStartTime();
        verify(apu,times(1)).getEndTime();
    }
    
    @Test
    public void testPlayPastEnd() throws TimedPlanUnitPlayException
    {
        TimedAbstractPlanUnit apu = spy(createStubPlanUnit("id1", "bml1"));
        when(apu.getStartTime()).thenReturn(0d);
        when(apu.getEndTime()).thenReturn(2d);
        
        
        apu.setState(TimedPlanUnitState.IN_EXEC);
        apu.play(2);
        assertTrue(apu.isDone());
        verify(apu,times(0)).playUnit(anyDouble());
        verify(apu,times(1)).stopUnit(2d);
    }
    
    @Test
    public void testStart() throws TimedPlanUnitPlayException
    {
        TimedAbstractPlanUnit apu = spy(createStubPlanUnit("id1", "bml1"));
        when(apu.getStartTime()).thenReturn(0d);
        when(apu.getEndTime()).thenReturn(2d);
        
        apu.setState(TimedPlanUnitState.LURKING);
        apu.start(0);
        assertTrue(apu.getState()==TimedPlanUnitState.IN_EXEC);
        verify(apu).startUnit(0d);
    }
    
    @Test
    public void testStartPastEnd() throws TimedPlanUnitPlayException
    {
        TimedAbstractPlanUnit apu = spy(createStubPlanUnit("id1", "bml1"));
        when(apu.getStartTime()).thenReturn(0d);
        when(apu.getEndTime()).thenReturn(2d);
        
        apu.setState(TimedPlanUnitState.LURKING);
        boolean ex = false;
        try
        {
            apu.start(3);
        }
        catch (TimedPlanUnitPlayException e)
        {
            ex = true;
        }
        assertTrue(ex);
        assertTrue(apu.getState()==TimedPlanUnitState.DONE);
        verify(apu,times(0)).startUnit(anyDouble());
    }
    
    @Test
    public void testPastEnd()throws TimedPlanUnitPlayException
    {
        TimedAbstractPlanUnit apu = spy(createStubPlanUnit("id1", "bml1"));
        when(apu.getStartTime()).thenReturn(0d);
        when(apu.getEndTime()).thenReturn(2d);
        
        apu.setState(TimedPlanUnitState.IN_EXEC);
        apu.play(3);        
        assertTrue(apu.getState()==TimedPlanUnitState.DONE);
        verify(apu,times(0)).playUnit(anyDouble());
    }
}
