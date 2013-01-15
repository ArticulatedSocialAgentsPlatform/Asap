package asap.incrementalttsengine;

import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.IU.Progress;
import inpro.incremental.unit.SysSegmentIU;
import inpro.synthesis.MaryAdapter;

import org.junit.Test;

import done.inpro.system.carchase.HesitatingSynthesisIU;

public class IncrementalTTSTest
{
    private static class MyUpdateListener implements IUUpdateListener
    {
        @Override
        public void update(IU updatedIU)
        {
            System.out.println("update " + updatedIU.toString());
            Progress newProgress = updatedIU.getProgress();
            System.out.println(newProgress.toString());
        }
    }

    @Test
    public void test() throws InterruptedException
    {
        MaryAdapter.getInstance();
        DispatchStream dispatcher = SimpleMonitor.setupDispatcher();
        /*
        TreeStructuredInstallmentIU installment = new TreeStructuredInstallmentIU(
                Collections.<String> singletonList("Hello world, this is a very long sentence."));
        */
        HesitatingSynthesisIU installment = new HesitatingSynthesisIU("Hello world, this is a very long sentence <hes>");
        for (IU word : installment.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(new MyUpdateListener());
        }        
        dispatcher.playStream(installment.getAudio(), true);        
        
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
