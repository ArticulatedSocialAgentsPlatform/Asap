/*******************************************************************************
 *******************************************************************************/
package asap.textengine.lipsync;

import hmi.tts.TTSTiming;
import hmi.tts.Visime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.textengine.TextOutput;
import asap.textengine.TimedSpeechTextUnit;

/**
 * LipSynchProvider implementation for the text engine. This basically provides
 * subtitles to go along with the TTS from the regular speech engine.
 *
 * @author Jordi Hendrix
 */
public class TimedSpeechTextUnitLipSynchProvider implements LipSynchProvider
{

    private final PlanManager<TimedSpeechTextUnit> textPlanManager;
    private static Logger logger = LoggerFactory.getLogger(TimedSpeechTextUnitLipSynchProvider.class.getName());
    private TextOutput textOutput = null;

    public TimedSpeechTextUnitLipSynchProvider(PlanManager<TimedSpeechTextUnit> textPlanManager, TextOutput to)
    {
        this.textPlanManager = textPlanManager;
        this.textOutput = to;
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, TTSTiming timing)
    {
        double totalDuration = 0d;

        for (Visime vis : timing.getVisimes())
        {
            totalDuration += vis.getDuration();
        }

        // Get the text from the behaviour
        SpeechBehaviour sb = (SpeechBehaviour) beh;
        String text = sb.getContent();

        TimedSpeechTextUnit ttsu = new TimedSpeechTextUnit(bbPeg, text, beh.getBmlId(), beh.id, textOutput);
        ttsu.setSubUnit(true);
        textPlanManager.addPlanUnit(ttsu);
        TimePeg startPeg = new OffsetPeg(bs.getTimePeg("start"), 0d);
        ttsu.setTimePeg("start", startPeg);
        TimePeg endPeg = new OffsetPeg(bs.getTimePeg("start"), totalDuration / 1000d);
        ttsu.setTimePeg("end", endPeg);
        logger.debug("Adding subtitle at {}-{}", ttsu.getStartTime(), ttsu.getEndTime());
    }
}
