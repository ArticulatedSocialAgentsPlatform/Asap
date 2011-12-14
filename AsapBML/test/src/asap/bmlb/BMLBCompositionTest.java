package asap.bmlb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;
import hmi.bml.core.BehaviourBlock;

import org.junit.Test;

/**
 *  
 * @author hvanwelbergen
 */
public class BMLBCompositionTest
{
    BMLBBMLBehaviorAttributes bbmlbExt = new BMLBBMLBehaviorAttributes();
    BehaviourBlock block = new BehaviourBlock(bbmlbExt);
    
    @Test 
    public void testMerge()
    {
        String bmlString = "<bml id=\"bml1\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBComposition.MERGE, BMLBComposition.convert(block.getSchedulingMechanism()));
    }
    
    @Test
    public void testChunkAfter()
    {
        String bmlString = "<bml id=\"bml1\" composition=\"chunk-after(bml2,bml3)\"/>";        
        block.readXML(bmlString);
        assertEquals(BMLBComposition.CHUNK_AFTER, block.getSchedulingMechanism());
        assertThat(bbmlbExt.getChunkAfterList(), hasItems("bml2", "bml3"));
    }
}
