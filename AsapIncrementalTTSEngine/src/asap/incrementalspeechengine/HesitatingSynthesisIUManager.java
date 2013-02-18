package asap.incrementalspeechengine;

import done.inpro.system.carchase.HesitatingSynthesisIU;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.WordIU;

public class HesitatingSynthesisIUManager
{
    private final DispatchStream dispatcher;
    private HesitatingSynthesisIU currentIU = null;
    private IncrementalTTSUnit currentTTSUnit;
    private static final double MERGE_TIME = 0.001d;

    public HesitatingSynthesisIUManager(DispatchStream dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public boolean appendIU(HesitatingSynthesisIU synthesisIU, IncrementalTTSUnit ttsCandidate)
    {
        if (currentIU == null || currentIU.isCompleted())
        {
            return false;
        }
        
        double timeDiff = ttsCandidate.getStartTime() - currentTTSUnit.getRelaxTime(); 
        System.out.println("timeDif: "+timeDiff);
        if (timeDiff < MERGE_TIME)
        {
            System.out.println("Merging!");
            currentIU.appendContinuation(synthesisIU.getWords());
            currentTTSUnit = ttsCandidate;
            return true;
        }
        return false;
    }

    public void playIU(HesitatingSynthesisIU synthesisIU, IncrementalTTSUnit ttsUnit)
    {
        if (currentTTSUnit==ttsUnit) return;//already added with appendIU
        
        currentTTSUnit = ttsUnit;
        if (currentIU == null || currentIU.isCompleted())
        {
            dispatcher.playStream(synthesisIU.getAudio(), true);
            currentIU = synthesisIU;
        }
        else
        {
            currentIU.appendContinuation(synthesisIU.getWords());
        }
    }
}
