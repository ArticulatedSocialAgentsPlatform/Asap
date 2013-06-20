package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GestureBehaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationUnit;
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
@Slf4j
public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private LMP lmp;
    private final PegBoard globalPegBoard;
    private final PegBoard localPegBoard;
    private Set<String> syncsHandled = new HashSet<String>();
    private final AnimationPlayer aniPlayer;
    private TimedAnimationUnit relaxUnit;

    // TODO: more or less duplicate with LinearStretchResolver, ProcAnimationGestureTMU
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

    private boolean notSet(String syncId)
    {
        return getTime(syncId) == TimePeg.VALUE_UNKNOWN;
    }

    private boolean noPegsSet()
    {
        return notSet("start") && notSet("relax") && notSet("ready") && notSet("strokeStart") && notSet("stroke") && notSet("strokeEnd")
                && notSet("relax") && notSet("end");
    }

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
    }

    private List<Integer> getSetSyncs(String[] syncs)
    {
        List<Integer> setsyncs = new ArrayList<>();
        int i = 0;
        for (String s : syncs)
        {
            if (getTime(s) != TimePeg.VALUE_UNKNOWN)
            {
                setsyncs.add(i);
            }
            i++;
        }
        return setsyncs;
    }

    private void backwardsResolve(int fromSync, String syncs[], double durations[])
    {
        double offset = getTime(syncs[fromSync]);
        for (int i = fromSync - 1; i >= 0; i--)
        {
            offset -= durations[i];
            this.getTimePeg(syncs[i]).setGlobalValue(offset);
        }
    }

    private void forwardsResolve(int fromSync, String syncs[], double durations[])
    {

        double offset = getTime(syncs[fromSync]);
        for (int i = fromSync + 1; i < syncs.length; i++)
        {
            offset += durations[i - 1];
            this.getTimePeg(syncs[i]).setGlobalValue(offset);
        }
    }

    private void inbetweenResolve(int fromSync, int toSync, String syncs[], double durations[])
    {
        double totalDuration = getTime(syncs[toSync]) - getTime(syncs[fromSync]);
        double defaultDuration = 0;

        for (int i = fromSync + 1; i <= toSync; i++)
        {
            defaultDuration += durations[i - 1];
        }

        double offset = getTime(syncs[fromSync]);
        for (int i = fromSync + 1; i < toSync; i++)
        {
            offset += totalDuration * durations[i - 1] / defaultDuration;
            this.getTimePeg(syncs[i]).setGlobalValue(offset);
        }
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
                if (mcpSync.equals(sync) && !mcpSync.equals("end"))
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

        String syncs[] = { "start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end" };
        double defaultDurations[] = { getPreparationDuration(), 0, 0, getStrokeDuration(), 0, getRetractionDuration() };

        if (noPegsSet())
        {
            getTimePeg("start").setLocalValue(0);
            getTimePeg("strokeStart").setLocalValue(getPreparationDuration());
            getTimePeg("stroke").setLocalValue(getPreparationDuration());
            getTimePeg("strokeEnd").setLocalValue(getPreparationDuration() + getStrokeDuration());
            getTimePeg("end").setLocalValue(getPreparationDuration() + getStrokeDuration() + getRetractionDuration());
        }
        else
        {
            List<Integer> setsyncs = getSetSyncs(syncs);
            backwardsResolve(setsyncs.get(0), syncs, defaultDurations);
            forwardsResolve(setsyncs.get(setsyncs.size() - 1), syncs, defaultDurations);
            for (int i = 0; i < setsyncs.size() - 1; i++)
            {
                inbetweenResolve(setsyncs.get(i), setsyncs.get(i + 1), syncs, defaultDurations);
            }
        }

        // XXX for now, ready and relax are not set up separately from strokeStart and strokeEnd
        getTimePeg("ready").setGlobalValue(getTime("strokeStart"));
        getTimePeg("relax").setGlobalValue(getTime("strokeEnd"));

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
        return lmp.hasValidTiming();
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

    private void updatePreparation(double time)
    {
        if (!getTimePeg("start").isAbsoluteTime()
                && globalPegBoard.getPegKeys(globalPegBoard.getTimePeg(getBMLId(), getId(), "start")).size() == 1)
        {
            localPegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - getPreparationDuration());
        }
    }

    @Override
    public void updateTiming(double time) throws TimedPlanUnitPlayException
    {
        if (isLurking())
        {
            updatePreparation(time);
        }
        if (isLurking() || isInExec())
        {
            if (globalPegBoard.getPegKeys(getTimePeg("strokeEnd")).size() <= 1 && !getTimePeg("strokeEnd").isAbsoluteTime())
            {
                getTimePeg("strokeEnd").setGlobalValue(getTimePeg("strokeStart").getGlobalValue() + getStrokeDuration(time));
            }
        }
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
        lmp.start(time);
        feedback("start", time);
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        Set<String> usedJoints = new HashSet<String>();
        usedJoints.addAll(getKinematicJoints());
        usedJoints.addAll(getPhysicalJoints());
        double retractionDuration = aniPlayer.getTransitionToRestDuration(usedJoints);
        TimePeg startPeg = getTimePeg("relax");
        TimePeg endPeg = getTimePeg("end");
        if (globalPegBoard.getPegKeys(endPeg).size() == 1 && !endPeg.isAbsoluteTime())
        {
            endPeg.setGlobalValue(startPeg.getGlobalValue() + retractionDuration);
        }
        relaxUnit = aniPlayer.createTransitionToRest(NullFeedbackManager.getInstance(), usedJoints, startPeg, endPeg, getBMLId(), getId(),
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
}
