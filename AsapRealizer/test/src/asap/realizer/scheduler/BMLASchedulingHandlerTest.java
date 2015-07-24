/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.BehaviourBlock;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for the BMLBandTSchedulingHandler
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLScheduler.class, BMLBlockManager.class })
public class BMLASchedulingHandlerTest
{
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private BMLBlockManager mockBMLBlockManager = mock(BMLBlockManager.class);
    private BMLBBlock mockBML1 = mock(BMLBBlock.class);
    private BMLBBlock mockBML2 = mock(BMLBBlock.class);
    private BMLBBlock mockBML3 = mock(BMLBBlock.class);
    private PegBoard pegBoard = new PegBoard();
    private BMLASchedulingHandler handler = new BMLASchedulingHandler(new SmartBodySchedulingStrategy(new PegBoard()), pegBoard);
    private static final double TIMING_PRECISION = 0.0001;

    @Before
    public void setup()
    {
        when(mockScheduler.getSchedulingTime()).thenReturn(0d);
        when(mockScheduler.getBMLBlockManager()).thenReturn(mockBMLBlockManager);
        when(mockBMLBlockManager.getBMLBlock("bml1")).thenReturn(mockBML1);
        when(mockBMLBlockManager.getBMLBlock("bml2")).thenReturn(mockBML2);
        when(mockBMLBlockManager.getBMLBlock("bml3")).thenReturn(mockBML3);
        when(mockBML1.getState()).thenReturn(TimedPlanUnitState.LURKING);
        when(mockBML2.getState()).thenReturn(TimedPlanUnitState.LURKING);
        when(mockBML3.getState()).thenReturn(TimedPlanUnitState.LURKING);
    }

    private BehaviourBlock createBehaviourBlock(String id)
    {
        return createBehaviourBlock(id, "");
    }

    private BehaviourBlock createBehaviourBlock(String id, String content)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLABMLBehaviorAttributes(), new BMLABMLBehaviorAttributes());
        bb.readXML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"" + id + "\"" + content + "/>");
        return bb;
    }

    private void assertBlockPegAdded(String id, double time)
    {
        ArgumentCaptor<BMLBlockPeg> blockPegArgument = ArgumentCaptor.forClass(BMLBlockPeg.class);
        verify(mockScheduler, times(1)).addBMLBlockPeg(blockPegArgument.capture());
        assertEquals(id, blockPegArgument.getValue().getId());
        assertEquals(time, blockPegArgument.getValue().getValue(), TIMING_PRECISION);
    }

    private BMLBBlock captureBMLBlock()
    {
        ArgumentCaptor<BMLBBlock> bmlBlockArgument = ArgumentCaptor.forClass(BMLBBlock.class);
        verify(mockScheduler, times(1)).addBMLBlock(bmlBlockArgument.capture());
        return bmlBlockArgument.getValue();
    }

    @Test
    public void testMergeBlock()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1");
        handler.schedule(bb, mockScheduler, 0);

        BMLBBlock block = captureBMLBlock();
        assertEquals("bml1", block.getBMLId());
        assertThat(block, instanceOf(BMLBBlock.class));
        assertBlockPegAdded("bml1", 0d);
        verify(mockScheduler, times(1)).startBlock("bml1", 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAppendAfter()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1", "composition=\"APPEND-AFTER(bml2,bml3)\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2", "bml3"));
        when(mockScheduler.predictEndTime((Set<String>) any())).thenReturn(3d);
        handler.schedule(bb, mockScheduler, 0);

        BMLBBlock block = captureBMLBlock();
        assertEquals("bml1", block.getBMLId());
        assertThat(block, instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING, block.getState());
        assertBlockPegAdded("bml1", 3d);
        verify(mockScheduler, times(1)).planningStart("bml1", 3d);
        verify(mockScheduler, times(1)).planningFinished(eq(bb), eq(3d), anyDouble());
    }

    @Test
    public void testAppendAfterFinishedBlocks()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1", "composition=\"APPEND-AFTER(bml2,bml3)\"");
        handler.schedule(bb, mockScheduler, 0);

        BMLBBlock block = captureBMLBlock();
        assertEquals("bml1", block.getBMLId());
        assertThat(block, instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING, block.getState());
        assertBlockPegAdded("bml1", 0d);
        verify(mockScheduler, times(1)).planningStart("bml1", 0d);
        verify(mockScheduler, times(1)).planningFinished(eq(bb), eq(0d), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testChunkAfter()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1", " xmlns:bmla=\"http://www.asap-project.org/bmla\" bmla:chunkAfter=\"bml2,bml3\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2", "bml3"));
        when(mockScheduler.predictSubsidingTime((Set<String>) any())).thenReturn(3d);
        handler.schedule(bb, mockScheduler, 0);

        BMLBBlock block = captureBMLBlock();
        assertEquals("bml1", block.getBMLId());
        assertThat(block, instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING, block.getState());
        verify(mockScheduler, times(1)).planningStart("bml1", 3d);
        verify(mockScheduler, times(1)).planningFinished(eq(bb), eq(3d), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAppendAfterBMLA()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1", " xmlns:bmla=\"http://www.asap-project.org/bmla\" bmla:chunkAfter=\"bml2,bml3\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2", "bml3"));
        when(mockScheduler.predictEndTime((Set<String>) any())).thenReturn(3d);
        handler.schedule(bb, mockScheduler, 0);

        BMLBBlock block = captureBMLBlock();
        assertEquals("bml1", block.getBMLId());
        assertThat(block, instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING, block.getState());
        assertBlockPegAdded("bml1", 3d);
        verify(mockScheduler, times(1)).planningStart("bml1", 3d);
        verify(mockScheduler, times(1)).planningFinished(eq(bb), eq(3d), anyDouble());
    }

    @Test
    public void testPrependBMLA()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1",
                " xmlns:bmla=\"http://www.asap-project.org/bmla\" bmla:prependBefore=\"bml2,bml3\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2", "bml3"));
        handler.schedule(bb, mockScheduler, 0);
        verify(mockBML2).addAppendTarget("bml1");
        verify(mockBML3).addAppendTarget("bml1");
    }

    @Test
    public void testPrependChunkBMLA()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1", " xmlns:bmla=\"http://www.asap-project.org/bmla\" bmla:chunkBefore=\"bml2,bml3\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2", "bml3"));
        handler.schedule(bb, mockScheduler, 0);
        verify(mockBML2).addChunkTarget("bml1");
        verify(mockBML3).addChunkTarget("bml1");
    }
}
