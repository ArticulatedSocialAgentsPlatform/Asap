/*******************************************************************************
 *******************************************************************************/
package asap.realizertestutil.planunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitPlayer;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Generic test cases for classes implementing TimedPlanUnitPlayer
 * @author welberge
 */
public abstract class AbstractTimedPlanUnitPlayerTest
{
    protected FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    protected TimedPlanUnitPlayer tpp;
    
    protected abstract TimedPlanUnitPlayer createTimedPlanUnitPlayer();
    
    @Before
    public void setup()
    {
        tpp = createTimedPlanUnitPlayer();
    }
    
    @Test
    public void testPlayUnit() throws InterruptedException
    {
        StubPlanUnit stubUnit = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        stubUnit.setState(TimedPlanUnitState.LURKING);
        tpp.playUnit(stubUnit, 0);
        Thread.sleep(100);
        assertEquals(TimedPlanUnitState.IN_EXEC,stubUnit.getState());
    }
    
    @Test
    public void testPlayUnitWithException() throws InterruptedException, TimedPlanUnitPlayException
    {
        StubPlanUnit stubUnit = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        StubPlanUnit stubUnitSpy = spy(stubUnit);
        TimedPlanUnitPlayException ex = new TimedPlanUnitPlayException("",stubUnit);
        doThrow(ex).when(stubUnitSpy).playUnit(0d);
        stubUnitSpy.setState(TimedPlanUnitState.LURKING);
        
        tpp.playUnit(stubUnitSpy, 0);
        Thread.sleep(100);
        assertThat(tpp.getPlayExceptions(),IsIterableContainingInOrder.contains(ex));
        assertThat(tpp.getStopExceptions(),Matchers.<TimedPlanUnitPlayException>empty());
    }
    
    @Test
    public void testStopUnitWithException() throws InterruptedException, TimedPlanUnitPlayException
    {
        StubPlanUnit stubUnit = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1",0,1);
        StubPlanUnit stubUnitSpy = spy(stubUnit);
        TimedPlanUnitPlayException ex = new TimedPlanUnitPlayException("",stubUnit);
        doThrow(ex).when(stubUnitSpy).stopUnit(1.1d);
        stubUnitSpy.setState(TimedPlanUnitState.IN_EXEC);
        
        tpp.stopUnit(stubUnitSpy, 1.1);
        Thread.sleep(100);
        assertThat(tpp.getStopExceptions(),IsIterableContainingInOrder.contains(ex));
        assertThat(tpp.getPlayExceptions(),Matchers.<TimedPlanUnitPlayException>empty());
    }
    
    @Test
    public void testStopUnit() throws InterruptedException
    {
        StubPlanUnit stubUnit = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1",0,1);
        stubUnit.setState(TimedPlanUnitState.IN_EXEC);
        tpp.stopUnit(stubUnit, 1.1);
        Thread.sleep(100);
        assertEquals(TimedPlanUnitState.DONE,stubUnit.getState());
    }
}
