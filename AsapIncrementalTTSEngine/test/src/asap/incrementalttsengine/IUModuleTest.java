package asap.incrementalttsengine;

import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.IUModule;
import inpro.incremental.processor.AdaptableSynthesisModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.HesitationIU;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.PhraseIU;
import inpro.incremental.unit.SysInstallmentIU;
import inpro.synthesis.MaryAdapter;
import inpro.synthesis.hts.LoudnessPostProcessor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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
    
    @Ignore
    @Test
    public void testPhraseVSIncremental() throws InterruptedException, IOException
    {
        //MaryAdapter.getInstance();    
        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        
        String str= "Tomorow at 10 is the meeting with your brother, and at two o clock you will go shopping, and at eight is the gettogether in the bar.";
        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);
        
        PhraseIU piu = new PhraseIU(str);
        mb.addToBuffer(piu);
        dispatcher.waitUntilDone();        
        
        String strsplit[] = { "Tomorrow at 10", "is the meeting with your brother,", "and at two o clock", "you will go shopping,",
                "and at eight", "is the gettogether", "in the bar." };
        for(String s:strsplit)
        {
            PhraseIU p = new PhraseIU(s);
            mb.addToBuffer(p);
        }  
        dispatcher.waitUntilDone();
        dispatcher.close();        
    }
    
    @Ignore
    @Test
    public void testHesitation() throws InterruptedException, IOException
    {
        //MaryAdapter.getInstance();    
        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);
        
        String str= "Hello";        
        PhraseIU piu = new PhraseIU(str);
        mb.addToBuffer(piu);
        MyWordUpdateListener l = new MyWordUpdateListener();
        for (IU word : piu.getWords())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        
        String str2 = "world.";
        PhraseIU piu2 = new PhraseIU(str2);
        mb.addToBuffer(piu2);
        for (IU word : piu2.getWords())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        
        
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

        String str= "Tomorrow at 10 is the meeting with your brother, and at two o clock you will go shopping, and at eight is the gettogether in the bar";        
        PhraseIU piu = new PhraseIU(str);
        
        
        SysInstallmentIU sysiu = new SysInstallmentIU(str);
        for (IU word : sysiu.groundedIn())
        {
            System.out.println("payload " + word.toPayLoad());
            System.out.println("start " + word.startTime());
        }
        
        MyWordUpdateListener l = new MyWordUpdateListener();
        
        piu.updateOnGrinUpdates();
        //piu.addUpdateListener(l);
        
        mb.addToBuffer(piu);
        for (IU word : piu.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(l);
        }
        loudnessAdapter.setLoudness(0);
        
        Thread.sleep(500);
        loudnessAdapter.setLoudness(70);
        System.out.println("loudness = 70");
        
        Thread.sleep(2500);
        loudnessAdapter.setLoudness(-50);
        
        //control at phrase level?
        //asm.scaleTempo(2);
        //asm.shiftPitch(400);
        
        
        //asm.stopAfterOngoingWord();
        
        
        dispatcher.waitUntilDone();
        dispatcher.close();
    }
}
