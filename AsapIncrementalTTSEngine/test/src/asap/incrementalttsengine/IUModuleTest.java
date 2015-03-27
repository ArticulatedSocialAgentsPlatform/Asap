/*******************************************************************************
 *******************************************************************************/
package asap.incrementalttsengine;

import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.apps.util.MonitorCommandLineParser;
import inpro.audio.DispatchStream;
import inpro.incremental.IUModule;
import inpro.incremental.processor.AdaptableSynthesisModule;
import inpro.incremental.unit.ChunkIU;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.HesitationIU;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SysInstallmentIU;
import inpro.synthesis.MaryAdapter;
import inpro.synthesis.MaryAdapter5internal;
import inpro.synthesis.hts.IUBasedFullPStream;
import inpro.synthesis.hts.LoudnessPostProcessor;
import inpro.synthesis.hts.VocodingAudioStream;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;

import marytts.util.data.audio.DDSAudioInputStream;

import org.junit.Ignore;
import org.junit.Test;

class MyIUModule extends IUModule
{
    @Override
    protected void leftBufferUpdate(Collection<? extends IU> arg0, List<? extends EditMessage<? extends IU>> arg1)
    {

    }

    public void addToBuffer(IU iu)
    {
        rightBuffer.addToBuffer(iu);
        notifyListeners();
    }

    @Override
    public void reset()
    {
        rightBuffer.setBuffer(null, null);
    }
}

public class IUModuleTest
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

    @Test
    public void testPreSynthesize() throws IOException, InterruptedException
    {
        System.setProperty("inpro.tts.voice", "cmu-slt-hsmm");
        System.setProperty("inpro.tts.language", "en_US");
        
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new MonitorCommandLineParser(new String[]{"-D","-c",""+new Resources("").getURL("sphinx-config.xml")}));
        List<IU> wordIUs = MaryAdapter.getInstance().text2IUs("Heating up.");
        dispatcher.playStream(new DDSAudioInputStream(new VocodingAudioStream(new IUBasedFullPStream(wordIUs.get(0)),
                MaryAdapter5internal.getDefaultHMMData(), true), new AudioFormat(16000.0F, 16, 1, true, false)), true);
        // wait for synthesis:
        dispatcher.waitUntilDone();
        dispatcher.close();
                
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        String str = "Hello cruel world.";
        MyIUModule mb = new MyIUModule();

        AdaptableSynthesisModule sma = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(sma);
        
        
        ChunkIU piu = new ChunkIU(str);
        piu.preSynthesize();

        long t = System.nanoTime();
        mb.addToBuffer(piu);
        long tDur = System.nanoTime() - t;
        System.out.println("time taken to add " + TimeUnit.MILLISECONDS.convert(tDur, TimeUnit.NANOSECONDS) + "ms");
        
        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    @Ignore
    @Test
    public void testPhraseVSIncremental() throws InterruptedException, IOException
    {
        // MaryAdapter.getInstance();
        System.setProperty("inpro.tts.voice", "cmu-slt-hsmm");
        System.setProperty("inpro.tts.language", "en_US");
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));

        String str = "Tomorow at 10 is the meeting with your brother, and at two o clock you will go shopping, and at eight is the gettogether in the bar.";
        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);

        ChunkIU piu = new ChunkIU(str);
        mb.addToBuffer(piu);
        dispatcher.waitUntilDone();

        String strsplit[] = { "Tomorrow at 10", "is the meeting with your brother,", "and at two o clock", "you will go shopping,",
                "and at eight", "is the gettogether", "in the bar." };
        for (String s : strsplit)
        {
            ChunkIU p = new ChunkIU(s);
            mb.addToBuffer(p);
        }
        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    @Ignore
    @Test
    public void testEmptyPhraseIU()
    {
        ChunkIU iu = new ChunkIU("test");
        iu.getWords();
    }

    @Test
    public void testFeedback() throws InterruptedException, IOException
    {
        System.setProperty("inpro.tts.voice", "cmu-slt-hsmm");
        System.setProperty("inpro.tts.language", "en_US");
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        MyIUModule mb = new MyIUModule();

        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);
        MyWordUpdateListener l = new MyWordUpdateListener();

        ChunkIU p = new ChunkIU("Hello world");
        p.preSynthesize();

        ChunkIU p2 = new ChunkIU("hello hello hello");
        p2.preSynthesize();

        mb.addToBuffer(p);
        for (IU word : p.getWords())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }

        mb.addToBuffer(p2);
        for (IU word : p2.getWords())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        System.out.println("p1 start:" + p.startTime());
        System.out.println("p2 start:" + p2.startTime());
        System.out.println("p1 first word start:" + p.getWords().get(0).startTime());
        System.out.println("p2 first word start:" + p2.getWords().get(0).startTime());

        dispatcher.waitUntilDone();
        dispatcher.close();

        System.out.println("p1 start:" + p.startTime());
        System.out.println("p2 start:" + p2.startTime());
        System.out.println("p1 first word start:" + p.getWords().get(0).startTime());
        System.out.println("p2 first word start:" + p2.getWords().get(0).startTime());
    }

    @Ignore
    @Test
    public void testHesitation() throws InterruptedException, IOException
    {
        // MaryAdapter.getInstance();
        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);
        MyWordUpdateListener l = new MyWordUpdateListener();
        // String str= "Hello";
        // PhraseIU piu = new PhraseIU(str);
        // mb.addToBuffer(piu);
        //
        // for (IU word : piu.getWords())
        // {
        // word.updateOnGrinUpdates();
        // word.addUpdateListener(l);
        // }
        //
        // String str2 = "world.";
        // PhraseIU piu2 = new PhraseIU(str2);
        // mb.addToBuffer(piu2);
        // for (IU word : piu2.getWords())
        // {
        // word.updateOnGrinUpdates();
        // word.addUpdateListener(l);
        // }

        HesitationIU hes = new HesitationIU();
        mb.addToBuffer(hes);
        for (IU word : hes.getWords())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    @Test
    public void testInterruptContinue() throws InterruptedException, IOException
    {
        System.setProperty("inpro.tts.voice", "cmu-slt-hsmm");
        System.setProperty("inpro.tts.language", "en_US");

        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));

        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);

        String str = "Tomorrow at 10 is the meeting with your brother, and at two o clock you will go shopping, and at eight is the gettogether in the bar";
        ChunkIU piu = new ChunkIU(str);
        mb.addToBuffer(piu);

        Thread.sleep(1000);
        asm.stopAfterOngoingPhoneme();

        mb.addToBuffer(new ChunkIU("Hello world."));
        Thread.sleep(500);

        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    @Ignore
    @Test
    public void test() throws InterruptedException, IOException
    {
        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");

        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));

        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);

        LoudnessPostProcessor loudnessAdapter = new LoudnessPostProcessor();
        asm.setFramePostProcessor(loudnessAdapter);

        String str = "Tomorrow at 10 is the meeting with your brother, and at two o clock you will go shopping, and at eight is the gettogether in the bar";
        ChunkIU piu = new ChunkIU(str);

        SysInstallmentIU sysiu = new SysInstallmentIU(str);
        for (IU word : sysiu.groundedIn())
        {
            System.out.println("payload " + word.toPayLoad());
            System.out.println("start " + word.startTime());
        }

        MyWordUpdateListener l = new MyWordUpdateListener();

        piu.updateOnGrinUpdates();
        // piu.addUpdateListener(l);

        mb.addToBuffer(piu);
        for (IU word : piu.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        loudnessAdapter.setLoudness(0);

        Thread.sleep(1000);
        asm.stopAfterOngoingPhoneme();
        // loudnessAdapter.setLoudness(70);
        // System.out.println("loudness = 70");

        mb.addToBuffer(new ChunkIU("Hello world."));
        Thread.sleep(500);
        // loudnessAdapter.setLoudness(-50);

        // control at phrase level?
        // asm.scaleTempo(2);
        // asm.shiftPitch(400);

        // asm.stopAfterOngoingWord();

        dispatcher.waitUntilDone();
        dispatcher.close();
    }
}
