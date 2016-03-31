/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GestureBehaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.math.LinearStretchTemporalResolver;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.collect.ImmutableMap;

/**
 * MURML motor program
 * @author hvanwelbergen
 * 
 */
public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private LMP lmp;
    private final PegBoard globalPegBoard;

    @Getter
    private final PegBoard localPegBoard;
    private Set<String> syncsHandled = new HashSet<String>();
    private final AnimationPlayer aniPlayer;
    private TimedAnimationUnit relaxUnit;
    private volatile boolean interrupted = false;

    @Override
    public double getPreparationDuration()
    {
        return lmp.getPreparationDuration();
    }

    @Override
    public double getStrokeDuration()
    {
        return lmp.getStrokeDuration();
    }

    public double getStrokeDuration(double time)
    {
        return lmp.getStrokeDuration(time);
    }

    @Override
    public double getRetractionDuration()
    {
        return lmp.getRetractionDuration();
        // return 1;
    }

    public void resolveSynchs(BMLBlockPeg bbPeg) throws BehaviourPlanningException
    {
        resolveSynchs(bbPeg, new ArrayList<TimePegAndConstraint>());
    }

    protected void linkMCPSynchs(LMP lmp)
    {
        for (String mcpSync : localPegBoard.getSyncs(getBMLId(), getId()))
        {
            for (String sync : lmp.getAvailableSyncs())
            {
                if (mcpSync.equals(sync) && !mcpSync.equals("end") && !mcpSync.equals("relax"))
                {
                    lmp.setTimePeg(sync, localPegBoard.getTimePeg(getBMLId(), getId(), sync));
                }
            }
        }
    }

    protected void resolveInternal(LMP lmp)
    {
        linkMCPSynchs(lmp);
        lmp.resolveTimePegs(0);
    }

    private TimePeg findTimePegInLMP(String sync)
    {
        if (lmp.getTimePeg(sync) != null)
        {
            return lmp.getTimePeg(sync);
        }
        return null;
    }

    protected void checkAndSetMissingTimePeg(String sync, double defaultTime)
    {
        if (getTimePeg(sync) == null)
        {
            TimePeg tp = findTimePegInLMP(sync);
            if (tp != null)
            {
                setTimePeg(sync, tp);
            }
            else
            {
                tp = new TimePeg(getBMLBlockPeg());
                tp.setGlobalValue(defaultTime);
                setTimePeg(sync, tp);
            }
        }
    }

    private void updateTimePegs(int offset, double time)
    {
        String syncs[] = { "start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end" };
        double times[] = new double[syncs.length];
        for (int i = 0; i < times.length; i++)
        {
            if (getTime(syncs[i]) != TimePeg.VALUE_UNKNOWN
                    && (globalPegBoard.getPegKeys(getTimePeg(syncs[i])).size() > 1 || getTimePeg(syncs[i]).isAbsoluteTime()))
            {
                times[i] = getTime(syncs[i]);
            }
            else
            {
                times[i] = LinearStretchTemporalResolver.TIME_UNKNOWN;
            }
        }
        solvePegTiming(offset, syncs, times, time);
    }

    private void setupTimePegs(int offset)
    {
        String syncs[] = { "start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end" };
        double times[] = new double[syncs.length];
        for (int i = offset; i < times.length; i++)
        {
            if (getTime(syncs[i]) != TimePeg.VALUE_UNKNOWN)
            {
                times[i] = getTime(syncs[i]);
            }
            else
            {
                times[i] = LinearStretchTemporalResolver.TIME_UNKNOWN;
            }
        }
        solvePegTiming(offset, syncs, times);
    }

    private double getPostStrokeHoldDuration()
    {
        TimePeg strokeEndPeg = getTimePeg("strokeEnd");
        TimePeg relaxPeg = getTimePeg("relax");
        if (strokeEndPeg instanceof OffsetPeg)
        {
            OffsetPeg op = (OffsetPeg) strokeEndPeg;
            if (op.getLink().equals(relaxPeg))
            {
                return -op.getOffset();
            }
        }

        if (relaxPeg instanceof OffsetPeg)
        {
            OffsetPeg op = (OffsetPeg) relaxPeg;
            if (op.getLink().equals(strokeEndPeg))
            {
                return op.getOffset();
            }
        }

        return 0;
    }

    private void solvePegTiming(int offset, String[] syncs, double[] times)
    {
        double prefDurations[] = { getPreparationDuration(), 0, 0, getStrokeDuration(), getPostStrokeHoldDuration(),
                getRetractionDuration() };
        resolvePegTimes(offset, syncs, times, prefDurations, bmlBlockPeg.getValue());
    }

    private void solvePegTiming(int offset, String[] syncs, double[] times, double time)
    {
        double prefDurations[] = { getPreparationDuration(), 0, 0, getStrokeDuration(time), getPostStrokeHoldDuration(),
                getRetractionDuration() };
        resolvePegTimes(offset, syncs, times, prefDurations, time);
    }

    private void resolvePegTimes(int offset, String[] syncs, double[] times, double[] prefDurations, double startTime)
    {
        double weights[] = { 2, 1, 1, 1, 3, 2 };

        boolean hasKnownTime = false;
        for (double t : times)
        {
            if (t != LinearStretchTemporalResolver.TIME_UNKNOWN)
            {
                hasKnownTime = true;
                break;
            }
        }

        if (offset > 0)
        {
            startTime = getTime(syncs[offset - 1]);
        }
        else if (hasKnownTime)
        {
            startTime = Double.NEGATIVE_INFINITY;
        }

        double solvedTimes[] = LinearStretchTemporalResolver.solve(times, prefDurations, weights, startTime);

        // only set the TimePeg if it's not an OffsetPeg that is connected to another TimePeg in this MCP
        Set<TimePeg> tpegs = new HashSet<TimePeg>();
        for (int i = offset; i < times.length; i++)
        {
            if (!(getTimePeg(syncs[i]) instanceof OffsetPeg))
            {
                tpegs.add(getTimePeg(syncs[i]));
            }
        }
        for (int i = offset; i < times.length; i++)
        {
            if ((getTimePeg(syncs[i]) instanceof OffsetPeg))
            {
                OffsetPeg op = (OffsetPeg) getTimePeg(syncs[i]);
                if (!tpegs.contains(op.getLink()))
                {
                    tpegs.add(op);
                }
            }
        }

        for (int i = offset; i < times.length; i++)
        {
            if (tpegs.contains(getTimePeg(syncs[i])))
            {
                getTimePeg(syncs[i]).setGlobalValue(solvedTimes[i]);
            }
        }
    }

    public void resolveSynchs(BMLBlockPeg bbPeg, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        linkSynchs(sacs);
        checkAndSetMissingTimePeg("start", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("ready", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("strokeStart", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("stroke", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("strokeEnd", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("relax", TimePeg.VALUE_UNKNOWN);
        checkAndSetMissingTimePeg("end", TimePeg.VALUE_UNKNOWN);

        setupTimePegs(0);

        resolveInternal(lmp);
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        resolveSynchs(bbPeg, sacs);
    }

    public MotorControlProgram(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard globalPegBoard,
            PegBoard localPegBoard, AnimationPlayer aniPlayer, LMP lmp)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.globalPegBoard = globalPegBoard;
        this.localPegBoard = localPegBoard;
        this.aniPlayer = aniPlayer;
        this.lmp = lmp;
    }

    @Override
    public double getStartTime()
    {
        return getTime("start");
    }

    @Override
    public double getEndTime()
    {
        return getTime("end");
    }

    @Override
    public double getRelaxTime()
    {
        if (getTimePeg("relax") != null && getTimePeg("relax").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            return getTimePeg("relax").getGlobalValue();
        }
        return getEndTime();
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = new ArrayList<>();
        for (String sync : GestureBehaviour.getDefaultSyncPoints())
        {
            syncs.add(sync);
        }

        ImmutableMap<PegKey, TimePeg> pegs = localPegBoard.getTimePegs();
        for (PegKey pk : pegs.keySet())
        {
            if (pk.getBmlId().equals(getBMLId()) && pk.getId().equals(getId()))
            {
                if (!syncs.contains(pk.getSyncId()))
                {
                    syncs.add(pk.getSyncId());
                }
            }
        }
        return syncs;
    }

    @Override
    public void setState(TimedPlanUnitState newState)
    {
        lmp.setState(newState);
        super.setState(newState);
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return globalPegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        localPegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
        globalPegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        return true;
        // return lmp.hasValidTiming();
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return lmp.getKinematicJoints();
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return lmp.getPhysicalJoints();
    }

    @Override
    public void updateTiming(double time) throws TimedPlanUnitPlayException
    {
        if (interrupted) return;
        if (isSubsiding()) return;
        int syncOffset = 0;
        if (!isLurking())
        {
            syncOffset = 1;
        }
        String syncsArr[] = { "start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end" };

        if (isLurking() || isInExec())
        {
            for (int i = 1; i < syncsArr.length; i++)
            {
                if (getTime(syncsArr[i - 1]) != TimePeg.VALUE_UNKNOWN && getTime(syncsArr[i - 1]) < time)
                {
                    syncOffset = i;
                    break;
                }
            }
        }
        updateTimePegs(syncOffset, time > bmlBlockPeg.getValue() ? time : bmlBlockPeg.getValue());
        lmp.updateTiming(time);
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (time > getRelaxTime())
        {
            relaxUnit.play(time);            
        }
        else
        {
            lmp.play(time);
        }
        feedbackForSyncs(time);
    }

    private void feedbackForSyncs(double time)
    {
        for (String sync : getAvailableSyncs())
        {
            if (!syncsHandled.contains(sync))
            {
                if (time > getTime(sync))
                {
                    feedback(sync, time);
                }
            }
        }
    }

    @Override
    protected void feedback(String sync, double time)
    {
        syncsHandled.add(sync);
        super.feedback(sync, time);
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        System.out.println("start " + this.getBMLId() + ":" + this.getId() + " " + time);
        lmp.start(time);
        feedback("start", time);
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        System.out.println("relax " + this.getBMLId() + ":" + this.getId() + " " + time);
        feedbackForSyncs(time);
        feedback("relax", time);
        Set<String> usedJoints = new HashSet<String>();
        usedJoints.addAll(getKinematicJoints());
        usedJoints.addAll(getPhysicalJoints());
        double retractionDuration = aniPlayer.getTransitionToRestDuration(usedJoints);

        TimePeg relaxPeg = getTimePeg("relax");
        TimePeg endPeg = getTimePeg("end");
        if (globalPegBoard.getPegKeys(endPeg).size() == 1 && !endPeg.isAbsoluteTime())
        {
            endPeg.setGlobalValue(relaxPeg.getGlobalValue() + retractionDuration);
        }
        relaxUnit = aniPlayer.createTransitionToRest(NullFeedbackManager.getInstance(), usedJoints, relaxPeg, endPeg, getBMLId(), getId(),
                bmlBlockPeg, globalPegBoard);
        relaxUnit.start(time);

        super.relaxUnit(time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        feedbackForSyncs(time);
        if (!syncsHandled.contains("end"))
        {
            feedback("end", time);
        }
    }

    protected void skipPegs(double time, String... pegs)
    {
        for (String peg : pegs)
        {
            if (getTime(peg) > time)
            {
                TimePeg tp = getTimePeg(peg);
                TimePeg tpNew = tp;
                if (globalPegBoard.getPegKeys(tp).size() > 1)
                {
                    tpNew = new TimePeg(tp.getBmlBlockPeg());
                    globalPegBoard.addTimePeg(getBMLId(), getId(), peg, tpNew);
                    localPegBoard.addTimePeg(getBMLId(), getId(), peg, tpNew);
                }
                tpNew.setGlobalValue(time - 0.01);
                setTimePeg(peg, tpNew);
            }
        }
    }

    private void gracefullInterrupt(double time)
    {
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);

        Set<String> usedJoints = new HashSet<String>();
        usedJoints.addAll(getKinematicJoints());
        usedJoints.addAll(getPhysicalJoints());
        double retractionDuration = aniPlayer.getTransitionToRestDuration(usedJoints);
        getTimePeg("end").setGlobalValue(time + retractionDuration);
    }

    @Override
    public void interrupt(double time) throws TimedPlanUnitPlayException
    {
        switch (getState())
        {
        case IN_PREP:
        case PENDING:
        case LURKING:
            stop(time);
            break; // just remove yourself
        case IN_EXEC:
            gracefullInterrupt(time);
            break; // gracefully interrupt yourself
        case SUBSIDING: // nothing to be done
        case DONE:
        default:
            break;
        }
    }

}
