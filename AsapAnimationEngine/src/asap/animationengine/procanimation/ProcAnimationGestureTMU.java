package asap.animationengine.procanimation;

import hmi.bml.BMLGestureSync;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.OffsetPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.motionunit.TMUPlayException;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.planunit.Priority;

/**
 * TimedMotionUnit for ProcAnimationGestureMU
 * @author Herwin
 * 
 */
@Slf4j
public class ProcAnimationGestureTMU extends TimedMotionUnit
{
    private final ProcAnimationGestureMU mu;

    public ProcAnimationGestureTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, ProcAnimationGestureMU m)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m);
        setPriority(Priority.GESTURE);
        mu = m;
    }

    // TODO: more or less duplicate with LinearStretchResolver
    private void linkSynchs(List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String syncId : getAvailableSyncs())
            {
                if (s.syncId.equals(syncId))
                {
                    if (s.offset == 0)
                    {
                        setTimePeg(syncId, s.peg);
                    }
                    else
                    {
                        setTimePeg(syncId, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    // TODO: more or less duplicate with LinearStretchResolver
    private void validateSyncs(Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint s : sac)
        {
            if (!getAvailableSyncs().contains(s.syncId))
            {
                throw new BehaviourPlanningException(b, "Sync id " + s.syncId + " not available for TimedPlanUnit " + this);
            }
        }
    }

    private TimePegAndConstraint getTimePeg(String id, List<TimePegAndConstraint> sac)
    {
        for (TimePegAndConstraint tpac : sac)
        {
            if (tpac.syncId.equals(id)) return tpac;
        }
        return null;
    }

    public double getPreparationDuration()
    {
        return mu.getPreparationDuration();
    }

    public double getRetractionDuration()
    {
        return mu.getRetractionDuration();
    }

    private double resolveLeftRight(double defaultDuration, TimePeg pegLeft, TimePeg pegRight)
    {
        if (pegLeft != null && pegLeft.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            if (pegRight != null)
            {
                if (pegRight.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    pegRight.setGlobalValue(pegLeft.getGlobalValue() + defaultDuration);
                }
                else
                {
                    return pegRight.getGlobalValue() - pegLeft.getGlobalValue();
                }
            }
        }
        else if (pegRight != null && pegRight.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            if (pegLeft != null)
            {
                if (pegLeft.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    pegLeft.setGlobalValue(pegRight.getGlobalValue() - defaultDuration);
                }
                else
                {
                    return pegRight.getGlobalValue() - pegLeft.getGlobalValue();
                }
            }
        }
        return defaultDuration;
    }

    private double resolveLeftRight(double defaultDuration, TimePegAndConstraint sacLeft, TimePegAndConstraint sacRight)
    {
        TimePeg pegLeft = sacLeft == null ? null : sacLeft.peg;
        TimePeg pegRight = sacRight == null ? null : sacRight.peg;
        return resolveLeftRight(defaultDuration, pegLeft, pegRight);
    }

    private void initializeAllTimePegs()
    {
        TimePeg startPeg = getTimePeg(BMLGestureSync.START.getId());
        TimePeg readyPeg = getTimePeg(BMLGestureSync.READY.getId());
        TimePeg strokeStartPeg = getTimePeg(BMLGestureSync.STROKE_START.getId());
        TimePeg strokeEndPeg = getTimePeg(BMLGestureSync.STROKE_END.getId());
        TimePeg strokePeg = getTimePeg(BMLGestureSync.STROKE.getId());
        TimePeg relaxPeg = getTimePeg(BMLGestureSync.RELAX.getId());
        TimePeg endPeg = getTimePeg(BMLGestureSync.END.getId());
        
        // 1a. preStroke constraints
        double preStrokeHoldDuration = resolveLeftRight(mu.getPreStrokeHoldDuration(), readyPeg, strokeStartPeg);
        // 1b postStroke constraints
        double postStrokeHoldDuration = resolveLeftRight(mu.getPostStrokeHoldDuration(), strokeEndPeg, relaxPeg);
        
        // 2 resolve stroke
        double strokeStartDuration = mu.getStrokeDuration() * mu.getRelativeStrokePos();
        double strokeEndDuration = mu.getStrokeDuration() - (mu.getStrokeDuration() * mu.getRelativeStrokePos());
        Set<TimePeg> strokePegsSet = new HashSet<TimePeg>();
        if (strokeStartPeg != null && strokeStartPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegsSet.add(strokeStartPeg);
        if (strokeEndPeg != null && strokeEndPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegsSet.add(strokeEndPeg);
        if (strokePeg != null && strokePeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegsSet.add(strokePeg);
        // scale
        if (strokePegsSet.size() == 2)
        {
            double scale = 1;
            if (strokePegsSet.contains(strokeStartPeg) && strokePegsSet.contains(strokePeg))
            {
                scale = strokePeg.getGlobalValue() - strokeStartPeg.getGlobalValue() / strokeStartDuration;
            }
            else if (strokePegsSet.contains(strokeStartPeg) && strokePegsSet.contains(strokeEndPeg))
            {
                scale = strokeEndPeg.getGlobalValue() - strokeStartPeg.getGlobalValue() / mu.getStrokeDuration();
            }
            else if (strokePegsSet.contains(strokePeg) && strokePegsSet.contains(strokeEndPeg))
            {
                scale = strokeEndPeg.getGlobalValue() - strokePeg.getGlobalValue() / strokeEndDuration;
            }
            strokeStartDuration *= scale;
            strokeEndDuration *= scale;
        }
        strokeStartDuration = resolveLeftRight(strokeStartDuration, strokeStartPeg, strokePeg);
        strokeEndDuration = resolveLeftRight(strokeEndDuration, strokePeg, strokeEndPeg);
        
        // 3a resolve prep
        double prepDuration = resolveLeftRight(getPreparationDuration(), startPeg, readyPeg);
        // 3b resolve relax
        double relaxDuration = resolveLeftRight(getRetractionDuration(), relaxPeg, endPeg);

        double phases[] = { prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration,
                relaxDuration };
        TimePeg pegs[] = { startPeg, readyPeg, strokeStartPeg, strokePeg, strokeEndPeg, relaxPeg, endPeg};
        String pegIds[] = {"start","ready","strokeStart","stroke","strokeEnd","relax","end"};
        // forward setting
        
        for (int i = 1; i < pegs.length; i++)
        {
            if (pegs[i] != null) 
            {
                if(pegs[i].getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    pegs[i].setGlobalValue(pegs[i-1].getGlobalValue() + phases[i - 1]);
                }
            }
            else
            {
                pegs[i] = new TimePeg(pegs[i-1].getBmlBlockPeg());
                pegs[i].setLocalValue(pegs[i-1].getLocalValue()+phases[i - 1]);
                setTimePeg(pegIds[i], pegs[i]);
            }
        }
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        if (sac.isEmpty()) return;

        resolveDefaultBMLKeyPositions();
        validateSyncs(b, sac);

        // sort sac
        List<TimePegAndConstraint> sortedSac = new ArrayList<TimePegAndConstraint>();
        for (String syncId : getAvailableSyncs())
        {
            for (TimePegAndConstraint s : sac)
            {
                if (s.syncId.equals(syncId))
                {
                    sortedSac.add(s);
                    break;
                }
            }
        }

        linkSynchs(sortedSac);

        TimePegAndConstraint startSac = getTimePeg(BMLGestureSync.START.getId(), sac);
        TimePegAndConstraint readySac = getTimePeg(BMLGestureSync.READY.getId(), sac);
        TimePegAndConstraint strokeStartSac = getTimePeg(BMLGestureSync.STROKE_START.getId(), sac);
        TimePegAndConstraint strokeEndSac = getTimePeg(BMLGestureSync.STROKE_END.getId(), sac);
        TimePegAndConstraint strokeSac = getTimePeg(BMLGestureSync.STROKE.getId(), sac);
        TimePegAndConstraint relaxSac = getTimePeg(BMLGestureSync.RELAX.getId(), sac);
        TimePegAndConstraint endSac = getTimePeg(BMLGestureSync.END.getId(), sac);

        // 1a. preStroke constraints
        double preStrokeHoldDuration = resolveLeftRight(mu.getPreStrokeHoldDuration(), readySac, strokeStartSac);
        // 1b postStroke constraints
        double postStrokeHoldDuration = resolveLeftRight(mu.getPostStrokeHoldDuration(), strokeEndSac, relaxSac);

        // 2 resolve stroke
        double strokeStartDuration = mu.getStrokeDuration() * mu.getRelativeStrokePos();
        double strokeEndDuration = mu.getStrokeDuration() - (mu.getStrokeDuration() * mu.getRelativeStrokePos());
        Set<TimePegAndConstraint> strokeSacsSet = new HashSet<TimePegAndConstraint>();
        if (strokeStartSac != null && strokeStartSac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokeSacsSet.add(strokeStartSac);
        if (strokeEndSac != null && strokeEndSac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokeSacsSet.add(strokeEndSac);
        if (strokeSac != null && strokeSac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokeSacsSet.add(strokeSac);
        // scale
        if (strokeSacsSet.size() == 2)
        {
            double scale = 1;
            if (strokeSacsSet.contains(strokeStartSac) && strokeSacsSet.contains(strokeSac))
            {
                scale = strokeSac.peg.getGlobalValue() - strokeStartSac.peg.getGlobalValue() / strokeStartDuration;
            }
            else if (strokeSacsSet.contains(strokeStartSac) && strokeSacsSet.contains(strokeEndSac))
            {
                scale = strokeEndSac.peg.getGlobalValue() - strokeStartSac.peg.getGlobalValue() / mu.getStrokeDuration();
            }
            else if (strokeSacsSet.contains(strokeSac) && strokeSacsSet.contains(strokeEndSac))
            {
                scale = strokeEndSac.peg.getGlobalValue() - strokeSac.peg.getGlobalValue() / strokeEndDuration;
            }
            strokeStartDuration *= scale;
            strokeEndDuration *= scale;
        }
        strokeStartDuration = resolveLeftRight(strokeStartDuration, strokeStartSac, strokeSac);
        strokeEndDuration = resolveLeftRight(strokeEndDuration, strokeSac, strokeEndSac);

        // 3a resolve prep
        double prepDuration = resolveLeftRight(getPreparationDuration(), startSac, readySac);
        // 3b resolve relax
        double relaxDuration = resolveLeftRight(getRetractionDuration(), relaxSac, endSac);

        double phases[] = { prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration,
                relaxDuration };
        TimePegAndConstraint pegAndConstrs[] = { startSac, readySac, strokeStartSac, strokeSac, strokeEndSac, relaxSac, endSac };
        int tpFirst = -1;
        // find first set peg
        for (int i = 0; i < pegAndConstrs.length; i++)
        {
            if (pegAndConstrs[i] != null && pegAndConstrs[i].peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                tpFirst = i;
                break;
            }
        }

        TimePeg tpRef;
        double offset = 0;
        if (tpFirst == -1)
        {
            // all syncs unknown, start at local time 0
            if (startSac != null)
            {
                tpRef = startSac.peg;
                tpRef.setValue(0, bbPeg);
            }
            else
            {
                tpRef = new TimePeg(bbPeg);
            }
            tpRef.setLocalValue(0);
            tpFirst = 0;
        }
        else
        {
            tpRef = pegAndConstrs[tpFirst].peg;
            offset = pegAndConstrs[tpFirst].offset;
        }

        // forward setting
        for (int i = tpFirst + 1; i < pegAndConstrs.length; i++)
        {
            offset += phases[i - 1];
            if (pegAndConstrs[i] != null && pegAndConstrs[i].peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegAndConstrs[i].peg.setGlobalValue(tpRef.getGlobalValue() + offset + pegAndConstrs[i].offset);
            }
        }

        // XXX: really needed? Added this for now for compatibility with the LinearStretchResolver
        // resolve end if unknown and not a persistent behavior
        // if (endSac==null)
        // {
        // TimePeg tpEnd = new TimePeg(bbPeg);
        // tpEnd.setGlobalValue(tpRef.getGlobalValue()+offset);
        // setTimePeg("end", tpEnd);
        // }

        // backward setting
        offset = 0;
        for (int i = tpFirst - 1; i >= 0; i--)
        {
            offset += phases[i];
            if (pegAndConstrs[i] != null && pegAndConstrs[i].peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegAndConstrs[i].peg.setGlobalValue(tpRef.getGlobalValue() - offset + pegAndConstrs[i].offset);
            }
        }

    }

    

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        initializeAllTimePegs();

        mu.setupTransitionUnits();

        // TimePeg relaxPeg = getTimePeg(BMLGestureSync.RELAX.getId());
        // if(relaxPeg==null)
        // {
        // double readyTime;
        // try
        // {
        // readyTime = puTimeManager.getRelativeTime("relax");
        // }
        // catch (SyncPointNotFoundException e)
        // {
        // throw new TimedPlanUnitPlayException("No ready keyposition defined, cannot set relax timepeg",this,e);
        // }
        //
        // OffsetPeg tpRelax = new OffsetPeg(this.getTimePeg("start"),readyTime);
        // setTimePeg("relax", tpRelax);
        // }
        super.startUnit(time);
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        super.relaxUnit(time);
        mu.setupRelaxUnit();
    }

    @Override
    public void stopUnit(double time)
    {
        super.stopUnit(time);
        log.debug("Tmu:{}:{} time={} relax={} stop={}", new Object[] { getBMLId(), getId(), time, getRelaxTime(), getEndTime() });
        if (time >= getRelaxTime() && time < getEndTime())
        {
            sendFeedback("end", time);
        }
    }

    @Override
    public void playUnit(double time) throws TMUPlayException
    {
        super.playUnit(time);
        setPriority(mu.getPriority());
    }
}
