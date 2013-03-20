package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Local motor program skeleton implementation
 * @author hvanwelbergen
 * 
 */
public abstract class LMP extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    protected final PegBoard pegBoard;

    public LMP(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId, true);
        this.pegBoard = pegBoard;
        createPegWhenMissingOnPegBoard("start");
        createPegWhenMissingOnPegBoard("strokeStart");
        createPegWhenMissingOnPegBoard("strokeEnd");
        createPegWhenMissingOnPegBoard("end");
    }

    public boolean hasFixedStrokeDuration()
    {
        return false;
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = new ArrayList<>();
        ImmutableMap<PegKey, TimePeg> pegs = pegBoard.getTimePegs();
        for (PegKey pk : pegs.keySet())
        {
            if (pk.getBmlId().equals(getBMLId()) && pk.getId().equals(getId()))
            {
                syncs.add(pk.getSyncId());
            }
        }
        return syncs;
    }

    // for now just moves the start to attain desired preparation timing.
    protected void updateStartTime()
    {
        double prepDuration = getPreparationDuration();

        TimePeg startPeg = getTimePeg("start");
        TimePeg strokeStartPeg = getTimePeg("strokeStart");
        ImmutableSet<PegKey> keys = pegBoard.getPegKeys(startPeg);
        if (keys.size()-countInternalSyncs(keys,0) == 0)
        {
            double localStart = pegBoard.getRelativePegTime(getBMLId(), strokeStartPeg) - prepDuration;
            if (localStart < 0) localStart = 0;
            startPeg.setLocalValue(localStart);
        }
        else if(startPeg instanceof AfterPeg)
        {
            double intendedStart = strokeStartPeg.getGlobalValue()-prepDuration;
            if(intendedStart<startPeg.getLink().getGlobalValue())
            {
                intendedStart = startPeg.getLink().getGlobalValue();
            }
            double localStart = pegBoard.getRelativePegTime(getBMLId(), strokeStartPeg) - prepDuration;
            if (localStart < 0) localStart = 0;
            startPeg.setLocalValue(localStart);
        }
    }

    @Override
    public void updateTiming(double time) throws TimedPlanUnitPlayException
    {
        if (isLurking())
        {
            updateStartTime();
        }
        resolveTimePegs(time);        
    }

    protected void createPegWhenMissingOnPegBoard(String syncId)
    {
        if (pegBoard.getTimePeg(getBMLId(), getId(), syncId) == null)
        {
            TimePeg tp = new TimePeg(getBMLBlockPeg());
            pegBoard.addTimePeg(getBMLId(), getId(), syncId, tp);
        }
    }

    @Override
    public double getStartTime()
    {
        return pegBoard.getPegTime(getBMLId(), getId(), "start");
    }

    @Override
    public double getEndTime()
    {
        return pegBoard.getPegTime(getBMLId(), getId(), "end");
    }

    @Override
    public double getRelaxTime()
    {
        if (pegBoard.getPegTime(getBMLId(), getId(), "relax") != TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(), getId(), "relax");
        }
        else if (pegBoard.getPegTime(getBMLId(), getId(), "strokeEnd") != TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(), getId(), "strokeEnd");
        }
        return getEndTime();
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return pegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        pegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    protected boolean noPegsSet()
    {
        for (TimePeg tp : pegBoard.getTimePegs(getBMLId(), getId()))
        {
            if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                return false;
            }
        }
        return true;
    }

    protected void setTpMinimumTime(double time)
    {

        for (TimePeg tp : pegBoard.getTimePegs(getBMLId(), getId()))
        {
            if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN && !tp.isAbsoluteTime())
            {
                if (tp.getGlobalValue() < time)
                {
                    tp.setGlobalValue(time);
                }
            }
        }
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {

    }

    protected abstract void setInternalStrokeTiming(double time);

    protected int countInternalSyncs(Set<PegKey> pks, int currentCount)
    {
        for (PegKey pk : pks)
        {
            if (pk.getBmlId().equals(getBMLId()) && pk.getId().equals(getId()) && getAvailableSyncs().contains(pk.getSyncId()))
            {
                currentCount++;
            }
        }
        return currentCount;
    }

    protected void resolveTimePegs(double time)
    {
        Set<PegKey> pkStart = pegBoard.getPegKeys(getTimePeg("start"));
        
        if(isLurking()||isInPrep())
        {
            if (!getTimePeg("start").isAbsoluteTime() && pkStart.size()-countInternalSyncs(pkStart,0) == 0)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - getPreparationDuration());
            }
        }
        pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + getRetractionDuration());

        setInternalStrokeTiming(time);

        if (!isPlaying()&&!isDone())
        {
            setTpMinimumTime(time);
        }
    }

}
