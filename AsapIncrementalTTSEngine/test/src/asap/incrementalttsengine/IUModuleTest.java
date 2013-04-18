package asap.incrementalttsengine;

import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.IUModule;
import inpro.incremental.processor.AdaptableSynthesisModule;
import inpro.incremental.processor.SynthesisModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.PhraseIU;
import inpro.incremental.unit.SysInstallmentIU;
import inpro.incremental.unit.WordIU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.synthesis.MaryAdapter;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

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
    public void test() throws InterruptedException
    {
        System.setProperty("inpro.tts.voice", "dfki-prudence-hsmm");
        System.setProperty("inpro.tts.language", "en_GB");

        //MaryAdapter.getInstance();    
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
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
        MyIUModule mb = new MyIUModule();
        AdaptableSynthesisModule asm = new AdaptableSynthesisModule(dispatcher);
        mb.addListener(asm);
        String str= "Tomorow at 10 is the meeting with your brother. and at two o'clock you'll go shopping, and at eight is the gettogether in the bar";
        
        
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
        
        
        Thread.sleep(2000);
        
        //control at phrase level?
        //asm.scaleTempo(2);
        //asm.shiftPitch(400);
        
        
        //asm.stopAfterOngoingWord();
        
        
        Thread.sleep(20000);
    }
}
