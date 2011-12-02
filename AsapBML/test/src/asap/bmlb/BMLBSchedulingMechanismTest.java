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
public class BMLBSchedulingMechanismTest
{
    BMLBBMLBehaviorAttributes bbmlbExt = new BMLBBMLBehaviorAttributes();
    BehaviourBlock block = new BehaviourBlock(bbmlbExt);
    
    @Test 
    public void testMerge()
    {
        String bmlString = "<bml id=\"bml1\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBSchedulingMechanism.MERGE, BMLBSchedulingMechanism.convert(block.getSchedulingMechanism()));
    }
    
    @Test
    public void testChunkAfter()
    {
        String bmlString = "<bml id=\"bml1\" scheduling=\"chunk-after(bml2,bml3)\"/>";        
        block.readXML(bmlString);
        assertEquals(BMLBSchedulingMechanism.CHUNK_AFTER, block.getSchedulingMechanism());
        assertThat(bbmlbExt.getChunkAfterList(), hasItems("bml2", "bml3"));
    }
}
