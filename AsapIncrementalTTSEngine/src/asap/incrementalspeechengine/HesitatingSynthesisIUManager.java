/*******************************************************************************
 *******************************************************************************/
package asap.incrementalspeechengine;

import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;
import lombok.Getter;
import done.inpro.system.carchase.HesitatingSynthesisIU;

/**
 * Manages the construction and concatenation of HesitatingSynthesisIUs
 * @author hvanwelbergen
 * 
 */
public class HesitatingSynthesisIUManager
{
    private final DispatchStream dispatcher;
    private HesitatingSynthesisIU currentIU = null;
    private IncrementalTTSUnit currentTTSUnit;
    @Getter
    private final String voiceName;
    
    private static final double MERGE_TIME = 0.001d;

    public HesitatingSynthesisIUManager(DispatchStream dispatcher, String voiceName)
    {
        this.dispatcher = dispatcher;
        this.voiceName = voiceName;
    }

    private int getRemainingPhonemes(WordIU word)
    {
        int i = 0;
        for (IU phonemeIU : word.groundedIn())
        {
            if (phonemeIU.isCompleted())
            {
                i++;
            }
        }
        return word.groundedIn().size() - i;
    }

    /**
     * Appends synthesisIU to the currentIU if currentIU is ongoing, but finishes or relaxes within two phonemes AND
     * synthesisIU is supposed to start at either the relax time or the end time of the currentIU.
     */
    public boolean justInTimeAppendIU(HesitatingSynthesisIU synthesisIU, IncrementalTTSUnit ttsCandidate)
    {
        if (currentIU == null || currentIU.isCompleted())
        {
            return false;
        }
        double timeDiffRelax = Math.abs(ttsCandidate.getStartTime() - currentTTSUnit.getRelaxTime());
        double timeDiffEnd = Math.abs(ttsCandidate.getStartTime() - currentTTSUnit.getEndTime());
        
        WordIU lastWord = currentIU.getWords().get(currentIU.getWords().size() - 1);
        
        boolean merge = false;
        if (lastWord.toPayLoad().equals("<hes>")&& timeDiffRelax < MERGE_TIME)
        {
            lastWord = currentIU.getWords().get(currentIU.getWords().size() - 2);
            merge = true;
        }
        else if(timeDiffEnd < MERGE_TIME && !lastWord.toPayLoad().equals("<hes>"))
        {
            merge = true;
        }

        if (merge)
        {
            if (getRemainingPhonemes(lastWord) <= 2)
            {
                currentIU.appendContinuation(synthesisIU.getWords());
                currentTTSUnit = ttsCandidate;
                return true;
            }
        }
        return false;
    }

    public void playIU(HesitatingSynthesisIU synthesisIU, IncrementalTTSUnit ttsUnit)
    {
        if (currentTTSUnit == ttsUnit) return;// already added with appendIU

        
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
