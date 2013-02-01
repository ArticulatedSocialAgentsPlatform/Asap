package asap.incrementalspeechengine;

import hmi.tts.Visime;
import hmi.tts.util.PhonemeToVisemeMapping;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SysSegmentIU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import saiba.bml.core.SpeechBehaviour;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableList;

import done.inpro.system.carchase.HesitatingSynthesisIU;

/**
 * Incrementally constructed and updated ttsunit
 * @author hvanwelbergen
 * 
 */
public class IncrementalTTSUnit extends TimedAbstractPlanUnit
{
    private HesitatingSynthesisIU synthesisIU;
    private DispatchStream dispatcher;
    private TimePeg startPeg;
    private TimePeg relaxPeg;
    private TimePeg endPeg;
    private double duration;
    private float stretch = 1;
    private float pitchShiftInCent = 0;
    private ImmutableList<LipSynchProvider> lsProviders;
    private final PhonemeToVisemeMapping visemeMapping;
    

    public IncrementalTTSUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, String text, DispatchStream dispatcher,
            Collection<LipSynchProvider> lsProviders, PhonemeToVisemeMapping visemeMapping)
    {
        super(fbm, bmlPeg, bmlId, behId);        
        this.lsProviders = ImmutableList.copyOf(lsProviders);
        synthesisIU = new HesitatingSynthesisIU(text);
        
        WordUpdateListener wul = new WordUpdateListener();
        for (IU word : synthesisIU.groundedIn())
        {
            word.updateOnGrinUpdates();
            for (IU we : word.groundedIn())
            {
                System.out.println("Phoneme: " + we.toPayLoad());
                System.out.println("Start: " + we.startTime());
                System.out.println("End: " + we.endTime());
                System.out.println("progress: " + we.getProgress());
            }
            word.addUpdateListener(wul);
        }
        this.visemeMapping = visemeMapping;
        this.dispatcher = dispatcher;
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        duration = synthesisIU.duration();
    }

    private void updateLipSyncUnit(IU phIU)
    {
       
        for(LipSynchProvider lsp:lsProviders)
        {
            //lsp.setLipSyncMovement(this.getBMLBlockPeg(), behavior, this, visemes);
        }
    }
    
    private void updateLipSync()
    {
        for (IU word :synthesisIU.groundedIn())
        {
            if(word.isUpcoming() || word.isOngoing())
            {
                for (IU ph :word.groundedIn())
                {
                    if(ph.isUpcoming())
                    {
                        updateLipSyncUnit(ph);
                        return;
                    }
                }
            }
        }
    }

    private class WordUpdateListener implements IUUpdateListener
    {
        @Override
        public void update(IU updatedIU)
        {
            updateLipSync();
//            for (IU word :synthesisIU.groundedIn())
//            {
//                for (IU we :word.groundedIn())
//                {
//                    System.out.println("Phoneme: " + we.toPayLoad());
//                    System.out.println("Start: " + we.startTime());
//                    System.out.println("End: " + we.endTime());
//                    System.out.println("progress: " + we.getProgress());
//                }
//            }
        }
    }

    private void stretch(float value)
    {
        stretch = value;
        for (SysSegmentIU seg : synthesisIU.getSegments())
        {
            if (!seg.isCompleted() && !seg.isOngoing())
            {
                seg.stretchFromOriginal(value);
            }
        }
    }

    private void setPitch(float value)
    {
        pitchShiftInCent = value;
        for (SysSegmentIU seg : synthesisIU.getSegments())
        {
            if (!seg.isCompleted() && !seg.isOngoing())
            {
                seg.pitchShiftInCent = value;
            }
        }
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        if (paramId.equals("stretch"))
        {
            return stretch;
        }
        else if (paramId.equals("pitch"))
        {
            return pitchShiftInCent;
        }
        else
        {
            return super.getFloatParameterValue(paramId);
        }
    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        if (paramId.equals("stretch"))
        {
            stretch(value);
        }
        else if (paramId.equals("pitch"))
        {
            setPitch(value);
        }
        else
        {
            super.setFloatParameterValue(paramId, value);
        }
    }

    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        return relaxPeg.getGlobalValue();
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        // TODO: add marks
        return ImmutableList.of("start", "relax", "end");
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start"))
        {
            return startPeg;
        }
        else if (syncId.equals("end"))
        {
            return endPeg;
        }
        else if (syncId.equals("relax"))
        {
            return relaxPeg;
        }

        // TODO pegs for marks
        return null;
    }

    @Override
    public double getPreferedDuration()
    {
        return duration;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (syncId.equals("start"))
        {
            startPeg = peg;
        }
        else if (syncId.equals("end"))
        {
            endPeg = peg;
        }
        else if (syncId.equals("relax"))
        {
            relaxPeg = peg;
        }
        // TODO: pegs for marks
    }

    @Override
    public boolean hasValidTiming()
    {
        return true;
    }

    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        updateLipSync();
        dispatcher.playStream(synthesisIU.getAudio(), true);
        sendFeedback("start", time);
        super.startUnit(time);
    }

    @Override
    protected void playUnit(double time)
    {

    }

    @Override
    protected void stopUnit(double time)
    {
        sendFeedback("end", time);
        // dispatcher.interruptPlayback();
    }

}
