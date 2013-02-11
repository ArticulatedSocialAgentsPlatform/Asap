package asap.incrementalspeechengine;

import hmi.tts.Visime;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.PhonemeUtil;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SysSegmentIU;
import inpro.incremental.unit.WordIU;

import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
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
@Slf4j
public class IncrementalTTSUnit extends TimedAbstractPlanUnit
{
    private HesitatingSynthesisIU synthesisIU;
    private DispatchStream dispatcher;
    private TimePeg startPeg;
    private TimePeg relaxPeg;
    private TimePeg endPeg;
    private float stretch = 1;
    private float pitchShiftInCent = 0;
    private ImmutableList<IncrementalLipSynchProvider> lsProviders;
    private final PhonemeToVisemeMapping visemeMapping;
    private final int visemeLookAhead = 2;
    private final Behaviour behavior;
    private final boolean hasRelax;
    private IU lastWord;

    public IncrementalTTSUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, String text, DispatchStream dispatcher,
            Collection<IncrementalLipSynchProvider> lsProviders, PhonemeToVisemeMapping visemeMapping, Behaviour beh)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.lsProviders = ImmutableList.copyOf(lsProviders);
        String generateFiller = beh.getStringParameterValue("http://www.asap-project.org/bmlis:generatefiller");
        if (generateFiller != null && generateFiller.trim().equals("true"))
        {
            text = text + " <hes>";
            hasRelax = true;
        }
        else
        {
            hasRelax = false;
        }

        synthesisIU = createHesitatingSynthesisIU(text);

        WordUpdateListener wul = new WordUpdateListener();
        lastWord = null;
        for (IU word : synthesisIU.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(wul);
            lastWord = word;
        }

        this.visemeMapping = visemeMapping;
        this.dispatcher = dispatcher;
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        behavior = beh;
    }

    private HesitatingSynthesisIU createHesitatingSynthesisIU(String text)
    {
        int pointIndex = text.indexOf('.');
        if (pointIndex == -1)
        {
            return new HesitatingSynthesisIU(text);
        }

        HesitatingSynthesisIU iu = null;
        int prevIndex = 0;
        while (pointIndex != -1)
        {
            String currentText = text.substring(prevIndex, pointIndex + 1);
            if (iu == null)
            {
                iu = new HesitatingSynthesisIU(currentText);
            }
            else if (currentText.trim().length() > 0)
            {
                HesitatingSynthesisIU iuCont = new HesitatingSynthesisIU(currentText);
                for (WordIU wiu : iuCont.getWords())
                {
                    wiu.shiftBy(iu.duration());
                }
                iu.appendContinuation(iuCont.getWords());
            }
            prevIndex = pointIndex + 1;
            pointIndex = text.indexOf('.', prevIndex);
        }
        return iu;
    }

    private void updateLipSyncUnit(IU phIU)
    {
        for (IncrementalLipSynchProvider lsp : lsProviders)
        {
            int number = visemeMapping.getVisemeForPhoneme(PhonemeUtil.phonemeStringToInt(phIU.toPayLoad()));
            Visime viseme = new Visime(number, (int) (1000 * (phIU.endTime() - phIU.startTime())), false);
            lsp.setLipSyncUnit(getBMLBlockPeg(), behavior, phIU.startTime() + getStartTime(), viseme, phIU);
        }
    }

    private void updateLipSync()
    {
        int visCount = visemeLookAhead;
        for (IU word : synthesisIU.groundedIn())
        {
            if (word.isUpcoming() || word.isOngoing())
            {
                for (IU ph : word.groundedIn())
                {
                    if (ph.isUpcoming())
                    {
                        updateLipSyncUnit(ph);
                        visCount--;
                        if (visCount == 0) return;
                    }
                }
            }
        }
    }

    private void updateRelax(IU iu)
    {
        if (iu.toPayLoad().equals("<hes>"))
        {
            relaxPeg.setGlobalValue(iu.startTime() + getStartTime());
        }
    }

    private void updateEnd()
    {
        log.debug("end time: {} global: {} start: {}", new Object[] { lastWord.endTime(), getStartTime() + lastWord.endTime(),
                getStartTime() });
        endPeg.setGlobalValue(getStartTime() + lastWord.endTime());
        log.debug("start: {}", getStartTime());
    }

    private class WordUpdateListener implements IUUpdateListener
    {
        @Override
        public void update(IU updatedIU)
        {
            updateEnd();
            if (hasRelax)
            {
                updateRelax(updatedIU);
            }
            else
            {
                relaxPeg.setGlobalValue(getEndTime());
            }
            updateLipSync();
        }
    }

    private void stretch(float value)
    {
        stretch = value;
        log.debug("set stretch {}", value);
        for (SysSegmentIU seg : synthesisIU.getSegments())
        {
            if (!seg.isCompleted() && !seg.isOngoing())
            {
                log.debug("setting segment stretch {}", value);
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
        else if (paramId.equals("volume"))
        {
            // TODO: implement this
            return 0;
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
        else if (paramId.equals("volume"))
        {
            // TODO: implement this
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
        return lastWord.endTime();
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
        endPeg.setGlobalValue(time + lastWord.endTime());
        relaxPeg.setGlobalValue(getEndTime());
        updateLipSync();
        sendFeedback("start", time);
        dispatcher.playStream(synthesisIU.getAudio(), true);
        super.startUnit(time);
    }

    @Override
    protected void relaxUnit(double time)
    {
        log.debug("incremental speech relax {}", time);
        sendFeedback("relax", time);
    }

    @Override
    protected void playUnit(double time)
    {

    }

    @Override
    protected void stopUnit(double time)
    {
        log.debug("incremental speech end {}", time);
        sendFeedback("end", time);
    }

}
