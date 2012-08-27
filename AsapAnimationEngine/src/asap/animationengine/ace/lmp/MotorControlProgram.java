package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import saiba.bml.core.GestureBehaviour;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private List<TimedAnimationUnit> lmpQueue = new ArrayList<>();
    private TimePeg startPeg, endPeg;
    private final PegBoard globalPegBoard;
    private final PegBoard localPegBoard;

    private final UniModalResolver resolver = new LinearStretchResolver();

    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        resolver.resolveSynchs(bbPeg, b, sac, this);
    }

    public MotorControlProgram(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId);
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);        
        globalPegBoard = pegBoard;
        localPegBoard = new PegBoard();
    }

    public void addLMP(TimedAnimationUnit tau)
    {
        lmpQueue.add(tau);
    }

    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        if (getTimePeg("relax")!=null && getTimePeg("relax").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
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
    public TimePeg getTimePeg(String syncId)
    {
        return localPegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            tmu.setTimePeg(syncId, peg);
        }
        localPegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
        if(syncId.equals("start"))
        {
            startPeg = peg;
        }
        if(syncId.equals("end"))
        {
            endPeg = peg;
        }
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

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            lmp.updateTiming(time);
        }
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            tmu.updateTiming(time);
            if(tmu.getStartTime()>time)
            {
                if(!tmu.isPlaying())
                {
                    tmu.start(time);
                }
                tmu.play(time);
            }
        }
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

    public void linkSync(TimePegAndConstraint s)
    {
        if (s.offset == 0)
        {
            setTimePeg(s.syncId, s.peg);
        }
        else
        {
            setTimePeg(s.syncId, new OffsetPeg(s.peg, -s.offset));
        }
    }
}
