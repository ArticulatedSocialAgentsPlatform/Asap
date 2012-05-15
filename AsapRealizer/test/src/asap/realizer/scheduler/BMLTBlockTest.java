package asap.realizer.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import saiba.bml.BMLGestureSync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.BMLTBlock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Unit test cases for the BMLBlock
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLScheduler.class)
public class BMLTBlockTest
{
    private static final String BLOCKID = "block1";
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private final static ImmutableMap<String,TimedPlanUnitState> EMPTY_UPDATE_MAP = new ImmutableMap.Builder<String,TimedPlanUnitState>().build();
    
    @Test
    public void testUpdate()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.LURKING);
        block.update(EMPTY_UPDATE_MAP);
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }

    @Test
    public void testNoUpdateInPending()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.PENDING);
        block.update(EMPTY_UPDATE_MAP);
        verify(mockScheduler, times(0)).startBlock(BLOCKID);
    }

    @Test
    public void testNoUpdateWhenAppending()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler, Sets.newHashSet("bml2"), new HashSet<String>());
        block.setState(TimedPlanUnitState.LURKING);        
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.IN_EXEC));
        verify(mockScheduler, times(0)).startBlock(BLOCKID);
    }

    @Test
    public void testFinishEmptyBlock()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.DONE, block.getState());
        verify(mockScheduler, times(1)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testNotFinishNonEmptyBlock()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.IN_EXEC, block.getState());
        verify(mockScheduler, times(0)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testFinishNonEmptyBlock()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.behaviorProgress("beh1", BMLGestureSync.END.getId());
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.DONE, block.getState());
        verify(mockScheduler, times(1)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testSubsiding()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.behaviorProgress("beh1", BMLGestureSync.RELAX.getId());
        block.update(EMPTY_UPDATE_MAP);        
        assertEquals(TimedPlanUnitState.SUBSIDING, block.getState());
    }

    @Test
    public void testSubsidingToDone()
    {
        BMLTBlock block = new BMLTBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.SUBSIDING);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.behaviorProgress("beh1", BMLGestureSync.END.getId());
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.DONE, block.getState());
        verify(mockScheduler, times(1)).blockStopFeedback(BLOCKID);
    }    
}
