package asap.bmlb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;
import saiba.bml.core.BehaviourBlock;

import org.junit.Test;

import asap.bml.ext.bmlb.BMLBBMLBehaviorAttributes;
import asap.bml.ext.bmlb.BMLBComposition;

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
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBComposition.MERGE, BMLBComposition.convert(block.getSchedulingMechanism()));
    }
    
    @Test
    public void testChunkAfter()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" composition=\"CHUNK-AFTER(bml2,bml3)\"/>";        
        block.readXML(bmlString);
        assertEquals(BMLBComposition.CHUNK_AFTER, block.getSchedulingMechanism());
        assertThat(bbmlbExt.getChunkAfterList(), hasItems("bml2", "bml3"));
    }
}
