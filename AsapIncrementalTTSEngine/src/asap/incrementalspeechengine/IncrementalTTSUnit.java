/*******************************************************************************
 *******************************************************************************/
package asap.incrementalspeechengine;

import hmi.tts.Visime;
import hmi.tts.util.BMLTextUtil;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.PhonemeUtil;
import hmi.tts.util.SyncAndOffset;
import inpro.incremental.unit.ChunkIU;
import inpro.incremental.unit.HesitationIU;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.IU.IUUpdateListener;
import inpro.incremental.unit.SegmentIU;
import inpro.incremental.unit.SysSegmentIU;
import inpro.incremental.unit.WordIU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Setter;
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

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Incrementally constructed and updated ttsunit
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class IncrementalTTSUnit extends TimedAbstractPlanUnit
{
    private ChunkIU synthesisIU;
    private TimePeg startPeg;
    private TimePeg relaxPeg;
    private TimePeg endPeg;
    private float stretch = 1;
    private float pitchShiftInCent = 0;
    private ImmutableList<IncrementalLipSynchProvider> lsProviders;
    private final PhonemeToVisemeMapping visemeMapping;
    private final int visemeLookAhead = 2;
    private final Behaviour behavior;
    private volatile IU lastWord;
    private volatile IU firstWord;
    private List<String> syncs = new ArrayList<>();
    private final PhraseIUManager iuManager;
    @Setter
    private volatile boolean scheduled = false;
    private int numwords;
    private Set<String> feedbackSent = Collections.synchronizedSet(new HashSet<String>());
    private final WordUpdateListener wul;
    private double startDelay = 0;
    private int loudness = 0;
    private String textNoSync;

    // nr of the word after the sync => SyncId
    private final BiMap<Integer, String> syncMap = HashBiMap.create();
    private HesitationIU hesitation = null;

    // syncName => TimePeg
    private Map<String, TimePeg> pegs = new HashMap<String, TimePeg>();

    public void reset()
    {
        if (hesitation != null)
        {
            hesitation = new HesitationIU();
        }
        if (textNoSync.length() > 0)
        {
            synthesisIU = new ChunkIU(textNoSync);
            synthesisIU.preSynthesize();
            lastWord = null;
            for (WordIU word : synthesisIU.getWords())
            {
                lastWord = word;
            }
            firstWord = synthesisIU.getWords().get(0);
        }
        scheduled = false;
    }

    public IncrementalTTSUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, String text, PhraseIUManager iuManager,
            Collection<IncrementalLipSynchProvider> lsProviders, PhonemeToVisemeMapping visemeMapping, Behaviour beh)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.lsProviders = ImmutableList.copyOf(lsProviders);
        this.iuManager = iuManager;
        textNoSync = BMLTextUtil.stripSyncs(text);
        setupSyncs(textNoSync, text);

        String generateFiller = null;
        if (beh.specifiesParameter("http://www.asap-project.org/bmlis:generatefiller"))
        {
            generateFiller = beh.getStringParameterValue("http://www.asap-project.org/bmlis:generatefiller");
        }

        textNoSync = textNoSync.trim();
        if (generateFiller != null && generateFiller.trim().equals("true"))
        {
            hesitation = new HesitationIU();
            if (textNoSync.endsWith("."))
            {
                textNoSync = textNoSync.substring(0, textNoSync.length() - 1);
            }
        }

        wul = new WordUpdateListener();

        if (textNoSync.length() > 0)
        {
            synthesisIU = new ChunkIU(textNoSync);
            synthesisIU.preSynthesize();
            lastWord = null;
            for (WordIU word : synthesisIU.getWords())
            {
                lastWord = word;
            }
            firstWord = synthesisIU.getWords().get(0);
        }
        else
        {
            synthesisIU = null;
        }
        this.visemeMapping = visemeMapping;

        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        for (String sync : syncs)
        {
            pegs.put(sync, new TimePeg(bmlPeg));
        }
        behavior = beh;
    }

    private ImmutableList<WordIU> getWords()
    {
        if (synthesisIU == null)
        {
            if (hesitation == null)
            {
                return ImmutableList.of();
            }
            return ImmutableList.copyOf(hesitation.getWords());
        }
        if (hesitation == null)
        {
            return ImmutableList.copyOf(synthesisIU.getWords());
        }
        return new ImmutableList.Builder<WordIU>().addAll(synthesisIU.getWords()).addAll(hesitation.getWords()).build();
    }

    private double getDefaultWordsDuration(int j1, int j2)
    {
        if (j2 > getWords().size()) return 0;

        double duration = 0;
        for (int j = j1; j < j2; j++)
        {
            WordIU word = getWords().get(j);
            for (SegmentIU seg : word.getSegments())
            {
                SysSegmentIU sseg = (SysSegmentIU) seg;
                duration += sseg.originalDuration();
            }
        }
        return duration;
    }

    @Override
    public void feedback(String syncId, double time)
    {
        boolean fb = false;
        synchronized (feedbackSent)
        {
            if (!feedbackSent.contains(syncId))
            {
                fb = true;
                feedbackSent.add(syncId);
            }
        }
        if (fb)
        {
            super.feedback(syncId, time);
        }
    }

    private void stretchWords(int j1, int j2, double stretch)
    {
        for (int j = j1; j < j2; j++)
        {
            WordIU word = getWords().get(j);
            for (SegmentIU seg : word.getSegments())
            {
                SysSegmentIU sseg = (SysSegmentIU) seg;
                sseg.stretchFromOriginal(stretch);
            }
        }
    }

    /**
     * Stretch timing to match that set in the timepegs
     */
    public void applyTimeConstraints()
    {
        List<Integer> syncOffsets = new ArrayList<>(syncMap.keySet());
        Collections.sort(syncOffsets);

        // apply sync constraints
        int prevIndex = 0;

        double prevTime = startPeg.getGlobalValue() + startDelay;
        for (int i : syncOffsets)
        {
            String sync = syncMap.get(i);
            TimePeg tp = getTimePeg(sync);
            if (tp != null && tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                double defDuration = getDefaultWordsDuration(prevIndex, i);
                double duration = tp.getGlobalValue() - prevTime;
                stretchWords(prevIndex, i, duration / defDuration);

                prevTime = tp.getGlobalValue();
                prevIndex = i;
            }
        }

        // TODO: apply relax constraint

        // TODO: fix for relax
        double defDuration = getDefaultWordsDuration(prevIndex, getWords().size());
        if (getEndTime() != TimePeg.VALUE_UNKNOWN)
        {
            double duration = getEndTime() - prevTime;
            stretchWords(prevIndex, getWords().size(), duration / defDuration);
        }
    }

    private void setupSyncs(String textNoSync, String text)
    {
        String words[] = Iterables.toArray(Splitter.on(" ").trimResults().omitEmptyStrings().split(textNoSync.replaceAll("[\\.,;]", " ")),
                String.class);
        numwords = words.length;
        List<SyncAndOffset> syncAndOffsets = BMLTextUtil.getSyncAndOffsetList(text, words.length);

        for (SyncAndOffset s : syncAndOffsets)
        {
            syncs.add(s.getSync());
            syncMap.put(s.getOffset(), s.getSync());
        }
    }

    private double getWordStartTime(int i)
    {
        return getWords().get(i).startTime() - firstWord.startTime();
    }

    @Override
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if (syncMap.inverse().containsKey(syncId))
        {
            int i = syncMap.inverse().get(syncId);
            if (i >= getWords().size())
            {
                return 1;
            }
            return getWordStartTime(i) / getPreferedDuration();
        }
        return super.getRelativeTime(syncId);
    }

    private void updateLipSyncUnit(IU phIU)
    {
        for (IncrementalLipSynchProvider lsp : lsProviders)
        {
            int number = visemeMapping.getVisemeForPhoneme(PhonemeUtil.phonemeStringToInt(phIU.toPayLoad()));
            Visime viseme = new Visime(number, (int) (1000 * (phIU.endTime() - phIU.startTime())), false);
            double fwTime = 0;
            if (firstWord != null)
            {
                fwTime = firstWord.startTime();
            }
            lsp.setLipSyncUnit(getBMLBlockPeg(), behavior, startDelay + phIU.startTime() - fwTime + getStartTime(), viseme, phIU);
            // lsp.setLipSyncUnit(getBMLBlockPeg(), behavior, startDelay + phIU.startTime() - fwTime + getStartTime(), viseme, phIU);
        }
    }

    private void updateFeedback()
    {
        int i = 0;
        IU lastWord = null;
        for (IU word : getWords())
        {
            if (word.isOngoing() || word.isCompleted())
            {
                if (syncMap.containsKey(i))
                {
                    String sync = syncMap.get(i);
                    feedback(sync, startDelay + getStartTime() + word.startTime() - firstWord.startTime());
                }
            }
            lastWord = word;
            i++;
        }
        if (lastWord != null && lastWord.isCompleted())
        {
            if (syncMap.containsKey(i))
            {
                String sync = syncMap.get(i);
                feedback(sync, startDelay + getStartTime() + getDuration());
            }
            feedback("relax", startDelay + getStartTime() + getDuration());
            feedback("end", startDelay + getStartTime() + getDuration());
        }

    }

    private double getIUTime()
    {
        double lastEnd = 0;
        if (synthesisIU != null)
        {
            for (IU word : getWords())
            {
                for (IU ph : word.groundedIn())
                {
                    if (ph.isCompleted())
                    {
                        lastEnd = ph.endTime() - firstWord.startTime();
                    }
                }
            }
        }
        return lastEnd;
    }

    private void updateLipSync()
    {
        int visCount = visemeLookAhead;
        for (WordIU word : getWords())
        {
            if (word.isUpcoming() || word.isOngoing())
            {
                for (IU syllable : word.groundedIn())
                {
                    if (syllable.isUpcoming() || syllable.isOngoing())
                    {
                        for (IU ph : syllable.groundedIn())
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
        }
    }

    // update relax sync point timing to actual word timing
    private void updateRelax()
    {
        if (hesitation != null)
        {
            relaxPeg.setGlobalValue(startDelay + getStartTime() + getDuration());
        }
        else
        {
            relaxPeg.setGlobalValue(getEndTime());
        }
    }

    // update end sync point timing to actual word timing
    private void updateEnd()
    {
        endPeg.setGlobalValue(getStartTime() + getDuration() + startDelay);
    }

    private double getDuration()
    {
        if (lastWord == null) return 0;
        return lastWord.endTime() - firstWord.startTime();
    }

    private class WordUpdateListener implements IUUpdateListener
    {
        @Override
        public void update(IU updatedIU)
        {
            // startDelay = (iuManager.getCurrentTime() - getStartTime()) - getIUTime();
            updateEnd();
            updateRelax();
            updateSyncTiming();
            updateLipSync();
            updateFeedback();
        }
    }

    private void stretch(float value)
    {
        if (stretch == value) return;

        // TODO: update timing of syncs

        stretch = value;
        log.debug("set stretch {}", value);

        for (SegmentIU seg : synthesisIU.getSegments())
        {
            SysSegmentIU sseg = (SysSegmentIU) seg;
            if (!seg.isCompleted() && !seg.isOngoing())
            {
                log.debug("setting segment stretch {}", value);
                sseg.stretchFromOriginal(value);
            }
        }
    }

    private void setLoudness(float value)
    {
        loudness = (int) value;
        iuManager.setLoudness(loudness);
    }

    private void setPitch(float value)
    {
        if (pitchShiftInCent == value) return;
        pitchShiftInCent = value;
        for (SegmentIU seg : synthesisIU.getSegments())
        {
            SysSegmentIU sseg = (SysSegmentIU) seg;
            if (!seg.isCompleted() && !seg.isOngoing())
            {
                sseg.pitchShiftInCent = value;
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
        else if (paramId.equals("loudness"))
        {
            return loudness;
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
        else if (paramId.equals("loudness"))
        {
            setLoudness(value);
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
        double duration = getDuration();
        if (hesitation != null)
        {
            duration += hesitation.duration();
        }
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
        pegs.put(syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        if (startPeg.getGlobalValue() >= endPeg.getGlobalValue() && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        if (startPeg.getGlobalValue() >= relaxPeg.getGlobalValue() && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        if (relaxPeg.getGlobalValue() > endPeg.getGlobalValue() && relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN
                && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) return false;
        return true;
    }

    // update sync point timing to actual word timing
    private void updateSyncTiming()
    {
        for (Entry<Integer, String> entry : syncMap.entrySet())
        {
            if (entry.getKey() < getWords().size())
            {
                WordIU wordAfter = getWords().get(entry.getKey());
                pegs.get(entry.getValue()).setGlobalValue(startDelay + getStartTime() + wordAfter.startTime() - firstWord.startTime());
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
        if (iuManager.isPending(getBMLId()))
        {
            scheduled = false;
            return;
        }
        if (!scheduled)// && time > getStartTime() - 3d)
        {
            updateEnd();
            updateRelax();
            updateSyncTiming();
            iuManager.justInTimeAppendIU(synthesisIU, hesitation, this);
        }

    }

    public void addedToBuffer()
    {
        // inbetween reset
        if (synthesisIU != null && synthesisIU.groundedIn().isEmpty()) return;

        if (synthesisIU != null)
        {
            firstWord = synthesisIU.getWords().get(0);
            for (IU word : synthesisIU.getWords())
            {
                word.updateOnGrinUpdates();
                word.addUpdateListener(wul);
                lastWord = word;
            }
        }
        if (hesitation != null)
        {
            for (IU word : hesitation.getWords())
            {
                word.updateOnGrinUpdates();
                word.addUpdateListener(wul);
            }
            if (firstWord == null)
            {
                firstWord = hesitation.getWords().get(0);
            }
        }

        if (isPlaying())
        {
            // startDelay = (iuManager.getCurrentTime() - getStartTime()) - getIUTime();

            // TODO: re-enable once sync stretching properly updates timing of syncs
            // applyTimeConstraints();

            endPeg.setGlobalValue(iuManager.getCurrentTime() + getPreferedDuration());
            relaxPeg.setGlobalValue(getEndTime());

            updateSyncTiming();
            updateLipSync();
        }
    }

    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        endPeg.setGlobalValue(time + getPreferedDuration());
        relaxPeg.setGlobalValue(getEndTime());

        iuManager.setLoudness(0);
        feedback("start", time);

        scheduled = true;
        iuManager.playIU(synthesisIU, hesitation, this);
        updateSyncTiming();
        updateLipSync();

        super.startUnit(time);
    }

    @Override
    protected void relaxUnit(double time)
    {
        log.debug("incremental speech relax {}", time);
        feedback("relax", time);
    }

    @Override
    protected void playUnit(double time)
    {

    }

    @Override
    public void interrupt(double time) throws TimedPlanUnitPlayException
    {
        super.stop(time);
        // iuManager.stopAfterOngoingWord();
        iuManager.stopAfterOngoingPhoneme();
    }

    @Override
    protected void stopUnit(double time)
    {
        log.debug("incremental speech end {}", time);
        if (getState() == TimedPlanUnitState.IN_EXEC)
        {
            if (syncMap.get(numwords) != null)
            {
                feedback(syncMap.get(numwords), time);
            }
            if (time > getRelaxTime())
            {
                feedback("relax", time);
            }
        }
        // TODO: send feedback for syncs
        iuManager.removeUnit(this);
        feedback("end", time);
    }

}
