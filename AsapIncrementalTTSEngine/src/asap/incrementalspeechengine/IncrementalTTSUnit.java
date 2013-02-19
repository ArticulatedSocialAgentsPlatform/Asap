package asap.incrementalspeechengine;

import hmi.tts.Visime;
import hmi.tts.util.BMLTextUtil;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.PhonemeUtil;
import hmi.tts.util.SyncAndOffset;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SysSegmentIU;
import inpro.incremental.unit.WordIU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
    private List<String> syncs = new ArrayList<>();
    private List<String> progressHandled = new ArrayList<>();
    private final HesitatingSynthesisIUManager iuManager;
    private volatile boolean isScheduled = false;
    private static final double SETUP_OFFSET = 0.5d;
    
    // nr of the word after the sync => SyncId
    private final BiMap<Integer, String> syncMap = HashBiMap.create();

    // syncName => TimePeg
    private Map<String, TimePeg> pegs = new HashMap<String, TimePeg>();

    public IncrementalTTSUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, String text, HesitatingSynthesisIUManager iuManager,
            Collection<IncrementalLipSynchProvider> lsProviders, PhonemeToVisemeMapping visemeMapping, Behaviour beh)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.lsProviders = ImmutableList.copyOf(lsProviders);

        String textNoSync = BMLTextUtil.stripSyncs(text);
        setupSyncs(textNoSync, text);

        String generateFiller = null;
        if (beh.specifiesParameter("http://www.asap-project.org/bmlis:generatefiller"))
        {
            generateFiller = beh.getStringParameterValue("http://www.asap-project.org/bmlis:generatefiller");
        }
        
        textNoSync = textNoSync.trim();
        if (generateFiller != null && generateFiller.trim().equals("true"))
        {
            if(textNoSync.endsWith("."))
            {
                textNoSync = textNoSync.substring(0,textNoSync.length()-1)+" <hes>";
            }
            else
            {
                textNoSync = textNoSync + " <hes>";
            }
            hasRelax = true;
        }
        else
        {
            hasRelax = false;
        }
        synthesisIU = createHesitatingSynthesisIU(textNoSync);

        WordUpdateListener wul = new WordUpdateListener();
        lastWord = null;
        for (IU word : synthesisIU.groundedIn())
        {
            word.updateOnGrinUpdates();
            word.addUpdateListener(wul);
            lastWord = word;
        }

        this.visemeMapping = visemeMapping;
        this.iuManager = iuManager;
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        for (String sync : syncs)
        {
            pegs.put(sync, new TimePeg(bmlPeg));
        }
        behavior = beh;
    }

    private void setupSyncs(String textNoSync, String text)
    {
        String words[] = textNoSync.split(" ");
        List<SyncAndOffset> syncAndOffsets = BMLTextUtil.getSyncAndOffsetList(text, words.length);

        for (SyncAndOffset s : syncAndOffsets)
        {
            syncs.add(s.getSync());
            syncMap.put(s.getOffset(), s.getSync());
        }
    }

    @Override
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if (syncMap.inverse().containsKey(syncId))
        {
            int i = syncMap.inverse().get(syncId);
            if(i>=synthesisIU.groundedIn().size())
            {
                return 1;
            }
            return synthesisIU.groundedIn().get(i).startTime() / getPreferedDuration();
        }
        return super.getRelativeTime(syncId);
    }

    private int getSeperatorIndexIfFoundAndSmaller(String text, int startIndex, char seperator, int currentValue)
    {
        int index = text.indexOf(seperator, startIndex);
        if (index != -1 && index < currentValue)
        {
            return index;
        }
        return currentValue;
    }

    private int getNextSentenceSeparatorIndex(String text, int startIndex)
    {
        int currentIndex = getSeperatorIndexIfFoundAndSmaller(text, startIndex, '.', Integer.MAX_VALUE);
        currentIndex = getSeperatorIndexIfFoundAndSmaller(text, startIndex, '!', currentIndex);
        currentIndex = getSeperatorIndexIfFoundAndSmaller(text, startIndex, '?', currentIndex);
        if (currentIndex == Integer.MAX_VALUE)
        {
            return -1;
        }
        return currentIndex;
    }

    private HesitatingSynthesisIU createHesitatingSynthesisIU(String text)
    {
        int pointIndex = getNextSentenceSeparatorIndex(text,0);
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
            pointIndex = getNextSentenceSeparatorIndex(text, prevIndex);
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

    private void updateFeedback()
    {
        int i = 0;
        for (IU word : synthesisIU.groundedIn())
        {
            if (word.isOngoing() || word.isCompleted())
            {
                if (syncMap.containsKey(i))
                {
                    String sync = syncMap.get(i);
                    if (!progressHandled.contains(sync))
                    {
                        progressHandled.add(sync);
                        feedback(sync, getStartTime() + word.startTime());
                    }
                }
            }
            i++;
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
            updateFeedback();
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
        return new ImmutableList.Builder<String>().add("start").addAll(syncs).add("relax").add("end").build();
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
        return pegs.get(syncId);
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
        pegs.put(syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        if (startPeg.getGlobalValue() >= endPeg.getGlobalValue() && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        if (startPeg.getGlobalValue() >= relaxPeg.getGlobalValue() && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        if (relaxPeg.getGlobalValue() >= endPeg.getGlobalValue() && relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        return true;
    }

    private void updateSyncTiming()
    {
        for (Entry<Integer, String> entry : syncMap.entrySet())
        {
            if (entry.getKey() < synthesisIU.getWords().size())
            {
                WordIU wordAfter = synthesisIU.getWords().get(entry.getKey());
                pegs.get(entry.getValue()).setGlobalValue(getStartTime() + wordAfter.startTime());
            }
            else
            {
                pegs.get(entry.getValue()).setGlobalValue(getEndTime());
            }
        }
    }
    
    
    @Override
    public void updateTiming(double time)
    {
        if(!isScheduled && time>getStartTime()-1d)
        {
            isScheduled = iuManager.justInTimeAppendIU(synthesisIU, this);
        }
    }
    
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        endPeg.setGlobalValue(time + lastWord.endTime());
        relaxPeg.setGlobalValue(getEndTime());
        updateSyncTiming();
        updateLipSync();
        sendFeedback("start", time);
        iuManager.playIU(synthesisIU, this);
        isScheduled = true; 
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
        if (getState() == TimedPlanUnitState.IN_EXEC)
        {
            if (time > this.getRelaxTime())
            {
                sendFeedback("relax", time);
            }
        }
        // TODO: send feedback for syncs
        sendFeedback("end", time);
    }

}
