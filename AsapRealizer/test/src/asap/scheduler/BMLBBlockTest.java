package asap.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.bml.BMLGestureSync;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Unit testcases for the BMLBBlock
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLScheduler.class)
public class BMLBBlockTest
{
    private static final String BLOCKID = "block1";
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private final static ImmutableMap<String,TimedPlanUnitState> EMPTY_UPDATE_MAP = new ImmutableMap.Builder<String,TimedPlanUnitState>().build();
    
    @Test
    public void testUpdate()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.LURKING);
        block.update(EMPTY_UPDATE_MAP);
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }

    @Test
    public void testNoUpdateInPending()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.PENDING);
        block.update(EMPTY_UPDATE_MAP);
        verify(mockScheduler, times(0)).startBlock(BLOCKID);
    }

    @Test
    public void testNoUpdateWhenAppending()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, Sets.newHashSet("bml2"), new HashSet<String>(), new HashSet<String>());
        block.setState(TimedPlanUnitState.LURKING);        
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.IN_EXEC));
        verify(mockScheduler, times(0)).startBlock(BLOCKID);
    }

    @Test
    public void testFinishEmptyBlock()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.DONE, block.getState());
        verify(mockScheduler, times(1)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testNotFinishNonEmptyBlock()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.IN_EXEC, block.getState());
        verify(mockScheduler, times(0)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testFinishNonEmptyBlock()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
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
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.IN_EXEC);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.behaviorProgress("beh1", BMLGestureSync.RELAX.getId());
        block.update(EMPTY_UPDATE_MAP);        
        assertEquals(TimedPlanUnitState.SUBSIDING, block.getState());
    }

    @Test
    public void testSubsidingToDone()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler);
        block.setState(TimedPlanUnitState.SUBSIDING);
        when(mockScheduler.getBehaviours(BLOCKID)).thenReturn(Sets.newHashSet("beh1"));
        block.behaviorProgress("beh1", BMLGestureSync.END.getId());
        block.update(EMPTY_UPDATE_MAP);
        assertEquals(TimedPlanUnitState.DONE, block.getState());
        verify(mockScheduler, times(1)).blockStopFeedback(BLOCKID);
    }

    @Test
    public void testChunk()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, new HashSet<String>(), new HashSet<String>(), Sets.newHashSet("bml2"));
        block.setState(TimedPlanUnitState.LURKING);
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.SUBSIDING));
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }
    
    @Test
    public void testChunkGone()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, new HashSet<String>(), new HashSet<String>(), Sets.newHashSet("bml2"));
        block.setState(TimedPlanUnitState.LURKING);
        block.update(EMPTY_UPDATE_MAP);
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }
    
    @Test
    public void testNotChunk()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, new HashSet<String>(), new HashSet<String>(), Sets.newHashSet("bml2"));
        block.setState(TimedPlanUnitState.LURKING);
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.IN_EXEC));
        verify(mockScheduler, times(0)).startBlock(BLOCKID);
    }
    
    @Test
    public void testChunkAndAppend()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, new HashSet<String>(), Sets.newHashSet("bml3"), Sets.newHashSet("bml2"));
        block.setState(TimedPlanUnitState.LURKING);
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.SUBSIDING,"bml3",TimedPlanUnitState.DONE));
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }
    
    @Test
    public void testChunkAndNoAppend()
    {
        BMLBBlock block = new BMLBBlock(BLOCKID, mockScheduler, new HashSet<String>(), Sets.newHashSet("bml3"), Sets.newHashSet("bml2"));
        block.setState(TimedPlanUnitState.LURKING);
        block.update(ImmutableMap.of("bml2", TimedPlanUnitState.SUBSIDING,"bml3",TimedPlanUnitState.SUBSIDING));
        verify(mockScheduler, times(1)).startBlock(BLOCKID);
    }
}
