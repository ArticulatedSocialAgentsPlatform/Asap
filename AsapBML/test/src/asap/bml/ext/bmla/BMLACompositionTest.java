package asap.bml.ext.bmla;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;

/**
 * 
 * @author hvanwelbergen
 */
public class BMLACompositionTest
{
    BMLABMLBehaviorAttributes bbmlbExt = new BMLABMLBehaviorAttributes();
    BehaviourBlock block = new BehaviourBlock(bbmlbExt);

    @Test
    public void testMerge()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\"/>";
        block.readXML(bmlString);
        assertEquals(CoreComposition.MERGE, block.getComposition());
    }

    @Test
    public void testChunkAfter()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" " +
        		"bmla:chunkAfter=\"bml2,bml3\"/>";
        block.readXML(bmlString);
        assertThat(bbmlbExt.getChunkAfterList(), hasItems("bml2", "bml3"));
    }
    
    @Test
    public void testPrependBefore()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" " +
        		"bmla:prependBefore=\"bml2,bml3\"/>";
        block.readXML(bmlString);
        assertThat(bbmlbExt.getPrependBeforeList(), hasItems("bml2", "bml3"));
    }
    
    @Test
    public void testChunkBefore()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" " +
                "bmla:chunkBefore=\"bml2,bml3\"/>";
        block.readXML(bmlString);
        assertThat(bbmlbExt.getChunkBeforeList(), hasItems("bml2", "bml3"));
    }
    
    @Test
    public void testDependencies()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" " +
                "bmla:chunkBefore=\"bml2,bml3\" bmla:prependBefore=\"bml4\" bmla:onStart=\"bml5\" bmla:appendAfter=\"bml6\" bmla:chunkAfter=\"bml7\"/>";
        block.readXML(bmlString);
        assertThat(bbmlbExt.getOtherBlockDependencies(), hasItems("bml2", "bml3", "bml4","bml5","bml6","bml7"));
    }
    
    @Test
    public void testWriteAttributes()
    {
        String bmlString = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "+
                "id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" " +                
                "bmla:interrupt=\"bml11,bml12\" "+
                "bmla:preplan=\"true\" bmla:onStart=\"bml9,bml10\" " +
        		"bmla:chunkBefore=\"bml2,bml3\" bmla:prependBefore=\"bml4\" bmla:appendAfter=\"bml6\" bmla:chunkAfter=\"bml7\"/>";
        block.readXML(bmlString);
        String bmlBlock = block.toBMLString();
        
        BMLABMLBehaviorAttributes bbmlbExt2 = new BMLABMLBehaviorAttributes();
        BehaviourBlock block2 = new BehaviourBlock(bbmlbExt2);
        block2.readXML(bmlBlock);
        assertThat(bbmlbExt2.getChunkBeforeList(), hasItems("bml2", "bml3"));
        assertThat(bbmlbExt2.getChunkAfterList(), hasItems("bml7"));
        assertThat(bbmlbExt2.getAppendAfterList(), hasItems("bml6"));
        assertThat(bbmlbExt2.getPrependBeforeList(), hasItems("bml4"));
        assertThat(bbmlbExt2.getInterruptList(), hasItems("bml11","bml12"));
        assertThat(bbmlbExt2.getOnStartList(), hasItems("bml9","bml10"));
        assertTrue(bbmlbExt2.isPrePlanned());
    }
}
