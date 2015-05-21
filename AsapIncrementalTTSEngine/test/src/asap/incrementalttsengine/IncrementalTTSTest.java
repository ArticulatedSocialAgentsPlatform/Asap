/*******************************************************************************
 *******************************************************************************/
package asap.incrementalttsengine;

import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.ChunkBasedInstallmentIU;
import inpro.incremental.unit.ChunkIU;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SysSegmentIU;
import inpro.synthesis.MaryAdapter;

import org.junit.Ignore;
import org.junit.Test;

import done.inpro.system.carchase.HesitatingSynthesisIU;

@Ignore
public class IncrementalTTSTest
{
    private static class MyWordUpdateListener implements IUUpdateListener
    {
        @Override
        public void update(IU updatedIU)
        {
            System.out.println("update " + updatedIU.toPayLoad());
            System.out.println("start " + updatedIU.startTime());

            for (IU we : updatedIU.groundedIn())
            {
                System.out.println("Phoneme: " + we.toPayLoad());
                System.out.println("Start: " + we.startTime());
                System.out.println("End: " + we.endTime());
                System.out.println("progress: " + we.getProgress());
            }

        }
    }

    //@Ignore
    @Test
    public void testVoice() throws InterruptedException
    {
        //System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.voice", "dfki-poppy-hsmm");
        //System.setProperty("inpro.tts.voice", "dfki-obadiah-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");

        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));

        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world.");
        dispatcher.playStream(installment.getAudio());
        Thread.sleep(20000);
    }
    
    @Ignore
    @Test
    public void testSentences() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));

        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Welcome ladies and gentleman.");
        for (IU word : installment.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(new MyWordUpdateListener());
        }
        dispatcher.playStream(installment.getAudio());
        Thread.sleep(20000);
    }
    
    @Ignore
    @Test
    public void testMultipleSentencesQuality() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        {
            ChunkBasedInstallmentIU installment1 = new ChunkBasedInstallmentIU(new ChunkIU("Morgen um 10"));
            ChunkIU installment2 = new ChunkIU("ist das treffen mit deinem Bruder");
            ChunkIU installment3 = new ChunkIU("und um zwei Uhr");
            ChunkIU installment4 = new ChunkIU("gehst du Einkaufen");
            ChunkIU installment5 = new ChunkIU("und abends ab acht");
            ChunkIU installment6 = new ChunkIU("ist der Stammtisch");
            ChunkIU installment7 = new ChunkIU("in der Kneipe.");
            dispatcher.playStream(installment1.getAudio());
            installment1.appendChunk(installment2);
            installment1.appendChunk(installment3);
            installment1.appendChunk(installment4);
            installment1.appendChunk(installment5);
            installment1.appendChunk(installment6);
            installment1.appendChunk(installment7);
        }
        Thread.sleep(20000);

        {
            HesitatingSynthesisIU installment1 = new HesitatingSynthesisIU("Morgen um 10");
            HesitatingSynthesisIU installment2 = new HesitatingSynthesisIU("ist das treffen mit deinem Bruder");
            HesitatingSynthesisIU installment3 = new HesitatingSynthesisIU("und um zwei Uhr");
            HesitatingSynthesisIU installment4 = new HesitatingSynthesisIU("gehst du Einkaufen");
            HesitatingSynthesisIU installment5 = new HesitatingSynthesisIU("und abends ab acht");
            HesitatingSynthesisIU installment6 = new HesitatingSynthesisIU("ist der Stammtisch");
            HesitatingSynthesisIU installment7 = new HesitatingSynthesisIU("in der Kneipe.");
            dispatcher.playStream(installment1.getAudio());
            installment1.appendContinuation(installment2.getWords());
            installment1.appendContinuation(installment3.getWords());
            installment1.appendContinuation(installment4.getWords());
            installment1.appendContinuation(installment5.getWords());
            installment1.appendContinuation(installment6.getWords());
            installment1.appendContinuation(installment7.getWords());
        }
        Thread.sleep(20000);

        String str = "Morgen um 10 ist das treffen mit deinem Bruder und um zwei Uhr gehst du Einkaufen und abends ab acht ist der Stammtisch in der Kneipe.";
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU(str);
        dispatcher.playStream(installment.getAudio());
        Thread.sleep(20000);
    }

    @Ignore
    @Test
    public void testMultipleSentences() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world bla bla bla.");
        System.out.println("Duration: " + installment.duration());

        HesitatingSynthesisIU installmentCont = new HesitatingSynthesisIU("This is.");
        installmentCont.getWords().get(0).shiftBy(installment.duration());
        installment.appendContinuation(installmentCont.getWords());
        System.out.println("Duration: " + installment.duration());

        dispatcher.playStream(installment.getAudio(), true);
        Thread.sleep(8000);
    }

    @Ignore
    @Test
    public void testHesitationContinuation() throws InterruptedException
    {

        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");

        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world <hes>");

        HesitatingSynthesisIU installmentCont = new HesitatingSynthesisIU("continuation.");

        for (IU word : installment.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(new MyWordUpdateListener());
        }
        dispatcher.playStream(installment.getAudio(), true);

        //Thread.sleep(2000);
        if (installment.isCompleted())
        {
            dispatcher.playStream(installmentCont.getAudio(), true);
        }
        else
        {
            installment.appendContinuation(installmentCont.getWords());
        }
        Thread.sleep(8000);

        // wordIU: <hes>, start is relax time
    }

    @Ignore
    @Test
    public void test() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        /*
         * TreeStructuredInstallmentIU installment = new TreeStructuredInstallmentIU(
         * Collections.<String> singletonList("Hello world, this is a very long sentence."));
         */
        // HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world, this is a very long sentence <hes>");
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello this is a basic BML test for the realizer bridge");
        dispatcher.playStream(installment.getAudio(), true);

        Thread.sleep(8000);

        for (IU word : installment.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(new MyWordUpdateListener());
        }
        dispatcher.playStream(installment.getAudio(), true);

        Thread.sleep(500);

        // dispatcher.interruptPlayback();

        // interrupt (?)
        for (IU word : installment.groundedIn())
        {
            word.revoke();
            for (IU we : word.groundedIn())
            {
                we.revoke();
                for (IU audio : we.groundedIn())
                {
                    audio.revoke();
                }
            }
        }

        /*
         * installment = new HesitatingSynthesisIU("Next sentence");
         * dispatcher.playStream(installment.getAudio(), true);
         */

        for (SysSegmentIU seg : installment.getSegments())
        {
            seg.stretchFromOriginal(2);
        }
        Thread.sleep(12000);

        // How to get phoneme timing?
    }
}
