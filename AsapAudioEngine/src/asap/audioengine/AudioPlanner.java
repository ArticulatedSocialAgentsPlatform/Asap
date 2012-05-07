/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.audioengine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hmi.elckerlyc.*;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.util.Resources;
import hmi.bml.core.Behaviour;
import hmi.bml.ext.bmlt.*;
import hmi.audioenvironment.*;

/**
 * Planner for BMLT audio behaviours
 * @author welberge
 */
public class AudioPlanner extends AbstractPlanner<TimedAbstractAudioUnit>
{
    private Resources audioResource;
    private static Logger logger = LoggerFactory.getLogger(AudioPlanner.class.getName());
    private final SoundManager soundManager;
    private static final double TIMEPEG_TOLERANCE = 0.003;

    public AudioPlanner(FeedbackManager bfm, Resources audioRes, PlanManager<TimedAbstractAudioUnit> planManager, SoundManager soundManager)
    {
        super(bfm, planManager);
        audioResource = audioRes;
        this.soundManager = soundManager;
    }

    private TimedAbstractAudioUnit createAudioUnit(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        BMLTAudioFileBehaviour bAudio = (BMLTAudioFileBehaviour) b;
        TimedAbstractAudioUnit au = new TimedWavAudioUnit(soundManager, fbManager, bbPeg, audioResource.getInputStream(bAudio
                .getStringParameterValue("fileName")), bAudio.getBmlId(), bAudio.id);
        try
        {
            au.setup();
        }
        catch (AudioUnitPlanningException e)
        {
            throw new BehaviourPlanningException(b, e.getLocalizedMessage(), e);
        }
        logger.debug("Creating audio unit {} duration: {}", b.id, au.getPreferedDuration());
        return au;
    }

    /**
     * Creates a AudioUnit that satisfies sacs and adds it to the audio plan. All registered BMLFeedbackListeners are linked to this AudioUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedAbstractAudioUnit au)
            throws BehaviourPlanningException
    {
        validateSyncs(sacs, b);
        ArrayList<SyncAndTimePeg> satp = new ArrayList<SyncAndTimePeg>();
        if (au == null)
        {
            au = createAudioUnit(bbPeg, b);
        }
        
        linkStartAndEnd(b, sacs, au);
        satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", au.getStartPeg()));
        if (au.getEndPeg() != null)
        {
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", au.getEndPeg()));
        }
        planManager.addPlanUnit(au);

        return satp;
    }

    private void linkStartAndEnd(Behaviour b, List<TimePegAndConstraint> sacs, TimedAbstractAudioUnit au)
    {
        // link start and end sync
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                if (sac.offset == 0)
                {
                    au.setStart(sac.peg);

                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    au.setStart(p);
                }
            }
            if (sac.syncId.equals("end"))
            {
                if (sac.offset == 0)
                {

                    au.setEnd(sac.peg);
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    au.setEnd(p);
                }
            }
        }
    }

    private void validateSyncs(List<TimePegAndConstraint> sacs, Behaviour b) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start") && !sac.syncId.equals("end"))
            {
                throw new BehaviourPlanningException(b, "Attempting to synchronize a audiofile behaviour with sync: " + sac.syncId
                        + " other than start or end");
            }
        }
    }

    @Override
    public TimedAbstractAudioUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        TimedAbstractAudioUnit au = createAudioUnit(bbPeg, b);
        double startTime = bbPeg.getValue();
        boolean startFound = false;

        validateSyncs(sacs, b);

        // resolve start time
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start") && sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                startTime = sac.peg.getGlobalValue() - sac.offset;
                startFound = true;
            }
        }

        if (!startFound)
        {
            for (TimePegAndConstraint sac : sacs)
            {
                if (sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (sac.syncId.equals("end") && sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                    {
                        startTime = sac.peg.getGlobalValue() - au.getPreferedDuration() - sac.offset;
                        break;
                    }
                }
            }
        }

        // resolve start and end
        TimePegAndConstraint sacNotStart = null;
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start"))
            {
                sacNotStart = sac;
                break;
            }
        }

        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("end"))
            {
                if (sac.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    sac.peg.setGlobalValue(au.getPreferedDuration() + startTime + sac.offset);
                }
                else if (Math.abs(sac.peg.getGlobalValue() - (startTime + au.getPreferedDuration() + sac.offset)) > TIMEPEG_TOLERANCE)
                {
                    throw new BehaviourPlanningException(b, "Stretching audio fragments is not supported yet. "
                            + "Should not be too hard to do, though." + " Behavior omitted.");
                }
            }

            if (sac.syncId.equals("start") && sac.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                if (sac.resolveAsStartOffset)
                {
                    OffsetPeg p = (OffsetPeg) sac.peg;
                    p.setLink(sacNotStart.peg);
                    p.setOffset(startTime - sacNotStart.peg.getGlobalValue());
                }
                else
                {
                    sac.peg.setGlobalValue(startTime + sac.offset);
                }
            }
        }
        linkStartAndEnd(b, sacs, au);
        return au;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLTAudioFileBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLTAudioFileBehaviour.class);
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 1;
    }
    
    @Override
    public void shutdown()
    {
        for(TimedAbstractAudioUnit au:planManager.getPlanUnits())
        {
            au.cleanup();
        }
    }
}
