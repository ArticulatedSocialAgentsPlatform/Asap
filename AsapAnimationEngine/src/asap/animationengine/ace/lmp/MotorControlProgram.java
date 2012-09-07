package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import saiba.bml.core.Behaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.BeforePeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

@Slf4j
public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private List<TimedAnimationUnit> lmpQueue = new ArrayList<>();
    private final PegBoard globalPegBoard;
    private final PegBoard localPegBoard;
    private Set<String> syncsHandled = new HashSet<String>();

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

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        //if (sacs.isEmpty()) return;
        linkSynchs(sacs);
        Collection<TimePegAndConstraint> sacsNoStartEnd = Collections2.filter(sacs, new Predicate<TimePegAndConstraint>()
        {

            @Override
            public boolean apply(@Nullable TimePegAndConstraint tpac)
            {
                if (tpac.syncId.equals("start")) return false;
                if (tpac.syncId.equals("end")) return false;
                return true;
            }

        });

        for (TimedAnimationUnit lmp : lmpQueue)
        {
            lmp.resolveSynchs(bbPeg, b, ImmutableList.copyOf(sacsNoStartEnd));
        }
        resolveMissingSyncPoints();
        
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp.getTimePeg("start") == null)
            {
                lmp.setTimePeg("start", new AfterPeg(getTimePeg("start"), 0));
            }
            if (lmp.getTimePeg("end") == null)
            {
                lmp.setTimePeg("end", new BeforePeg(getTimePeg("end"), 0));
            }
        }

    }

    public MotorControlProgram(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard globalPegBoard,
            PegBoard localPegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.globalPegBoard = globalPegBoard;
        this.localPegBoard = localPegBoard;
    }

    public void addLMP(TimedAnimationUnit tau)
    {
        lmpQueue.add(tau);
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
        if (newState.equals(TimedPlanUnitState.LURKING))
        {
            for (TimedAnimationUnit tmu : lmpQueue)
            {
                tmu.setState(newState);
            }
        }
        super.setState(newState);
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return localPegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        /*
         * needed??
         * for (TimedAnimationUnit tmu : lmpQueue)
         * {
         * tmu.setTimePeg(syncId, peg);
         * }
         */
        localPegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (!tmu.hasValidTiming()) return false;
        }
        return true;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        Set<String> kinJoints = new HashSet<>();
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            kinJoints.addAll(tmu.getKinematicJoints());
        }
        return ImmutableSet.copyOf(kinJoints);
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        Set<String> phJoints = new HashSet<>();
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            phJoints.addAll(tmu.getPhysicalJoints());
        }
        return ImmutableSet.copyOf(phJoints);
    }

    private TimePeg findTimePegInLMPs(String sync)
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (tmu.getTimePeg(sync) != null)
            {
                return tmu.getTimePeg(sync);
            }
        }
        return null;
    }

    private void checkAndSetMissingTimePeg(String sync, double defaultTime)
    {
        if (getTimePeg(sync) == null)
        {
            TimePeg tp = findTimePegInLMPs(sync);
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

    /**
     * Fills out default BML TimePegs that are not yet in the MotorControlProgram.
     * Conventions:
     * missing start => start is start of first LMP
     * missing end => end is end of last LMP
     * missing ready => ready = start
     * missing relax => relax = end
     * missing strokeStart => strokeStart = ready
     * missing strokeEnd => strokeEnd = relax
     * missing stroke => stroke = strokeStart
     * 
     */
    private void resolveMissingSyncPoints()
    {
        if (getStartTime() == TimePeg.VALUE_UNKNOWN)
        {
            double startTime = Doubles.min(Doubles.toArray(Collections2.transform(lmpQueue, new Function<TimedAnimationUnit, Double>()
            {
                @Override
                public Double apply(@Nullable TimedAnimationUnit tau)
                {
                    if (tau.getStartTime() != TimePeg.VALUE_UNKNOWN)
                    {
                        return tau.getStartTime();
                    }
                    return Double.MAX_VALUE;
                }
            })));
            if (startTime < Double.MAX_VALUE)
            {
                TimePeg tp = this.getTimePeg("start");
                if (tp == null)
                {
                    tp = new TimePeg(getBMLBlockPeg());
                    setTimePeg("start", tp);
                }
                tp.setGlobalValue(startTime);
            }
        }

        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            double endTime = Doubles.max(Doubles.toArray(Collections2.transform(lmpQueue, new Function<TimedAnimationUnit, Double>()
            {
                @Override
                public Double apply(@Nullable TimedAnimationUnit tau)
                {
                    if (tau.getEndTime() != TimePeg.VALUE_UNKNOWN)
                    {
                        return tau.getEndTime();
                    }
                    return -Double.MAX_VALUE;
                }
            })));
            if (endTime > -Double.MAX_VALUE)
            {
                TimePeg tp = this.getTimePeg("end");
                if (tp == null)
                {
                    tp = new TimePeg(getBMLBlockPeg());
                    setTimePeg("end", tp);
                }                
                tp.setGlobalValue(endTime);                
            }
        }
        checkAndSetMissingTimePeg("ready", getStartTime());
        checkAndSetMissingTimePeg("relax", getEndTime());
        checkAndSetMissingTimePeg("strokeStart", this.getTime("ready"));
        checkAndSetMissingTimePeg("strokeEnd", this.getTime("relax"));
        checkAndSetMissingTimePeg("stroke", this.getTime("strokeStart"));
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        if (lmpQueue.size() == 0)
        {
            log.warn("running a mcp with an empty lmp queue");
            return;
        }

        for (TimedAnimationUnit lmp : lmpQueue)
        {
            lmp.updateTiming(time);
        }
        resolveMissingSyncPoints();
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            tmu.updateTiming(time);
            if (time > tmu.getStartTime())
            {
                if (!tmu.isPlaying())
                {
                    tmu.start(time);
                }
                tmu.play(time);
            }
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
    protected void startUnit(double time)
    {
        feedback("start", time);
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
