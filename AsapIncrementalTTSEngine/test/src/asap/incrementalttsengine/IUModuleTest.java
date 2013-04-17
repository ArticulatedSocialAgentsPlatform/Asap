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
import inpro.synthesis.MaryAdapter;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class IUModuleTest
{
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
        PhraseIU piu = new PhraseIU("blah blah");        
        mb.addToBuffer(piu);
        
        
        //asm.stopAfterOngoingWord();
        
        Thread.sleep(3000);
    }
}
