/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import hmi.tts.Bookmark;
import hmi.tts.TimingInfo;
import hmi.tts.Visime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLGestureSync;
import saiba.bml.core.Behaviour;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.speechengine.ttsbinding.TTSBinding;

import com.google.common.collect.ImmutableList;

/**
 * A TimedPlanUnit that contains speech played back through a TTS system, 
 * either directly or via a .wav file
 * @author Herwin
 */
public abstract class TimedTTSUnit extends TimedAbstractSpeechUnit
{
    protected TTSBinding ttsBinding;

    private double duration;

    protected List<Bookmark> bookmarks;

    protected List<Visime> visimes;

    public int prevVisime;

    public int curVisime;

    public int nextVisime;

    public double visimeDuration;

    private final Class<? extends Behaviour> behaviourClass;

    protected Map<Bookmark, TimePeg> pegs = new HashMap<Bookmark, TimePeg>();

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
        else if (BMLGestureSync.isBMLSync(syncId))
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
            visimes.addAll(ti.getVisimes());
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
        List<Visime> list;
        synchronized(ttsBinding)
        {
            list = ImmutableList.copyOf(visimes);
        }
        return list;
    }
}
