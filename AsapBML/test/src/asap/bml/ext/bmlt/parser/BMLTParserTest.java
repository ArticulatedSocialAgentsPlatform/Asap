package asap.bml.ext.bmlt.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;
import saiba.bml.parser.BMLParser;
import hmi.util.Resources;
import static asap.bml.parser.ParserTestUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;

import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.bml.ext.bmlt.BMLTSchedulingMechanism;
import asap.bml.ext.maryxml.MaryXMLBehaviour;
import asap.bml.parser.ExpectedConstraint;
import asap.bml.parser.ExpectedSync;

/**
 * Testcases for BMLT specific parsing
 * @author hvanwelbergen
 *
 */
public class BMLTParserTest
{
    private static final int PARSE_TIMEOUT = 300;
    private BMLParser parser;
    private Resources res = new Resources("bmltest");    
    private BehaviourBlock block;
    private BMLTBMLBehaviorAttributes bmltExt;
    
    static
    {
        BMLTInfo.init();
    }
    
    private void readXML(String file) throws IOException
    {
        BMLInfo.supportedExtensions.add(MaryXMLBehaviour.class);
        parser.clear();
        block = new BehaviourBlock(new BMLTBMLBehaviorAttributes());        
        block.readXML(res.getReader(file));
        bmltExt = block.getBMLBehaviorAttributeExtension(BMLTBMLBehaviorAttributes.class);
        parser.addBehaviourBlock(block);
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testSpeechNoAppend()throws IOException
    {
        readXML("empty.xml");
        assertEquals(0, bmltExt.getAppendList().size());
    }

    @Test(timeout = PARSE_TIMEOUT)
    public void testSpeechNoAppendAfter()throws IOException
    {
        readXML("bmlt/emptyappendafter.xml");
        assertEquals(0, bmltExt.getAppendList().size());
    }

    
    

    @Test(timeout = PARSE_TIMEOUT)
    public void testAppendAfter()throws IOException
    {
        readXML("bmlt/testspeechappendafter.xml");
        assertEquals(BMLTSchedulingMechanism.APPEND_AFTER, block.getSchedulingMechanism());
        assertEquals(3, bmltExt.getAppendList().size());
        assertThat(bmltExt.getAppendList(), hasItems("bml1", "bml2", "bml3"));
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testParameterValueChange() throws IOException
    {
        readXML("bmlt/parametervaluechange.xml");
        List<ExpectedConstraint> expectedConstraints = new ArrayList<ExpectedConstraint>();

        ExpectedConstraint expected1 = new ExpectedConstraint();
        expected1.expectedSyncs.add(new ExpectedSync("bml1", "pvc1", "start", 0));
        expected1.expectedSyncs.add(new ExpectedSync("bml1", "speech1", "start", 0));
        expectedConstraints.add(expected1);

        ExpectedConstraint expected2 = new ExpectedConstraint();
        expected2.expectedSyncs.add(new ExpectedSync("bml1", "pvc1", "end", 0));
        expected2.expectedSyncs.add(new ExpectedSync("bml1", "speech1", "end", 0));
        expected2.expectedSyncs.add(new ExpectedSync("bml1", "speech2", "start", 0));
        expectedConstraints.add(expected2);

        assertEqualConstraints(expectedConstraints, parser.getConstraints());
    }   
    
    @Before
    public void setup()
    {
        BMLInfo.supportedExtensions.add(MaryXMLBehaviour.class);
        parser = new BMLParser();
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testMaryDesc()throws IOException
    {
        readXML("runtime/bmlt/interruptbehaviormary/maryspeech.xml");
        Behaviour b = block.behaviours.get(0);
        assertEquals("s1", b.id);
        assertThat(b, instanceOf(MaryXMLBehaviour.class));
    }

    @Test(timeout = PARSE_TIMEOUT)
    public void testBMLTAudioUnit()throws IOException
    {
        readXML("bmlt/bmltaudio.xml");
        assertEquals(1, parser.getBehaviours().size());
        BehaviourBlock bb = parser.getBehaviourBlocks().get(0);
        Behaviour b = bb.behaviours.get(0);
        assertEquals("audio1", b.id);
        assertEquals("audio/audience.wav", b.getStringParameterValue("fileName"));
        assertThat(b, instanceOf(BMLTAudioFileBehaviour.class));
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testSpeechNoOnstart() throws IOException
    {
        readXML("empty.xml");
        assertEquals(0, bmltExt.getOnStartList().size());
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testSpeechOnstart()throws IOException
    {
        readXML("bmlt/testspeechonstart.xml");
        assertEquals(3, bmltExt.getOnStartList().size());
        assertThat(bmltExt.getOnStartList(), hasItems("bml1", "bml2", "bml3"));
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testTightMerge()throws IOException
    {
        readXML("bmlt/testtightmerge.xml");
        BehaviourBlock bb = parser.getBehaviourBlocks().get(0);
        assertEquals(CoreComposition.MERGE, bb.getSchedulingMechanism());
        assertTrue(bmltExt.allowExternalRefs());

        List<ExpectedConstraint> expectedConstraints = new ArrayList<ExpectedConstraint>();

        ExpectedConstraint expected1 = new ExpectedConstraint();
        expected1.expectedSyncs.add(new ExpectedSync("bml2", "pvc1", "start", 0));
        expected1.expectedSyncs.add(new ExpectedSync("bml1", "speech1", "start", 0));
        expectedConstraints.add(expected1);

        ExpectedConstraint expected2 = new ExpectedConstraint();
        expected2.expectedSyncs.add(new ExpectedSync("bml2", "pvc1", "end", 0));
        expected2.expectedSyncs.add(new ExpectedSync("bml1", "speech1", "end", 0));
        expectedConstraints.add(expected2);
        assertEqualConstraints(expectedConstraints, parser.getConstraints());
    }
    
    @Test(timeout = PARSE_TIMEOUT)
    public void testSpeechPreplan()throws IOException
    {
        readXML("bmlt/testspeechpreplan.xml");
        assertEquals(true, bmltExt.isPrePlanned());
    }

    @Test(timeout = PARSE_TIMEOUT)
    public void testInterrupt()throws IOException
    {
        readXML("bmlt/testspeechinterrupt.xml");
        assertEquals(3, bmltExt.getInterruptList().size());
        assertThat(bmltExt.getInterruptList(), hasItems("bml1", "bml2", "bml3"));
    }

    @Test(timeout = PARSE_TIMEOUT)
    public void testSimpleInterrupt()throws IOException
    {
        readXML("bmlt/testinterruptbml1.xml");
        assertEquals(1, bmltExt.getInterruptList().size());
        assertThat(bmltExt.getInterruptList(), hasItem("bml1"));
    }
}
