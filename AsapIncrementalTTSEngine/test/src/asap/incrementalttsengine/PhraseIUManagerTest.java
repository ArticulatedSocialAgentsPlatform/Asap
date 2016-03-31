/*******************************************************************************
 *******************************************************************************/
package asap.incrementalttsengine;

import static org.mockito.Mockito.mock;
import hmi.util.Resources;
import hmi.util.SystemClock;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.ChunkIU;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.parser.BMLParser;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.incrementalspeechengine.PhraseIUManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLASchedulingHandler;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;

public class PhraseIUManagerTest
{
    private BMLScheduler bmlScheduler;
    private DispatchStream dispatcher;
    private PegBoard pegBoard = new PegBoard();    
    @Before
    public void setup()
    {
        SystemClock clock = new SystemClock();
        clock.start();        
        bmlScheduler = new BMLScheduler("id1", new BMLParser(), NullFeedbackManager.getInstance(),clock ,
                new BMLASchedulingHandler(new SortedSmartBodySchedulingStrategy(pegBoard), pegBoard), new BMLBlockManager(), pegBoard);
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
    }
    
    @Test
    public void test() throws InterruptedException, IOException
    {
        final String CHUNK_CONTENT[] = { "Tomorrow at 10", "is the meeting with your brother", "and at two oclock", "you will go shopping",
                "and at eight", "is the gettogether", "in the bar" };
        PhraseIUManager man = new PhraseIUManager(dispatcher,"dfki-prudence-hsmm",bmlScheduler);
        
        final String CHUNK_CONTINUER_CONTENT[] = { "So, tomorrow at 10", "so, then is the meeting with your brother",
                "and at two", "so, then you will go shopping", "and at eight", "so, then is the gettogether",
                "that is in the bar" };
        
        for(String str:CHUNK_CONTENT)
        {
            man.playIU(new ChunkIU(str), null, mock(IncrementalTTSUnit.class));
        }
        Thread.sleep(3000);
        man.stopAfterOngoingPhoneme();
        for(String str:CHUNK_CONTINUER_CONTENT)
        {
            man.playIU(new ChunkIU(str), null, mock(IncrementalTTSUnit.class));
        }
        Thread.sleep(10000);
        dispatcher.waitUntilDone();
        dispatcher.close();
    }
}
