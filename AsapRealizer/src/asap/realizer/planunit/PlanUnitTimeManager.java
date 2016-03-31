/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLGestureSync;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.pegboard.TimePeg;

/**
 * Keeps track of time pegs and keypositions and maps absolute time to relative time.
 * @author Herwin
 */
public class PlanUnitTimeManager
{
    private Map<KeyPosition, TimePeg> pegs = new TreeMap<KeyPosition,TimePeg>();
    
    private final KeyPositionManager kpManager;
    
    private static final Logger logger = LoggerFactory.getLogger(PlanUnitTimeManager.class.getName());
    
    public PlanUnitTimeManager(KeyPositionManager kpm)
    {
        kpManager = kpm;
    }
    
    public void setPegs(Map<KeyPosition, TimePeg>pegs)
    {
        this.pegs = pegs;
    }
    
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if(kpManager.getKeyPosition(syncId)!=null)
        {
            return kpManager.getKeyPosition(syncId).time;
        }
        //TODO: somehow hook up with real bmlId, behaviorId
        throw new SyncPointNotFoundException("","",syncId);
    }
    
    /**
     * @return the synchs
     */
    public Map<KeyPosition, TimePeg> getPegs()
    {
        return pegs;
    }
    
    public boolean hasValidTiming()
    {
        double prevTime = 0;
        for(KeyPosition kp:pegs.keySet())
        {
            TimePeg p = pegs.get(kp);
            if(p.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
            {
                if(p.getGlobalValue()<prevTime)
                {
                    return false;
                }
                else
                {
                    prevTime = p.getGlobalValue();
                }
            }
        }
        return true;
    }    
    
    /**
     * Gets the keyposition associated with sp
     * @param sp
     * @return
     */
    public KeyPosition getKeyPosition(TimePeg sp)
    {
        for(KeyPosition kp:pegs.keySet())
        {
            if(pegs.get(kp)==sp)return kp;
            if(pegs.get(kp)==sp.getLink())return kp;
            if(pegs.get(kp).getLink()==sp)return kp;
        }
        return null;
    }
    
    
    
    /**
     * Get the time of the first set timepeg before the timepeg linked to this keyposition
     * @param pid id of the keyposition
     * @return the time of the first set timepeg before the timepeg linked to this keyposition, TimePeg.VALUE_UNKNOWN if none set or pid unknown
     */
    public double getPrevPegTime(String pid)
    {
        KeyPosition kpCur = kpManager.getKeyPosition(pid);
        boolean getNext = false;
        BMLGestureSync curSync = BMLGestureSync.get(pid);   
        ListIterator<KeyPosition> listIter = kpManager.getKeyPositions().listIterator(kpManager.getKeyPositions().size());
        while(listIter.hasPrevious())
        {
            KeyPosition kp = listIter.previous();
            BMLGestureSync sync = BMLGestureSync.get(kp.id);
            if(curSync!=null && sync!=null)
            {
                if(sync.isBefore(curSync))
                {
                    getNext = true;
                }
            }
            if(getNext)
            {
                TimePeg tp = pegs.get(kp);
                if(tp!=null && tp.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
                {
                    return tp.getGlobalValue();
                }
            }
            if(kp==kpCur)
            {
                getNext = true;
            }
        }
        return TimePeg.VALUE_UNKNOWN;
    }
    
    /**
     * Get the timePeg after pid that has a value != TimePeg.VALUE_UNKNOWN
     * @return the TimePeg, null if none
     */
    public TimePeg getNextSetTimePeg(String pid)
    {
        KeyPosition kpCur = kpManager.getKeyPosition(pid);
        boolean getNext = false;
        BMLGestureSync curSync = BMLGestureSync.get(pid);   
        for(KeyPosition kp:pegs.keySet())
        {
            BMLGestureSync sync = BMLGestureSync.get(kp.id);
            if(curSync!=null && sync!=null)
            {
                if(sync.isAfter(curSync))
                {
                    getNext = true;
                }
            }
            if(getNext)
            {
                TimePeg tp = pegs.get(kp);
                if(tp!=null && tp.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
                {
                    return tp;
                }
            }
            if(kp==kpCur)
            {
                getNext = true;
            }            
        }
        return null;
    }
    /**
     * Get the time of the next set timepeg after the timepeg linked to this keyposition
     * @param pid id of the keyposition
     * @return the time of the next set timepeg after the timepeg linked to this keyposition, TimePeg.VALUE_UNKNOWN if none set
     */
    public double getNextPegTime(String pid)
    {
        TimePeg tp = getNextSetTimePeg(pid);
        if(tp==null)return TimePeg.VALUE_UNKNOWN;
        return tp.getGlobalValue();        
    }
    
    public double getTime(String pid)
    {
        KeyPosition kp = kpManager.getKeyPosition(pid);
        if(kp==null)
        {
            return TimePeg.VALUE_UNKNOWN;
        }
        TimePeg p = pegs.get(kp);
        if(p==null)return TimePeg.VALUE_UNKNOWN;
        return p.getGlobalValue();        
    }
    
    public TimePeg getTimePeg(String pid)
    {
        KeyPosition kp = kpManager.getKeyPosition(pid);
        if(kp!=null)
        {
            return pegs.get(kp);
        }
        return null;
    }
    
    public double getStartTime()
    {
        if(kpManager.getKeyPosition("start")==null)
        {
            logger.warn("Null start key on behavior");
            return TimePeg.VALUE_UNKNOWN;
        }
        TimePeg tp = pegs.get(kpManager.getKeyPosition("start"));
        if(tp!=null)
        {
            return tp.getGlobalValue();
        }
        return TimePeg.VALUE_UNKNOWN;
    }
    
    public double getEndTime()
    {
        if(kpManager.getKeyPosition(BMLGestureSync.END.getId())==null)
        {
            return TimePeg.VALUE_UNKNOWN;
        }
        TimePeg tp = pegs.get(kpManager.getKeyPosition(BMLGestureSync.END.getId()));
        
        if(tp!=null)
        {
            return tp.getGlobalValue();
        }
        return TimePeg.VALUE_UNKNOWN;
    }
    
    public double getRelaxTime()
    {
        if(kpManager.getKeyPosition(BMLGestureSync.RELAX.getId())==null)
        {
            return getEndTime();
        }
        TimePeg tp = pegs.get(kpManager.getKeyPosition(BMLGestureSync.RELAX.getId()));
        
        if(tp!=null)
        {
            return tp.getGlobalValue();
        }
        return getEndTime();
    }
    
    public void setTimePeg(KeyPosition kp, TimePeg sp)
    {
        pegs.put(kp, sp);        
    }
    
    public void setTimePeg(String kid, TimePeg sp)
    {
        KeyPosition kp = kpManager.getKeyPosition(kid);
        //TODO: handle kp == null
        pegs.put(kp, sp);        
    }
    
    public void resolveStartAndEndKeyPositions()
    {
        KeyPosition kp = kpManager.getKeyPosition("start");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("start", 0, 0));
        }
        kp = kpManager.getKeyPosition("end");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("end", 1, 1));
        }
    }
    
    public void resolveFaceKeyPositions()
    {
        resolveStartAndEndKeyPositions();
        KeyPosition kp = kpManager.getKeyPosition("attackPeak");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("attackPeak", 0.25, 0.01));
        }
        
        kp = kpManager.getKeyPosition("relax");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("relax", 0.75, 0.01));
        }
    }
    
    public void resolvePostureKeyPositions()
    {
        resolveStartAndEndKeyPositions();
        KeyPosition kp = kpManager.getKeyPosition("ready");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("ready", 0.25, 0.01));
        }
        
        kp = kpManager.getKeyPosition("relax");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("relax", 0.75, 0.01));
        }
    }
    
    public void resolveGazeKeyPositions()
    {
        resolveStartAndEndKeyPositions();
        KeyPosition kp = kpManager.getKeyPosition("ready");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("ready", 0.25, 0.01));
        }
        
        kp = kpManager.getKeyPosition("relax");
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition("relax", 0.75, 0.01));
        }
    }
    
    public void resolveHeadKeyPositions()
    {
        resolveGestureKeyPositions();
    }
    
    /**
     * Fills out default BML keypositions that are not yet in the TimedMotionUnit. 
     * Conventions:
     * missing ready =&gt; ready = start = 0; 
     * missing relax =&gt; relax = end = 1; 
     * missing strokeStart =&gt; strokeStart = ready 
     * missing strokeEnd =&gt; strokeEnd = relax 
     * missing stroke =&gt; stroke = strokeStart
     * 
     */
    public void resolveGestureKeyPositions()
    {
        double left = 0;
        double right = 1;

        resolveStartAndEndKeyPositions();
        KeyPosition kp = kpManager.getKeyPosition(BMLGestureSync.READY.getId());
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), 0, 0.01));
        }
        else
        {
            left = kp.time;
        }

        kp = kpManager.getKeyPosition(BMLGestureSync.RELAX.getId());
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), 1, 0.01));
        }
        else
        {
            right = kp.time;
        }

        kp = kpManager.getKeyPosition(BMLGestureSync.STROKE_START.getId());
        if (kp == null)
        {
            kpManager.addKeyPosition(
                    new KeyPosition(BMLGestureSync.STROKE_START.getId(), left, 0.01));
        }
        else
        {
            left = kp.time;
        }

        kp = kpManager.getKeyPosition(BMLGestureSync.STROKE_END.getId());
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), right, 0.01));
        }

        kp = kpManager.getKeyPosition(BMLGestureSync.STROKE.getId());
        if (kp == null)
        {
            kpManager.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE.getId(), left, 0.01));
        }
    }
    
    /**
     * Get relative time (0..1) using a linear timewarp between the two keypositions that most closely match absolute time
     */
    public double getRelativeTime(double absoluteTime)
    {
        double startSegmentTime = getStartTime();
        double startKeyTime = 0;
        double endSegmentTime = getEndTime();
        double endKeyTime = 1;
        for (KeyPosition k:pegs.keySet())
        {
            TimePeg s = pegs.get(k);
            
            if(s!=null && s.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
            {
                //find first keypos                    
                if(s.getGlobalValue()<=absoluteTime)
                {
                    startSegmentTime = s.getGlobalValue();
                    startKeyTime = k.time;                        
                }
                //find end keypos
                else
                {
                    endSegmentTime = s.getGlobalValue();
                    endKeyTime = k.time;
                    break;
                }
            }               
        }
        
        if(endSegmentTime == TimePeg.VALUE_UNKNOWN)
        {
            return startKeyTime;
        }
        
        //local timewarp            
        double realDuration = endSegmentTime-startSegmentTime;
        double canDuration = endKeyTime - startKeyTime;            
        return ((absoluteTime-startSegmentTime)/realDuration)*canDuration+startKeyTime;        
    }

    public List<String> getAvailableSyncs()
    {
        List<String>availableSyncs = new ArrayList<String>();
        for (KeyPosition k:kpManager.getKeyPositions())
        {
            availableSyncs.add(k.id);
        }
        return availableSyncs;
    }
}
