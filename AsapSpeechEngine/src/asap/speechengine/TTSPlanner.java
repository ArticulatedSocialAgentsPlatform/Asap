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
package asap.speechengine;

import hmi.tts.Bookmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.speechengine.ttsbinding.TTSBinding;

/**
 * Planner that creates and plans TimedTTSUnits from e.g. SpeechBehaviours.
 * @author hvanwelbergen
 * 
 */
public class TTSPlanner extends AbstractPlanner<TimedTTSUnit>
{
    private static Logger logger = LoggerFactory.getLogger(TTSPlanner.class.getName());
    private final TTSBinding ttsBinding;
    private final TimedTTSUnitFactory suFactory;
    private static final double TIMEPEG_TOLERANCE = 0.003;
    private Collection<LipSynchProvider> lipSynchers = new ArrayList<LipSynchProvider>();

    @Override
    public String toString()
    {
        return getClass().getName() + "[" + ttsBinding.getClass().getName() + ", " + suFactory.getClass().getName() + "]";
    }

    public void addLipSyncher(LipSynchProvider ls)
    {
        lipSynchers.add(ls);
    }

    public TTSPlanner(FeedbackManager bfm, TimedTTSUnitFactory suf, TTSBinding ttsBin, PlanManager<TimedTTSUnit> planManager)
    {
        super(bfm, planManager);
        suFactory = suf;
        ttsBinding = ttsBin;
    }

    @Override
    public void shutdown()
    {
        ttsBinding.cleanup();
    }

    public void setSpeaker(String speaker)
    {
        synchronized (ttsBinding)
        {
            ttsBinding.setVoice(speaker);
        }
    }

    public String[] getVoices()
    {
        synchronized (ttsBinding)
        {
            return ttsBinding.getVoices();
        }
    }

    private TimedTTSUnit createSpeechUnit(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        try
        {
            SpeechBehaviour bSpeech = (SpeechBehaviour) b;

            String voice = null;
            if(bSpeech.specifiesParameter(BMLTBehaviour.BMLTNAMESPACE + ":" + "voice"))
            {
                voice = bSpeech.getStringParameterValue(BMLTBehaviour.BMLTNAMESPACE + ":" + "voice");
            }
            
            // TODO: ultimately, this may be the characterId from the behavior -- but remember that characterId may be empty
            String voiceId = "voice1";
            
            TimedTTSUnit bs = suFactory.createTimedTTSUnit(bbPeg, bSpeech.getContent(), voiceId, bSpeech.getBmlId(), bSpeech.id,
                    ttsBinding, b.getClass());

            synchronized (ttsBinding)
            {
                String oldVoice = ttsBinding.getVoice();
                if (voice != null)
                {
                    ttsBinding.setVoice(voice);
                }
                bs.setup();
                if (voice!=null && !voice.equals(oldVoice))
                {
                    ttsBinding.setVoice(oldVoice);
                }          
            }
            logger.debug("Created speech unit {} duration: {}", b.id, bs.getPreferedDuration());
            return bs;
        }
        catch (SpeechUnitPlanningException e)
        {
            throw new BehaviourPlanningException(b, e.getLocalizedMessage(), e);
        }
    }

    private void validateSacs(Behaviour b, TimedTTSUnit bs, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (!bs.hasSync(sac.syncId)) throw new BehaviourPlanningException(b, "Invalid synchronization constraint " + sac + " syncId "
                    + sac.syncId + " not found in speech unit");
        }
    }

    @Override
    public TimedTTSUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        TimedTTSUnit bs = createSpeechUnit(bbPeg, b);
        validateSacs(b, bs, sacs);

        double startTime = bbPeg.getValue();
        boolean startFound = false;

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
                    for (Bookmark bm : bs.getBookmarks())
                    {
                        if (bm.getName().equals(sac.syncId))
                        {
                            startTime = sac.peg.getGlobalValue() - bm.getOffset() * 0.001 - sac.offset;
                            logger.debug("Setting start time based on bookmark {} , startTime: {}, sac.offset: {}, bm.offset: {}",
                                    new Object[] { sac.syncId, startTime, sac.offset, bm.getOffset() });
                            break;
                        }
                    }
                    if (sac.syncId.equals("end") && sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                    {
                        startTime = sac.peg.getGlobalValue() - bs.getPreferedDuration() - sac.offset;
                        break;
                    }
                }
            }
        }

        // resolve synchronization points linked to bookmarks
        linkBookmarks(bs, sacs, startTime, b);

        // resolve start and end
        TimePegAndConstraint sacNotStart = null;

        // find a random TimePegAndConstraint to link to the start
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
                    sac.peg.setGlobalValue(bs.getPreferedDuration() + startTime + sac.offset);
                }
                else if (Math.abs(sac.peg.getGlobalValue() - (startTime + bs.getPreferedDuration() + sac.offset)) > TIMEPEG_TOLERANCE)
                {
                    throw new BehaviourPlanningException(b,
                            "Stretching speech is not supported yet. Possibly this can be solved by moving the speech behavior up in the BML spec?"
                                    + " Behavior omitted.");
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
        linkStartAndEnd(b, sacs, bs);
        return bs;
    }

    /**
     * Creates a SpeechUnit that satisfies sacs and adds it to the motion plan. All registered BMLFeedbackListeners are linked to this SpeechUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedTTSUnit bs)
            throws BehaviourPlanningException
    {
        if (bs == null)
        {
            bs = createSpeechUnit(bbPeg, b);
        }

        validateSacs(b, bs, sacs);

        linkStartAndEnd(b, sacs, bs);
        //ArrayList<SyncAndTimePeg> satp = new ArrayList<SyncAndTimePeg>();
        //satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", bs.getStartPeg()));
//        if (bs.getEndPeg() == null)
//        {
//            bs.setEnd(new OffsetPeg(bs.getStartPeg(), bs.getPreferedDuration()));
//        }
        //satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", bs.getEndPeg()));
        linkBookmarks(bs, sacs, bs.getStartTime(), b);

        // find pegs
        /*
        for (Bookmark bm : bs.getBookmarks())
        {
            TimePeg p = bs.getBookMarkTimePeg(bm);
            if (p != null)
            {
                satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, bm.getName(), p));
            }
        }
        */
        List<SyncAndTimePeg> satp = constructSyncAndTimePegs(bbPeg, b, bs); 

        for (LipSynchProvider ls : lipSynchers)
        {
            ls.addLipSyncMovement(bbPeg, b, bs, bs.visimes);
        }
        planManager.addPlanUnit(bs);

        return satp;
    }

    private void linkStartAndEnd(Behaviour b, List<TimePegAndConstraint> sacs, TimedTTSUnit bs)
    {
        // link start and end sync
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                if (sac.offset == 0)
                {
                    bs.setStart(sac.peg);
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    bs.setStart(p);
                }
            }
            if (sac.syncId.equals("end"))
            {
                if (sac.offset == 0)
                {
                    bs.setEnd(sac.peg);
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    bs.setEnd(p);
                }
            }
        }
    }

    // link synchpoints in sacs to bookmark times
    private void linkBookmarks(TimedTTSUnit su, List<TimePegAndConstraint> sacs, double startTime, Behaviour b)
            throws BehaviourPlanningException
    {
        for (Bookmark bm : su.getBookmarks())
        {
            for (TimePegAndConstraint sac : sacs)
            {
                if (sac.syncId.equals(bm.getName()))
                {
                    if (sac.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                    {
                        logger.debug("Setting time for bookmark {} : {}", bm.getName(), (-sac.offset + startTime + bm.getOffset() * 0.001));
                        sac.peg.setGlobalValue(sac.offset + startTime + bm.getOffset() * 0.001);
                        if (sac.offset == 0)
                        {
                            su.setTimePeg(bm, sac.peg);
                        }
                        else
                        {
                            su.setTimePeg(bm, new OffsetPeg(sac.peg, -sac.offset));
                        }
                    }
                    else
                    {
                        if (Math.abs((sac.peg.getGlobalValue() - sac.offset) - (startTime + bm.getOffset() * 0.001)) > 0.1)
                        {
                            throw new BehaviourPlanningException(b, "Can't set bookmark timing for bookmark: " + bm.getName()
                                    + ", functionality not yet supported. Desired time: " + (sac.peg.getGlobalValue() - sac.offset)
                                    + " Speech time: " + (startTime + bm.getOffset() * 0.001f) + ". Behavior omitted.");
                        }
                        else
                        {
                            if (sac.offset == 0)
                            {
                                su.setTimePeg(bm, sac.peg);
                            }
                            else
                            {
                                OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                                su.setTimePeg(bm, p);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(SpeechBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        return ttsBinding.getSupportedBMLDescriptionExtensions();
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 1;
    }
}
