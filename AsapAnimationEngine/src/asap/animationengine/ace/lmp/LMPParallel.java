/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.BeforePeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Parallel executed LMPs. That is: strokeStart is connected to the same TimePeg for all LMPs; strokeEnd is the latest strokeEnd of the LMP set.
 * Start and end of each LMP are connected to after and before peg of the LMPParallel.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class LMPParallel extends LMP
{
    private ImmutableList<TimedAnimationUnit> lmpQueue;
    private volatile boolean linked = false;

    public LMPParallel(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard, List<TimedAnimationUnit> lmps)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        lmpQueue = ImmutableList.copyOf(lmps);
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

    protected int countInternalSyncs(Set<PegKey> pks, int currentCount)
    {
        currentCount = super.countInternalSyncs(pks, currentCount);
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (tmu instanceof LMP)
            {
                currentCount = ((LMP) tmu).countInternalSyncs(pks, currentCount);
            }
        }
        return currentCount;
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

    public TimePeg getTimePeg(String sync)
    {
        return pegBoard.getTimePeg(getBMLId(), getId(), sync);
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
    public void updateTiming(double time) throws TimedPlanUnitPlayException
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

        if (isLurking())
        {
            updateStartTime();
        }
    }

    @Override
    public double getPreparationDuration()
    {
        double duration = 0;
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp.getPreparationDuration() > duration)
            {
                duration = lmp.getPreparationDuration();
            }
        }
        return duration;
    }

    @Override
    public double getRetractionDuration()
    {
        double duration = 0;
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp.getRetractionDuration() > duration)
            {
                duration = lmp.getRetractionDuration();
            }
        }
        return duration;
    }

    @Override
    public double getStrokeDuration()
    {
        double duration = 0;

        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp.getStrokeDuration() > duration)
            {
                duration = lmp.getStrokeDuration();
            }
        }
        return duration;
    }

    @Override
    public boolean hasValidTiming()
    {
        /*
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (!tmu.hasValidTiming()) return false;
        }
        */
        return true;
    }

    public void linkLMPSyncs()
    {
        if (linked) return;
        for (String hSync : pegBoard.getSyncs(getBMLId(), getId()))
        {
            for (TimedAnimationUnit lmp : lmpQueue)
            {
                for (String sync : lmp.getAvailableSyncs())
                {
                    if (hSync.equals(sync))
                    {
                        if (!hSync.equals("end") && !hSync.equals("start") && !hSync.equals("strokeEnd"))
                        {
                            lmp.setTimePeg(sync, pegBoard.getTimePeg(getBMLId(), getId(), sync));
                        }
                        else if (hSync.equals("strokeEnd"))
                        {
                            lmp.setTimePeg("strokeEnd", new BeforePeg(pegBoard.getTimePeg(getBMLId(), getId(), hSync)));
                        }
                    }
                }                
            }
        }
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            lmp.setTimePeg("relax", getStrokeEndPeg());
            lmp.setTimePeg("start", new AfterPeg(getStartPeg(), 0, bmlBlockPeg));
        }
        linked = true;
    }

    @Override
    protected void resolveTimePegs(double time)
    {
        TimePeg strokeStartPeg = getTimePeg("strokeStart");
        TimePeg hStartPeg = getTimePeg("start");
        hStartPeg.setGlobalValue(strokeStartPeg.getGlobalValue() - getPreparationDuration());

        linkLMPSyncs();
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (!isPlaying())
            {
                TimePeg sPeg = lmp.getTimePeg("start");
                
                double startValue = strokeStartPeg.getGlobalValue() - lmp.getPreparationDuration();
                if (startValue > hStartPeg.getGlobalValue())
                {
                    sPeg.setGlobalValue(startValue);
                }
                else
                {
                    sPeg.setGlobalValue(hStartPeg.getGlobalValue());
                }
            }
            
            // update stroke durations
            if (time < getStrokeStartTime() || !isPlaying())
            {
                double desiredStrokeEnd = getStrokeStartTime() + lmp.getStrokeDuration();
                log.debug("desiredStrokeEnd of " + lmp.getId() + ": " + desiredStrokeEnd + "=" + getStrokeStartTime() + "+"
                        + lmp.getStrokeDuration());
                if (desiredStrokeEnd < getStrokeEndTime())
                {
                    lmp.getTimePeg("strokeEnd").setGlobalValue(desiredStrokeEnd);
                    log.debug("strokeEnd set: " + desiredStrokeEnd);
                }
                else
                {
                    lmp.getTimePeg("strokeEnd").setGlobalValue(getStrokeEndTime());
                    log.debug("strokeEnd set: " + getStrokeEndTime());
                }
            }
        }

        super.resolveTimePegs(time);
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp instanceof LMP)
            {
                ((LMP) lmp).resolveTimePegs(time);
            }
        }
    }

    @Override
    protected void setInternalStrokeTiming(double time)
    {

    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (time >= tmu.getStartTime())
            {
                if (!tmu.isPlaying())
                {
                    tmu.start(time);
                }
                tmu.updateTiming(time);
                tmu.play(time);
            }
        }
    }   
    
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        resolveTimePegs(time);
        super.startUnit(time);
    }
    
    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
                
    }
}
