package asap.scheduler;

import java.util.Set;

import hmi.bml.core.BehaviourBlock;
import hmi.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.PegBoard;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlock;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.scheduler.SmartBodySchedulingStrategy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import asap.bmlb.BMLBBMLBehaviorAttributes;


/**
 * Unit test cases for the BMLBandTSchedulingHandler
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class})
public class BMLBandTSchedulingHandlerTest
{
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private BMLBandTSchedulingHandler handler = new BMLBandTSchedulingHandler(new SmartBodySchedulingStrategy(new PegBoard()));
    
    @Before
    public void setup()
    {
        when(mockScheduler.getSchedulingTime()).thenReturn(0d);
    }
    
    private BehaviourBlock createBehaviourBlock(String id)
    {
        return createBehaviourBlock(id,"");
    }
    
    private BehaviourBlock createBehaviourBlock(String id, String content)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLBBMLBehaviorAttributes(),new BMLTBMLBehaviorAttributes());
        bb.readXML("<bml id=\""+id+"\""+ content+"/>");
        return bb;
    }
    
    private void assertBlockPegAdded(String id, double time)
    {
        ArgumentCaptor<BMLBlockPeg> blockPegArgument = ArgumentCaptor.forClass(BMLBlockPeg.class);
        verify(mockScheduler,times(1)).addBMLBlockPeg(blockPegArgument.capture());
        assertEquals(id,blockPegArgument.getValue().getId());
        assertEquals(time,blockPegArgument.getValue().getValue(),0.001);        
    }
    
    private BMLBlock captureBMLBlock()
    {
        ArgumentCaptor<BMLBlock> bmlBlockArgument = ArgumentCaptor.forClass(BMLBlock.class);
        verify(mockScheduler,times(1)).addBMLBlock(bmlBlockArgument.capture());
        return bmlBlockArgument.getValue();    
    }
    
    @Test
    public void testMergeBlock()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1");         
        handler.schedule(bb, mockScheduler);
        
        BMLBlock block = captureBMLBlock();
        assertEquals("bml1",block.getBMLId());        
        assertThat(block,instanceOf(BMLBBlock.class));
        assertBlockPegAdded("bml1",0d);
        verify(mockScheduler,times(1)).startBlock("bml1");
    }
    
    @Test
    @SuppressWarnings("unchecked")    
    public void testAppendAfter()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1","composition=\"append-after(bml2,bml3)\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2","bml3"));
        when(mockScheduler.predictEndTime((Set<String>)any())).thenReturn(3d);
        handler.schedule(bb, mockScheduler);       
        
        
        BMLBlock block = captureBMLBlock();
        assertEquals("bml1",block.getBMLId());        
        assertThat(block,instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING,block.getState());
        assertBlockPegAdded("bml1",3d);
        verify(mockScheduler,times(1)).planningStart("bml1", 3d);
        verify(mockScheduler,times(1)).planningFinished(eq("bml1"), eq(3d),anyDouble());
    }
    
    @Test
    public void testAppendAfterFinishedBlocks()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1","composition=\"append-after(bml2,bml3)\"");
        handler.schedule(bb, mockScheduler);        
        
        BMLBlock block = captureBMLBlock();
        assertEquals("bml1",block.getBMLId());        
        assertThat(block,instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING,block.getState());
        assertBlockPegAdded("bml1",0d);
        verify(mockScheduler,times(1)).planningStart("bml1", 0d);
        verify(mockScheduler,times(1)).planningFinished(eq("bml1"), eq(0d),anyDouble());        
    }
    
    @Test
    @SuppressWarnings("unchecked")    
    public void testChunkAfter()
    {
        BehaviourBlock bb = createBehaviourBlock("bml1","composition=\"chunk-after(bml2,bml3)\"");
        when(mockScheduler.getBMLBlocks()).thenReturn(ImmutableSet.of("bml2","bml3"));
        when(mockScheduler.predictSubsidingTime((Set<String>)any())).thenReturn(3d);
        handler.schedule(bb, mockScheduler);   
        
        BMLBlock block = captureBMLBlock();
        assertEquals("bml1",block.getBMLId());        
        assertThat(block,instanceOf(BMLBBlock.class));
        assertEquals(TimedPlanUnitState.LURKING,block.getState());
        verify(mockScheduler,times(1)).planningStart("bml1", 3d);
        verify(mockScheduler,times(1)).planningFinished(eq("bml1"), eq(3d),anyDouble());
    }
}
