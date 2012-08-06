/**
 * *****************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the
 * Netherlands
 *
 * This file is part of the Elckerlyc BML realizer.
 *
 * Elckerlyc is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Elckerlyc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Elckerlyc. If not, see http://www.gnu.org/licenses/.
 * ****************************************************************************
 */
package asap.textengine.lipsync;

import hmi.tts.Visime;

import java.util.List;

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
public class TimedSpeechTextUnitLipSynchProvider implements LipSynchProvider {

    private final PlanManager<TimedSpeechTextUnit> textPlanManager;
    private static Logger logger = LoggerFactory.getLogger(TimedSpeechTextUnitLipSynchProvider.class.getName());
    private TextOutput textOutput = null;

    public TimedSpeechTextUnitLipSynchProvider(PlanManager<TimedSpeechTextUnit> textPlanManager, TextOutput to) {
        this.textPlanManager = textPlanManager;
        this.textOutput = to;
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, List<Visime> visemes) {
        double totalDuration = 0d;

        for (Visime vis : visemes) {
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
