package asap.bmlb.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.parser.BMLParser;
import hmi.util.Resources;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.bml.ext.bmlb.BMLBBMLBehaviorAttributes;
import asap.bml.ext.murml.MURMLFaceBehaviour;

/**
 * Unit test cases for a BML parser that can parse BMLB
 * @author hvanwelbergen
 *
 */
public class BMLBParserTest
{
    private static final int PARSE_TIMEOUT = 300;
    private BMLParser parser;
    private Resources res = new Resources("bmltest");    
    private BehaviourBlock block;
    //private BMLBBMLBehaviorAttributes bmltExt;
    
    @Before
    public void setup()
    {
        BMLInfo.addDescriptionExtension(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
        BMLInfo.supportedExtensions.add(MURMLFaceBehaviour.class);        
        parser = new BMLParser();
    }
    
    private void readXML(String file) throws IOException
    {
        BMLInfo.supportedExtensions.add(MURMLFaceBehaviour.class);
        parser.clear();
        block = new BehaviourBlock(new BMLBBMLBehaviorAttributes());        
        block.readXML(res.getReader(file));
        //bmltExt = block.getBMLBehaviorAttributeExtension(BMLBBMLBehaviorAttributes.class);
        parser.addBehaviourBlock(block);
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testMurmlFaceDesc()throws IOException
    {
        readXML("runtime/murml/murmlfacekeyframedescription.xml");
        Behaviour b = block.behaviours.get(0);
        assertEquals("face1", b.id);
        assertThat(b, instanceOf(MURMLFaceBehaviour.class));
    }
}
