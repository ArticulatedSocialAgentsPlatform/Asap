package asap.realizertester;


import hmi.realizertester.AbstractElckerlycRealizerTest;

import java.io.IOException;

import org.junit.Test;

import bml.bmlinfo.DefaultSyncPoints;

/**
 * Asap/MURML specific testcases for the AsapRealizer 
 * @author hvanwelbergen
 *
 */
public abstract class AbstractASAPRealizerTest extends AbstractElckerlycRealizerTest
{
    @Test
    public void testChunk() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("asap/chunking/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunking/secondchunk.xml");
        
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1","bml2");
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncsInOrder("bml1", "g1", DefaultSyncPoints.getDefaultSyncPoints("gesture"));
        realizerHandler.assertSyncsInOrder("bml2", "g1", DefaultSyncPoints.getDefaultSyncPoints("gesture"));
        
        realizerHandler.assertLinkedSyncs("bml1", "g1", "relax", "bml2", "g1", "start");        
    }
    
    @Test
    public void testChunkConflictResolution() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("asap/chunkingconflictres/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunkingconflictres/secondchunk.xml");
        
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1","bml2");
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertLinkedSyncs("bml1", "g1", "relax", "bml2", "g1", "start");
        realizerHandler.assertLinkedSyncs("bml1", "g1", "relax", "bml1", "g1", "end");
    }
    
    @Test
    public void testMURMLFace() throws IOException, InterruptedException
    {
        String bmlString = readTestFile("murml/murmlfacekeyframe.xml");
        realizerHandler.performBML(bmlString);
        
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1");
        realizerHandler.assertSyncsInOrder("bml1", "face1", DefaultSyncPoints.getDefaultSyncPoints("face"));
    }
    
    @Test
    public void testMURMLBody() throws IOException, InterruptedException
    {
        String bmlString = readTestFile("murml/murmlbodykeyframe.xml");
        realizerHandler.performBML(bmlString);
        
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1");
        realizerHandler.assertSyncsInOrder("bml1", "gesture1", DefaultSyncPoints.getDefaultSyncPoints("gesture"));
    }
}
