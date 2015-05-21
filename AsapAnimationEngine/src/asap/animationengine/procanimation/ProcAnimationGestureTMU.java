/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.BMLGestureSync;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * TimedMotionUnit for ProcAnimationGestureMU
 * @author Herwin
 * 
 */
@Slf4j
public class ProcAnimationGestureTMU extends TimedAnimationMotionUnit
{
    private final ProcAnimationGestureMU mu;
    private volatile boolean interrupted = false;
    private TimePeg startPeg, readyPeg, strokeStartPeg, strokeEndPeg, strokePeg, relaxPeg, endPeg;
    private double prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration, relaxDuration;

    public ProcAnimationGestureTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, ProcAnimationGestureMU m,
            PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m, pb, aniPlayer);
        mu = m;
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

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        if (interrupted) return;
        setAllDefaultPegs();

        try
        {
            updateAllTimePegs(time);
        }
        catch (FindTimingException e)
        {
            throw new TMUPlayException("Exception in updateTiming(" + time + ")", this, e);
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

    public void updatePreparationDuration()
    {
        if (isLurking() || isPending())
        {
            prepDuration = mu.getPreparationDuration();
        }
        else if (isInPrep())
        {
            prepDuration = mu.getPreparationFromRestDuration();
        }
    }

    public double updateRetractionDuration()
    {
        if (!isSubsiding())
        {
            relaxDuration = mu.getRetractionDuration();
        }
        return relaxDuration;
    }

    private double resolveLeftRight(double defaultDuration, TimePeg pegLeft, TimePeg pegRight, Set<TimePeg> changeAblePegs)
    {
        if (pegLeft != null && !changeAblePegs.contains(pegLeft))
        {
            if (pegRight != null)
            {
                if (changeAblePegs.contains(pegRight))
                {
                    pegRight.setGlobalValue(pegLeft.getGlobalValue() + defaultDuration);
                }
                else
                {
                    return pegRight.getGlobalValue() - pegLeft.getGlobalValue();
                }
            }
        }
        else if (pegRight != null && !changeAblePegs.contains(pegRight))
        {
            if (pegLeft != null)
            {
                if (changeAblePegs.contains(pegLeft))
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
                if (tp.isAbsoluteTime() || tp.getGlobalValue() <= time || pegBoard.getPegKeys(tp).size() > 1)
                {
                    fixedPegs.add(tp);
                    log.debug("Fixed peg: {} TimePeg: {}", pegBoard.getPegKeys(tp), tp);
                }
            }
        }
        Set<TimePeg> changeAblePegs = findTiming(fixedPegs);

        double phases[] = { prepDuration, preStrokeHoldDuration, strokeStartDuration, strokeEndDuration, postStrokeHoldDuration,
                relaxDuration };

        int tpFirst = -1;

        // find first set peg
        for (int i = 0; i < pegs.length; i++)
        {
            if (pegs[i] != null && !changeAblePegs.contains(pegs[i]))
            {
                tpFirst = i;
                break;
            }
        }

        TimePeg tpRef;
        double offset = 0;
        if (tpFirst == -1)
        {
            double localTime = 0;
            BMLBlockPeg currentBlockPeg = pegBoard.getBMLBlockPeg(getBMLId());
            if (time > currentBlockPeg.getValue())
            {
                localTime = time - currentBlockPeg.getValue();
            }
            // all syncs unknown, start at local time 0
            if (startPeg != null)
            {
                tpRef = startPeg;
                tpRef.setValue(localTime, currentBlockPeg);
            }
            else
            {
                tpRef = new TimePeg(currentBlockPeg);
                tpRef.setLocalValue(localTime);
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
            if (pegs[i] != null && changeAblePegs.contains(pegs[i]))
            {
                pegs[i].setGlobalValue(tpRef.getGlobalValue() + offset);
            }
            else if (!changeAblePegs.contains(pegs[i]) && pegs[i].getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                offset = pegs[i].getGlobalValue() - tpRef.getGlobalValue();
            }
        }

        // backward setting
        offset = 0;
        BMLBlockPeg bbPeg = pegBoard.getBMLBlockPeg(getBMLId());
        for (int i = tpFirst - 1; i >= 0; i--)
        {
            offset += phases[i];
            double globalValue = tpRef.getGlobalValue() - offset;
            if (tpRef.getGlobalValue() - offset < bbPeg.getValue())
            {
                globalValue = bbPeg.getValue();
            }
            if (pegs[i] != null && changeAblePegs.contains(pegs[i]))
            {
                pegs[i].setGlobalValue(globalValue);
            }
            else if (!changeAblePegs.contains(pegs[i]) && pegs[i].getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                offset = tpRef.getGlobalValue() - pegs[i].getGlobalValue();
            }
        }
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        if (sac.isEmpty()) return;

        resolveGestureKeyPositions();
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
        if (getTimePeg(sync) == null)
        {
            TimePeg tp = new TimePeg(pegBoard.getBMLBlockPeg(getBMLId()));
            setTimePeg(sync, tp);
            pegBoard.addTimePeg(getBMLId(), getId(), sync, tp);
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
        findCurrentTimePegs();
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

    private Set<TimePeg> findTiming(Set<TimePeg> fixedPegs) throws FindTimingException
    {
        Set<TimePeg> changeAblePegs = new HashSet<TimePeg>();
        findCurrentTimePegs();

        TimePeg pegs[] = { startPeg, readyPeg, strokeStartPeg, strokeEndPeg, strokePeg, relaxPeg, endPeg };
        for (TimePeg tp : pegs)
        {
            if (!fixedPegs.contains(tp) && tp != null && !fixedPegs.contains(tp.getLink()))
            {
                changeAblePegs.add(tp);
            }
        }

        // 1a. preStroke constraints
        preStrokeHoldDuration = resolveLeftRight(mu.getPreStrokeHoldDuration(), readyPeg, strokeStartPeg, changeAblePegs);
        // 1b postStroke constraints
        postStrokeHoldDuration = resolveLeftRight(mu.getPostStrokeHoldDuration(), strokeEndPeg, relaxPeg, changeAblePegs);

        // 2 resolve stroke
        strokeStartDuration = mu.getStrokeDuration() * mu.getRelativeStrokePos();
        strokeEndDuration = mu.getStrokeDuration() - (mu.getStrokeDuration() * mu.getRelativeStrokePos());
        Set<TimePeg> strokePegSet = new HashSet<TimePeg>();
        if (strokeStartPeg != null && !changeAblePegs.contains(strokeStartPeg)) strokePegSet.add(strokeStartPeg);
        if (strokeEndPeg != null && !changeAblePegs.contains(strokeEndPeg)) strokePegSet.add(strokeEndPeg);
        if (strokePeg != null && !changeAblePegs.contains(strokePeg)) strokePegSet.add(strokePeg);

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
        strokeStartDuration = resolveLeftRight(strokeStartDuration, strokeStartPeg, strokePeg, changeAblePegs);
        strokeEndDuration = resolveLeftRight(strokeEndDuration, strokePeg, strokeEndPeg, changeAblePegs);

        updatePreparationDuration();
        updateRetractionDuration();
        if (startPeg != null && endPeg != null)
        {
            if (!changeAblePegs.contains(startPeg) && !changeAblePegs.contains(endPeg))
            {
                double totalDuration = endPeg.getGlobalValue() - startPeg.getGlobalValue();
                if (totalDuration < preStrokeHoldDuration + postStrokeHoldDuration + strokeStartDuration + strokeEndDuration)
                {
                    throw new FindTimingException("Gesture stroke+hold duration longer than allowed by start and end constraints");
                }
                else
                {
                    double scale = totalDuration
                            / (preStrokeHoldDuration + postStrokeHoldDuration + strokeStartDuration 
                                    + strokeEndDuration + prepDuration + relaxDuration);
                    prepDuration *= scale;
                    relaxDuration *= scale;
                }
            }
        }
        // 3a resolve prep
        prepDuration = resolveLeftRight(prepDuration, startPeg, readyPeg, changeAblePegs);

        // 3b resolve relax
        relaxDuration = resolveLeftRight(relaxDuration, relaxPeg, endPeg, changeAblePegs);

        return changeAblePegs;
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        prepDuration = mu.getPreparationDuration();
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
    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        interrupted = true;
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + mu.getInterruptionDuration());
    }

    @Override
    public void stopUnit(double time)
    {
        super.stopUnit(time);
        log.debug("Tmu:{}:{} time={} relax={} stop={}", new Object[] { getBMLId(), getId(), time, getRelaxTime(), getEndTime() });
        if (time >= getRelaxTime() && time < getEndTime())
        {
            feedback("end", time);
        }
    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        super.playUnit(time);
        setPriority(mu.getPriority());
    }
}
