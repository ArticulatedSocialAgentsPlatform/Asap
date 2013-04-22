package asap.incrementalspeechengine;

import inpro.audio.DispatchStream;
import inpro.incremental.IUModule;
import inpro.incremental.processor.AdaptableSynthesisModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.HesitationIU;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.PhraseIU;
import inpro.synthesis.hts.LoudnessPostProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import asap.realizer.scheduler.BMLBBlock;
import asap.realizer.scheduler.BMLBlock;
import asap.realizer.scheduler.BMLScheduler;

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
    
    public void clearBuffer()
    {
        rightBuffer.setBuffer(new ArrayList<IU>());
    }
}

@Data
class Phrase
{
    private final IU iu;
    private final IU hes;
    private final IncrementalTTSUnit ttsUnit;
}

@Slf4j
class PhraseConsumer implements Runnable
{
    private final BlockingQueue<Phrase> phraseQueue;
    private MyIUModule iuModule;
    
    public PhraseConsumer(BlockingQueue<Phrase> phraseQueue, MyIUModule iuModule)
    {
        this.phraseQueue = phraseQueue;
        this.iuModule = iuModule;
    }
    
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Phrase p = phraseQueue.take();
                if(p.getIu()!=null)
                {
                    iuModule.addToBuffer(p.getIu());
                }
                if(p.getHes()!=null)
                {
                    iuModule.addToBuffer(p.getHes());
                }
                p.getTtsUnit().addedToBuffer();
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
            }
            catch (Exception ex)
            {
                log.warn("Exception ", ex);
            }
        }
    }
    
}

public class PhraseIUManager
{
    private final MyIUModule iuModule = new MyIUModule();
    private final AdaptableSynthesisModule asm;
    private String voice;
    private DispatchStream dispatcher;
    private final BMLScheduler scheduler;
    private final LoudnessPostProcessor loudnessAdapter;
    private final BlockingQueue<Phrase> phraseQueue = new LinkedBlockingQueue<Phrase>();
    
    private List<IncrementalTTSUnit> currentTTSUnits = Collections.synchronizedList(new ArrayList<IncrementalTTSUnit>());
    
    public PhraseIUManager(DispatchStream dispatcher, String voice, BMLScheduler scheduler)
    {
        asm = new AdaptableSynthesisModule(dispatcher);
        this.voice = voice;
        this.scheduler = scheduler;
        iuModule.addListener(asm);
        loudnessAdapter = new LoudnessPostProcessor();
        asm.setFramePostProcessor(loudnessAdapter);
        
        Thread conThread = new Thread(new PhraseConsumer(phraseQueue, iuModule));
        conThread.start();
    }
    
    /**
     * Set loudness
     * @param loudness between -100 and 100
     */
    public void setLoudness(int loudness)
    {
        loudnessAdapter.setLoudness(loudness);
    }
    
    public double getCurrentTime()
    {
        return scheduler.getSchedulingTime();
    }
    
    
    /**
     * Appends synthesisIU to the currentIU if currentIU is ongoing, but finishes or relaxes within two phonemes AND
     * synthesisIU is supposed to start at either the relax time or the end time of the currentIU.
     */
    public boolean justInTimeAppendIU(PhraseIU synthesisIU, HesitationIU hes, IncrementalTTSUnit ttsCandidate)
    {
        if (currentTTSUnits.isEmpty())return false;
        IncrementalTTSUnit top = currentTTSUnits.get(currentTTSUnits.size()-1);
        if(ttsCandidate.getTimePeg("start").getLocalValue()==0 && !isPending(ttsCandidate.getBMLId()))
        {
            BMLBlock b = scheduler.getBMLBlockManager().getBMLBlock(ttsCandidate.getBMLId());
            if(b instanceof BMLBBlock)
            {
                BMLBBlock bb = (BMLBBlock)b;
                if(bb.getChunkAfterSet().contains(top.getBMLId()))
                {
                    //TODO: check if top aligns with end/relax of its block
                    
                    System.out.println("updateTiming: adding "+ttsCandidate.getBMLId()+" to buffer");
                    addIU(synthesisIU, hes, ttsCandidate);
                    return true;
                }
            }
        }
        else
        {
            //System.out.println(ttsCandidate.getBMLId()+"does not start at local time 0");
        }        
        return false;
    }

    public boolean isPending(String bmlId)
    {
        return scheduler.isPending(bmlId);
    }
    
    public void removeUnit(IncrementalTTSUnit ttsUnit)
    {
        currentTTSUnits.remove(ttsUnit);
    }
    
    public void playIU(PhraseIU synthesisIU, HesitationIU hes, IncrementalTTSUnit ttsUnit)
    {
        if(currentTTSUnits.contains(ttsUnit))return;
        addIU(synthesisIU, hes, ttsUnit);        
    }

    private void addIU(PhraseIU synthesisIU, HesitationIU hes, IncrementalTTSUnit ttsUnit)
    {
        //TODO: move to PhraseConsumer
        if(voice!=null)
        {
            System.setProperty("inpro.tts.voice",voice);
        }  
        
        currentTTSUnits.add(ttsUnit);        
        try
        {
            phraseQueue.put(new Phrase(synthesisIU, hes, ttsUnit));
        }
        catch (InterruptedException e1)
        {
            Thread.interrupted();
        }
        System.out.println("Adding "+ttsUnit.getBMLId()+" to buffer");
    }
    
    public void stopAfterOngoingWord()
    {
        currentTTSUnits.clear();
        phraseQueue.clear();
        asm.stopAfterOngoingWord();
    }
    
    public void stopAfterOngoingPhoneme()
    {
        currentTTSUnits.clear();
        phraseQueue.clear();
        asm.stopAfterOngoingPhoneme();        
        iuModule.clearBuffer();
    }
}
