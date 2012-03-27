package asap.realizertester;


import hmi.bml.core.FaceBehaviour;
import hmi.bml.core.GestureBehaviour;
import hmi.realizertester.AbstractElckerlycRealizerTest;

import java.io.IOException;
import org.junit.Test;

public abstract class AbstractASAPRealizerTest extends AbstractElckerlycRealizerTest
{
    @Test
    public void testChunk() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("asap/chunking/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunking/secondchunk.xml");
        
        realizerPort.performBML(bmlString1);
        realizerPort.performBML(bmlString2);
        
        waitForBMLEndFeedback("bml1");
        waitForBMLEndFeedback("bml2");
        assertNoExceptions();
        assertNoWarnings();
        assertBlockStartAndStopFeedbacks("bml1","bml2");
        assertNoDuplicateFeedbacks();
        assertAllBMLSyncsInBMLOrder("bml1", "g1", GestureBehaviour.getDefaultSyncPoints());
        assertAllBMLSyncsInBMLOrder("bml2", "g1", GestureBehaviour.getDefaultSyncPoints());
        
        assertLinkedSyncs("bml1", "g1", "relax", "bml2", "g1", "start");        
    }
    
    @Test
    public void testChunkConflictResolution() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("asap/chunkingconflictres/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunkingconflictres/secondchunk.xml");
        
        realizerPort.performBML(bmlString1);
        realizerPort.performBML(bmlString2);
        
        waitForBMLEndFeedback("bml1");
        waitForBMLEndFeedback("bml2");
        
        assertNoExceptions();
        assertNoWarnings();
        assertBlockStartAndStopFeedbacks("bml1","bml2");
        assertNoDuplicateFeedbacks();
        assertLinkedSyncs("bml1", "g1", "relax", "bml2", "g1", "start");
        assertLinkedSyncs("bml1", "g1", "relax", "bml1", "g1", "end");
    }
    
    @Test
    public void testMURMLFace() throws IOException, InterruptedException
    {
        String bmlString = readTestFile("murml/murmlfacekeyframe.xml");
        realizerPort.performBML(bmlString);
        
        waitForBMLEndFeedback("bml1");
        assertNoExceptions();
        assertNoWarnings();
        assertBlockStartAndStopFeedbacks("bml1");
        assertAllBMLSyncsInBMLOrder("bml1", "face1", FaceBehaviour.getDefaultSyncPoints());
    }
}
