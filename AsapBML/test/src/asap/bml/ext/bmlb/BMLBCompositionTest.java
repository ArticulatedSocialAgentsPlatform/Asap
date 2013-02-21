package asap.bml.ext.bmlb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import saiba.bml.core.BehaviourBlock;

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
    
    @Test
    public void testPrepend()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" composition=\"PREPEND\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBComposition.PREPEND, block.getSchedulingMechanism());
    }
    
    @Test
    public void testPrependBefore()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" composition=\"PREPEND-BEFORE(bml2,bml3)\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBComposition.PREPEND_BEFORE, block.getSchedulingMechanism());
        assertThat(bbmlbExt.getPrependBeforeList(), hasItems("bml2", "bml3"));
    }
    
    @Test
    public void testChunkBefore()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" composition=\"CHUNK-BEFORE(bml2,bml3)\"/>";
        block.readXML(bmlString);
        assertEquals(BMLBComposition.CHUNK_BEFORE, block.getSchedulingMechanism());
        assertThat(bbmlbExt.getChunkBeforeList(), hasItems("bml2", "bml3"));
    }
}
