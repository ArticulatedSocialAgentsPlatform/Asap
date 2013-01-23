package asap.incrementalttsengine;

import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.IU.Progress;
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
            Progress newProgress = updatedIU.getProgress();
            System.out.println(newProgress.toString());
            
            for(IU we: updatedIU.groundedIn())
            {
                System.out.println("Phoneme: "+we.toPayLoad());
                System.out.println("Start: "+we.startTime());
                System.out.println("End: "+we.endTime());
                System.out.println("progress: "+we.getProgress());
            }
        }
    }

    @Test
    public void test() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        /*
        TreeStructuredInstallmentIU installment = new TreeStructuredInstallmentIU(
                Collections.<String> singletonList("Hello world, this is a very long sentence."));
        */
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world, this is a very long sentence <hes>");
        for (IU word : installment.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(new MyWordUpdateListener());            
        }        
        dispatcher.playStream(installment.getAudio(), true);        
        
        Thread.sleep(500);
        
        //dispatcher.interruptPlayback();
        
        //interrupt (?)
        for (IU word : installment.groundedIn())
        {
            word.revoke();
            for(IU we: word.groundedIn())
            {
                we.revoke();
                for(IU audio: we.groundedIn())
                {
                    audio.revoke();
                }
            }
        }
        
        /*
        installment = new HesitatingSynthesisIU("Next sentence");
        dispatcher.playStream(installment.getAudio(), true);
        */
        
        /*
        for (SysSegmentIU seg : installment.getSegments()) {
            seg.stretch(2);
        }
        */
        Thread.sleep(12000);
        
        //How to get phoneme timing?        
    }
}
