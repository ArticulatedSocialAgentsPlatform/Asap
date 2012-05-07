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

import hmi.bml.BMLGestureSync;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.*;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.tts.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.speechengine.ttsbinding.TTSBinding;

/**
 * A TimedPlanUnit that contains speech played back through a TTS system, 
 * either directly or via a .wav file
 * @author Herwin
 */
public abstract class TimedTTSUnit extends TimedAbstractSpeechUnit
{
    // TODO: replace all bookmark specific stuff to generic PlanUnit calls

    protected TTSBinding ttsBinding;

    private double duration;

    protected List<Bookmark> bookmarks;

    protected List<Visime> visimes;

    public int prevVisime;

    public int curVisime;

    public int nextVisime;

    public double visimeDuration;

    private final Class<? extends Behaviour> behaviourClass;

    protected TreeMap<Bookmark, TimePeg> pegs = new TreeMap<Bookmark, TimePeg>();

    private static Logger logger = LoggerFactory.getLogger(TimedTTSUnit.class.getName());

    public Class<? extends Behaviour> getBehaviourClass()
    {
        return behaviourClass;
    }

    public TimedTTSUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass)
    {
        super(bfm, bbPeg, text, bmlId, id);
        ttsBinding = ttsBin;
        bookmarks = new ArrayList<Bookmark>();
        visimes = new ArrayList<Visime>();
        behaviourClass = behClass;
    }

    public TimePeg getBookMarkTimePeg(String bmid)
    {
        for (Bookmark b : bookmarks)
        {
            if (b.getName().equals(bmid))
            {
                if (pegs.get(b) != null)
                {
                    return pegs.get(b);
                }
            }
        }
        return null;
    }

    public double getBookMarkTime(Bookmark b)
    {
        TimePeg p = pegs.get(b);
        if (p == null) return TimePeg.VALUE_UNKNOWN;
        return p.getGlobalValue();
    }

    public TimePeg getBookMarkTimePeg(Bookmark b)
    {
        if (pegs.get(b) != null)
        {
            return pegs.get(b);
        }
        return null;
    }

    private double getBookMarkTime(String bmid)
    {
        for (Bookmark b : bookmarks)
        {
            if (b.getName().equals(bmid))
            {
                if (pegs.get(b) != null)
                {
                    return pegs.get(b).getGlobalValue();
                }
            }
        }
        return TimePeg.VALUE_UNKNOWN;
    }

    private Bookmark getBookMark(String syncId)
    {
        for (Bookmark b : bookmarks)
        {
            if (b.getName().equals(syncId))
            {
                return b;
            }
        }
        return null;
    }

    @Override
    public double getTime(String syncId)
    {
        if (getBookMarkTime(syncId) != TimePeg.VALUE_UNKNOWN) return getBookMarkTime(syncId);
        return super.getTime(syncId);
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (getBookMarkTimePeg(syncId) != null) return getBookMarkTimePeg(syncId);
        if (syncId.equals("start")) return getStartPeg();
        if (syncId.equals("end")) return getEndPeg();
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (getBookMark(syncId) != null)
        {
            setTimePeg(getBookMark(syncId), peg);
        }

        if (BMLGestureSync.isBMLSync(syncId))
        {
            if (BMLGestureSync.get(syncId).isAfter(BMLGestureSync.STROKE))
            {
                setEnd(peg);
            }
            else
            {
                setStart(peg);
            }
        }
        else
        {
            logger.warn("Can't set TimePeg on non-BML, non-Bookmark sync {}", syncId);
        }
    }

    @Override
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if (getBookMarkTime(syncId) != TimePeg.VALUE_UNKNOWN)
        {
            return (getBookMarkTime(syncId) - getStartTime()) / getPreferedDuration();
        }
        return super.getRelativeTime(syncId);
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> availableSyncs = new ArrayList<String>();
        availableSyncs.add("start");
        for (Bookmark b : getBookmarks())
        {
            availableSyncs.add(b.getName());
        }
        availableSyncs.add("end");
        return availableSyncs;
    }

    @Override
    public boolean hasValidTiming()
    {
        if (getStartTime() > getEndTime())
        {
            return false;
        }
        else if (Math.abs((getEndTime() - getStartTime()) - getPreferedDuration()) > 0.0001)
        {
            logger.debug("End time: {}", getEndTime());
            logger.debug("Start time: ", getStartTime());
            logger.debug("End-start: ", (getEndTime() - getStartTime()));
            logger.debug("Duration: ", getPreferedDuration());
            return false;
        }

        for (Bookmark b : getBookmarks())
        {
            double bmTime = getBookMarkTime(b);
            if (bmTime != TimePeg.VALUE_UNKNOWN && Math.abs((b.getOffset() * 0.001 + getStartTime()) - bmTime) > 0.0001)
            {
                logger.debug("(b.offset*0.001+getStartTime())= {}", (b.getOffset() * 0.001 + getStartTime()));
                logger.debug("bookmarktime= {}", bmTime);
                return false;
            }
        }
        return true;
    }

    /**
     * @return Prefered duration (in seconds) of this speech unit (call setup before calling this)
     */
    @Override
    public double getPreferedDuration()
    {
        return duration;
    }

    protected abstract TimingInfo getTiming() throws SpeechUnitPlanningException;

    /**
     * @throws SpeechUnitPlanningException
     *             if cache setup failed.
     */
    protected void setupCache() throws SpeechUnitPlanningException
    {
    }

    /**
     * Finds relative bookmark timing and prefered duration of the speech unit
     * 
     * @throws SpeechUnitPlanningException
     */
    public synchronized void setup() throws SpeechUnitPlanningException
    {
        synchronized (ttsBinding)
        {
            ttsBinding.setCallback(null);
            TimingInfo ti = getTiming();
            setupCache();

            duration = ti.getDuration();
            bookmarks.clear();
            bookmarks = ti.getBookmarks();
            visimes.clear();
            visimes = ti.getVisimes();
        }
    }

    /**
     * Send progress feedback for all bookmarks passed at playTime.
     * 
     * @param playTime
     *            time since start of the speech unit
     * @param time
     *            time since start of BML execution
     */
    protected abstract void sendProgress(double playTime, double time);

    public void setTimePeg(Bookmark bm, TimePeg sp)
    {
        pegs.put(bm, sp);
    }

    /**
     * Get the processed bookmarks in the speechtext (call setup before calling this)
     * 
     * @return the bookmarks
     */
    public List<Bookmark> getBookmarks()
    {
        return bookmarks;
    }

    /**
     * Get the processed visimes in the speechtext (call setup before calling this)
     * 
     * @return the visimes
     */
    public List<Visime> getVisimes()
    {
        return visimes;
    }
}
