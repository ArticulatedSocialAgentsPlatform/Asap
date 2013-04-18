package asap.incrementalspeechengine;

import inpro.audio.DispatchStream;
import inpro.incremental.IUModule;
import inpro.incremental.processor.AdaptableSynthesisModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.PhraseIU;
import inpro.incremental.unit.WordIU;

import java.util.Collection;
import java.util.List;

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

public class PhraseIUManager
{
    private IncrementalTTSUnit currentTTSUnit;
    private static final double MERGE_TIME = 0.001d;
    private PhraseIU currentIU = null;
    private final MyIUModule iuModule = new MyIUModule();
    private final AdaptableSynthesisModule asm;
    private String voice;
    
    public PhraseIUManager(DispatchStream dispatcher, String voice)
    {
        asm = new AdaptableSynthesisModule(dispatcher);
        this.voice = voice;
        iuModule.addListener(asm);
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
    public boolean justInTimeAppendIU(PhraseIU synthesisIU, IncrementalTTSUnit ttsCandidate)
    {
        if (currentIU == null || currentIU.isCompleted())
        {
            return false;
        }
        double timeDiffRelax = Math.abs(ttsCandidate.getStartTime() - currentTTSUnit.getRelaxTime());
        double timeDiffEnd = Math.abs(ttsCandidate.getStartTime() - currentTTSUnit.getEndTime());
        
        WordIU lastWord = (WordIU)currentIU.groundedIn().get(currentIU.groundedIn().size() - 1);
        
        boolean merge = false;
        if (lastWord.toPayLoad().equals("<hes>")&& timeDiffRelax < MERGE_TIME)
        {
            lastWord = (WordIU)currentIU.groundedIn().get(currentIU.groundedIn().size() - 2);
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
                currentTTSUnit = ttsCandidate;
                
                
                iuModule.addToBuffer(synthesisIU);
                System.out.println("Adding "+ttsCandidate.getBMLId()+" to buffer");
                return true;
            }
        }
        return false;
    }

    public void playIU(PhraseIU synthesisIU, IncrementalTTSUnit ttsUnit)
    {
        if (currentTTSUnit == ttsUnit) return;// already added with appendIU

        if(voice!=null)
        {
            System.setProperty("inpro.tts.voice",voice);
        }
        currentTTSUnit = ttsUnit;
        iuModule.addToBuffer(synthesisIU);
        System.out.println("Adding "+ttsUnit.getBMLId()+" to buffer");
        
        currentIU = synthesisIU;        
    }
    
    public void stopAfterOngoingWord()
    {
        asm.stopAfterOngoingWord();
        System.out.println("Remove All!");
    }
}
