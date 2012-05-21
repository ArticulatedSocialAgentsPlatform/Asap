package asap.realizertester;


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;

import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.bml.ext.bmlt.BMLTParameterValueChangeBehaviour;
import asap.bml.ext.bmlt.BMLTTransitionBehaviour;
import asap.bml.feedback.BMLPredictionListener;
import bml.bmlinfo.DefaultSyncPoints;
import bml.realizertest.AbstractBML1RealizerTest;

/**
 * Asap/MURML specific testcases for the AsapRealizer 
 * @author hvanwelbergen
 *
 */
public abstract class AbstractASAPRealizerTest extends AbstractBML1RealizerTest implements BMLPredictionListener 
{
    private List<BMLPredictionFeedback> predictionList = Collections
    .synchronizedList(new ArrayList<BMLPredictionFeedback>());
    
    
    protected void clearFeedbackLists()
    {
        predictionList.clear();
        realizerHandler.clearFeedbackLists();
    }
    
    protected BMLBlockPredictionFeedback getBMLSchedulingFinishedFeedback(String bmlId)
    {
        synchronized (predictionList)
        {
            for (BMLPredictionFeedback bpf : predictionList)
            {
                for(BMLBlockPredictionFeedback bp:bpf.getBmlBlockPredictions())
                {
                    if (bp.getId().equals(bmlId))
                    {
                        if(bp.getGlobalEnd()!=BMLBlockPredictionFeedback.UNKNOWN_TIME)
                        {
                            return bp;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    protected synchronized void waitForBMLSchedulingFinishedFeedback(String bmlId) throws InterruptedException
    {
        while (!hasBMLFinishedFeedbacks(bmlId))
        {
            wait();
            
        }
    }
    
    protected boolean hasBMLFinishedFeedbacks(String bmlId)
    {
        if(getBMLSchedulingFinishedFeedback(bmlId)!=null)return true;
        return false;        
    }
    
    @Override
    public synchronized void prediction(BMLPredictionFeedback bpf)
    {
        predictionList.add(bpf);
        notifyAll();
    }
    
    @Test
    public void testParameterValueChangeWithTightMerge() throws IOException, InterruptedException
    {
        String bmlString1 = readTestFile("bmlt/tightmerge/speechandnod.xml");
        String bmlString2 = readTestFile("bmlt/tightmerge/volumechange.xml");
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();        
        realizerHandler.assertNoDuplicateSyncs();
        
        
        realizerHandler.assertSyncsInOrder("bml1", "speech1", "start","s1","end");
        realizerHandler.assertSyncsInOrder("bml1", "speech2", DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml2", "pvc1", BMLTParameterValueChangeBehaviour.
                getDefaultSyncPoints().toArray(new String[0]));
        
        realizerHandler.assertLinkedSyncs("bml1", "speech1", "start","bml2", "pvc1", "start");
        realizerHandler.assertLinkedSyncs("bml1", "speech1", "end", "bml2", "pvc1", "end");        
    }

    @Test
    public void testActivate() throws IOException, InterruptedException
    {
        String bmlString1 = readTestFile("bmlt/activate/testpreplannedspeech1.xml");
        String bmlString2 = readTestFile("bmlt/activate/testactivate.xml");
        realizerHandler.performBML(bmlString1);
        waitForBMLSchedulingFinishedFeedback("bml1");
        
        realizerHandler.performBML(bmlString2);
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.waitForBMLEndFeedback("bml1");        
        
        realizerHandler.assertSyncsInOrder("bml1", "speech1", DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml2", "nod1", DefaultSyncPoints.getDefaultSyncPoints("head"));
        realizerHandler.assertLinkedSyncs("bml1", "speech1", "start", "bml2", "nod1", "end");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
    }
    
    @Test
    public void testParameterValueChange() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/parametervaluechange.xml");
        realizerHandler.performBML(bmlString1);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();        
        realizerHandler.assertNoDuplicateSyncs();        
        
        realizerHandler.assertSyncsInOrder("bml1", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml1", "speech2",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml1", "pvc1", BMLTParameterValueChangeBehaviour.getDefaultSyncPoints().toArray(new String[0]));
        realizerHandler.assertLinkedSyncs("bml1", "speech1", "end", "bml1", "pvc1", "end");        
    }

    @Test
    public void testPreplan() throws InterruptedException, IOException
    {
        final double DELAY = 3;
        String bmlString1 = readTestFile("bmlt/preplan/testspeech1.xml");
        String bmlString2 = readTestFile("bmlt/preplan/testspeechinterrupt.xml");
        String bmlString3 = readTestFile("bmlt/preplan/testnod.xml");

        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        Thread.sleep((long) (DELAY * 1000));
        realizerHandler.performBML(bmlString3);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.waitForBMLEndFeedback("bml3");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
    }

    @Test
    public void testAppendAfter() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/appendafter/testspeech1.xml");
        String bmlString2 = readTestFile("bmlt/appendafter/testspeech2.xml");
        String bmlString3 = readTestFile("bmlt/appendafter/testspeech3.xml");
        String bmlString4 = readTestFile("bmlt/appendafter/testnodappendafter.xml");

        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString4);

        realizerHandler.waitForBMLEndFeedback("bml3");
        realizerHandler.waitForBMLEndFeedback("bml4");

        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncsInOrder("bml1", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml2", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml3", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncsInOrder("bml4", "nod1",DefaultSyncPoints.getDefaultSyncPoints("head"));
        
        realizerHandler.assertBlockStartLinkedToBlockStop("bml2", "bml1");
        realizerHandler.assertBlockStartLinkedToBlockStop("bml3", "bml2");
        realizerHandler.assertBlockStartLinkedToBlockStop("bml4", "bml2");
    }

    
    
    @Test
    public void testInterrupt() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/interruptblock/testlongspeechandnod.xml");
        String bmlString2 = readTestFile("bmlt/interruptblock/testinterruptbml1.xml");
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");

        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        
        realizerHandler.assertBlockStartLinkedToBlockStop("bml2","bml1");        
    }

    @Test
    public void testInterruptBehaviour() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/interrupt/testlongspeechandnod.xml");
        String bmlString2 = readTestFile("bmlt/interrupt/testspeechinterrupt.xml");
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        Thread.sleep(1000);
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        assertEquals(realizerHandler.getBMLPerformanceStopFeedback("bml1").timeStamp, 
                realizerHandler.getBMLPerformanceStartFeedback("bml2").timeStamp + 2, 0.2);
    }

    @Test
    public void testInterruptBehaviour2() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/interruptbehavior/testlongspeechandnod.xml");
        String bmlString2 = readTestFile("bmlt/interruptbehavior/testspeechinterrupt.xml");
        String bmlString3 = readTestFile("bmlt/interruptbehavior/testinterruptionspeech.xml");

        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.waitForBMLEndFeedback("bml3");

        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncLinkedToBlockStart("bml1", "speech1", "s1", "bml3");        
    }

    @Test
    public void testInterruptBehaviourRestart() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/interruptbehavior/testlongspeechandnod.xml");
        String bmlString2 = readTestFile("bmlt/interruptbehavior/testspeechinterrupt.xml");
        String bmlString3 = readTestFile("bmlt/interruptbehavior/testinterruptionspeech.xml");
        String bmlString4 = "<bml id=\"bmlrep\" composition=\"REPLACE\"/>";

        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.waitForBMLEndFeedback("bml3");
        realizerHandler.assertSyncsInOrder("bml3", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncLinkedToBlockStart("bml1", "speech1", "s1", "bml3");        

        // reset and do it again        
        realizerHandler.performBML(bmlString4);
        realizerHandler.waitForBMLEndFeedback("bmlrep");
        clearFeedbackLists();
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.waitForBMLEndFeedback("bml3");
        

        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateSyncs();
        realizerHandler.assertSyncsInOrder("bml3", "speech1",DefaultSyncPoints.getDefaultSyncPoints("speech"));
        realizerHandler.assertSyncLinkedToBlockStart("bml1", "speech1", "s1", "bml3");        
    }

    @Test
    public void testBMLTAudio() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/bmltaudio.xml");
        realizerHandler.performBML(bmlString1);
        realizerHandler.waitForBMLEndFeedback("bml1");
        
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncsInOrder("bml1", "audio1",BMLTAudioFileBehaviour.getDefaultSyncPoints().toArray(new String[0]));
    }
    
    @Test
    public void testTransition() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("bmlt/transition.xml");
        realizerHandler.performBML(bmlString1);
        realizerHandler.waitForBMLEndFeedback("bml1");
        Thread.sleep(1000);
        
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertSyncsInOrder("bml1", "transition1",BMLTTransitionBehaviour.getDefaultSyncPoints().toArray(new String[0]));
        realizerHandler.assertSyncsInOrder("bml1", "nod1",DefaultSyncPoints.getDefaultSyncPoints("head"));
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1");
        realizerHandler.assertFeedbackForBehaviors("bml1", "transition1","nod1");        
    }
    
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
