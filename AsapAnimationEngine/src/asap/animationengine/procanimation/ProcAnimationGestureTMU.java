package asap.animationengine.procanimation;

import hmi.bml.BMLGestureSync;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
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
    private TimePeg startPeg, readyPeg, strokeStartPeg, strokeEndPeg, strokePeg, relaxPeg, endPeg;
    private double prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration, relaxDuration;

    public ProcAnimationGestureTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, ProcAnimationGestureMU m,
            PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m, pb);
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

    public void updateTiming(double time)
    {
        if (isLurking())
        {

        }
        if (isPlaying())
        {
            // fix pegs that have globalTime < time
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

    private void updateAllTimePegs(double time) throws FindTimingException
    {
        findCurrentTimePegs();
        TimePeg pegs[] = { startPeg, readyPeg, strokeStartPeg, strokePeg, strokeEndPeg, relaxPeg, endPeg };

        Set<TimePeg> fixedPegs = new HashSet<TimePeg>();
        for (TimePeg tp : pegs)
        {
            if (tp != null && tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN) 
            {
                if(tp.isAbsoluteTime() || tp.getGlobalValue() <= time || pegBoard.getPegKeys(tp).size()>1)
                {
                    fixedPegs.add(tp);
                }
            }
        }
        findTiming(fixedPegs);

        double phases[] = { prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration,
                relaxDuration };
        // String pegIds[] = { "start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end" };

        int tpFirst = -1;
        // find first set peg
        for (int i = 0; i < pegs.length; i++)
        {
            if (pegs[i] != null && pegs[i].getGlobalValue() != TimePeg.VALUE_UNKNOWN)
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
            if (startPeg != null)
            {
                tpRef = startPeg;
                tpRef.setValue(0, pegBoard.getBMLBlockPeg(getBMLId()));
            }
            else
            {
                tpRef = new TimePeg(pegBoard.getBMLBlockPeg(getBMLId()));
                tpRef.setLocalValue(0);
                startPeg = tpRef;
                setTimePeg("start", startPeg);
            }
            tpFirst = 0;
        }
        else
        {
            tpRef = pegs[tpFirst];
        }

        // forward setting
        for (int i = tpFirst + 1; i < pegs.length; i++)
        {
            offset += phases[i - 1];
            if (pegs[i] != null && pegs[i].getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegs[i].setGlobalValue(tpRef.getGlobalValue() + offset);
            }
        }

        // backward setting
        offset = 0;
        for (int i = tpFirst - 1; i >= 0; i--)
        {
            offset += phases[i];
            if (pegs[i] != null && pegs[i].getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegs[i].setGlobalValue(tpRef.getGlobalValue() - offset);
            }
        }

        // // forward setting
        // for (int i = 1; i < pegs.length; i++)
        // {
        // if (pegs[i] != null)
        // {
        // if (pegs[i].getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        // {
        // pegs[i].setGlobalValue(pegs[i - 1].getGlobalValue() + phases[i - 1]);
        // }
        // }
        // else
        // {
        // pegs[i] = new TimePeg(pegs[i - 1].getBmlBlockPeg());
        // pegs[i].setLocalValue(pegs[i - 1].getLocalValue() + phases[i - 1]);
        // setTimePeg(pegIds[i], pegs[i]);
        // }
        // }
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
        TimePegAndConstraint pegAndConstrs[] = { startSac, readySac, strokeStartSac, strokeSac, strokeEndSac, relaxSac, endSac };

        findCurrentTimePegs();
        TimePeg pegs[] = { startPeg, readyPeg, strokeStartPeg, strokePeg, strokeEndPeg, relaxPeg, endPeg };

        Set<TimePeg> fixedPegs = new HashSet<TimePeg>();
        for (int i = 0; i < pegAndConstrs.length; i++)
        {
            if (pegAndConstrs[i] != null && pegs[i].getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                fixedPegs.add(pegs[i]);
            }
        }
        try
        {
            findTiming(fixedPegs);
        }
        catch (FindTimingException e)
        {
            throw new BehaviourPlanningException(b, "Cannot resolve timing: " + sac + " for " + b + ":" + e.getMessage(), e);
        }

        double phases[] = { prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration,
                relaxDuration };

        int tpFirst = -1;
        // find first set peg
        for (int i = 0; i < pegs.length; i++)
        {
            if (pegs[i] != null && pegs[i].getGlobalValue() != TimePeg.VALUE_UNKNOWN)
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
            if (startPeg != null)
            {
                tpRef = startPeg;
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
            tpRef = pegs[tpFirst];
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

    private void setPegIfNotThere(String sync)
    {
        if (getTimePeg(sync)==null)
        {
            setTimePeg(sync, new TimePeg(pegBoard.getBMLBlockPeg(getBMLId())));
        }
    }
    
    private void setAllDefaultPegs()
    {
        setPegIfNotThere(BMLGestureSync.START.getId());
        setPegIfNotThere(BMLGestureSync.READY.getId());
        setPegIfNotThere(BMLGestureSync.STROKE_START.getId());
        setPegIfNotThere(BMLGestureSync.STROKE_END.getId());
        setPegIfNotThere(BMLGestureSync.STROKE.getId());
        setPegIfNotThere(BMLGestureSync.RELAX.getId());
        setPegIfNotThere(BMLGestureSync.END.getId());
    }
    
    private void findCurrentTimePegs()
    {
        startPeg = getTimePeg(BMLGestureSync.START.getId());
        readyPeg = getTimePeg(BMLGestureSync.READY.getId());
        strokeStartPeg = getTimePeg(BMLGestureSync.STROKE_START.getId());
        strokeEndPeg = getTimePeg(BMLGestureSync.STROKE_END.getId());
        strokePeg = getTimePeg(BMLGestureSync.STROKE.getId());
        relaxPeg = getTimePeg(BMLGestureSync.RELAX.getId());
        endPeg = getTimePeg(BMLGestureSync.END.getId());
    }

    private static class FindTimingException extends Exception
    {
        public FindTimingException(String msg)
        {
            super(msg);
        }

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

    }

    private void findTiming(Set<TimePeg> fixedPegs) throws FindTimingException
    {
        findCurrentTimePegs();

        TimePeg pegs[] = { startPeg, readyPeg, strokeStartPeg, strokeEndPeg, strokePeg, relaxPeg, endPeg };
        for (TimePeg tp : pegs)
        {
            if (!fixedPegs.contains(tp) && tp != null && !fixedPegs.contains(tp.getLink()))
            {
                tp.setGlobalValue(TimePeg.VALUE_UNKNOWN);
            }
        }

        // 1a. preStroke constraints
        preStrokeHoldDuration = resolveLeftRight(mu.getPreStrokeHoldDuration(), readyPeg, strokeStartPeg);
        // 1b postStroke constraints
        postStrokeHoldDuration = resolveLeftRight(mu.getPostStrokeHoldDuration(), strokeEndPeg, relaxPeg);

        // 2 resolve stroke
        strokeStartDuration = mu.getStrokeDuration() * mu.getRelativeStrokePos();
        strokeEndDuration = mu.getStrokeDuration() - (mu.getStrokeDuration() * mu.getRelativeStrokePos());
        Set<TimePeg> strokePegSet = new HashSet<TimePeg>();
        if (strokeStartPeg != null && strokeStartPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegSet.add(strokeStartPeg);
        if (strokeEndPeg != null && strokeEndPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegSet.add(strokeEndPeg);
        if (strokePeg != null && strokePeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN) strokePegSet.add(strokePeg);

        // scale
        if (strokePegSet.size() == 2)
        {
            double scale = 1;
            if (strokePegSet.contains(strokeStartPeg) && strokePegSet.contains(strokePeg))
            {
                scale = strokePeg.getGlobalValue() - strokePeg.getGlobalValue() / strokeStartDuration;
            }
            else if (strokePegSet.contains(strokePeg) && strokePegSet.contains(strokeEndPeg))
            {
                scale = strokeEndPeg.getGlobalValue() - strokeStartPeg.getGlobalValue() / mu.getStrokeDuration();
            }
            else if (strokePegSet.contains(strokePeg) && strokePegSet.contains(strokeEndPeg))
            {
                scale = strokeEndPeg.getGlobalValue() - strokePeg.getGlobalValue() / strokeEndDuration;
            }
            strokeStartDuration *= scale;
            strokeEndDuration *= scale;
        }
        strokeStartDuration = resolveLeftRight(strokeStartDuration, strokeStartPeg, strokePeg);
        strokeEndDuration = resolveLeftRight(strokeEndDuration, strokePeg, strokeEndPeg);

        prepDuration = getPreparationDuration();
        relaxDuration = getRetractionDuration();
        if (startPeg != null && endPeg != null)
        {
            if (startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                double totalDuration = endPeg.getGlobalValue() - startPeg.getGlobalValue();
                if (totalDuration < preStrokeHoldDuration + postStrokeHoldDuration + strokeStartDuration + strokeEndDuration)
                {
                    throw new FindTimingException("Gesture stroke+hold duration longer than allowed by start and end constraints");
                }
                else
                {
                    double scale = totalDuration
                            / (preStrokeHoldDuration + postStrokeHoldDuration + strokeStartDuration + strokeEndDuration + prepDuration + relaxDuration);
                    prepDuration *= scale;
                    relaxDuration *= scale;
                }
            }
        }
        // 3a resolve prep
        prepDuration = resolveLeftRight(prepDuration, startPeg, readyPeg);
        // 3b resolve relax
        relaxDuration = resolveLeftRight(relaxDuration, relaxPeg, endPeg);
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        setAllDefaultPegs();
        try
        {
            updateAllTimePegs(time);
        }
        catch (FindTimingException e)
        {
            throw new TimedPlanUnitPlayException("Invalid timing", this, e);
        }

        mu.setupTransitionUnits();
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
