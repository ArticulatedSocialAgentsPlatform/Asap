package asap.animationengine.ace.lmp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Parallel executed LMPs. That is: strokeStart and strokeEnd are connected to the same TimePeg for all LMPs.
 * Start and end of each LMP are connected to after and before peg of the LMPParallel.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class LMPParallel extends LMP
{
    private ImmutableList<TimedAnimationUnit> lmpQueue;
    
    public LMPParallel(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard, List<TimedAnimationUnit> lmps)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        lmpQueue = ImmutableList.copyOf(lmps);
        createMissingTimePegs();
    }
    
    private void createMissingTimePegs()
    {
        createPegWhenMissingOnPegBoard("stroke");
        createPegWhenMissingOnPegBoard("ready");
        createPegWhenMissingOnPegBoard("relax");
        createPegWhenMissingOnPegBoard("strokeStart");
        createPegWhenMissingOnPegBoard("strokeEnd");
        createPegWhenMissingOnPegBoard("start");        
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
        for(TimedAnimationUnit tmu:lmpQueue)
        {
            if(tmu instanceof LMP)
            {
                currentCount = ((LMP)tmu).countInternalSyncs(pks,currentCount);
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
        return pegBoard.getTimePeg(getBMLId(),getId(),sync);
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
        
        if(isLurking())
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
        boolean setByHandMove = false;
        
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if(lmp instanceof LMP && ((LMP)lmp).hasFixedStrokeDuration())
            {
                if(setByHandMove)
                {
                    if (lmp.getStrokeDuration() > duration)
                    {
                        duration = lmp.getStrokeDuration();
                    }
                }
                else
                {
                    duration = lmp.getStrokeDuration();
                }
                setByHandMove = true;
            }
            else if (lmp.getStrokeDuration() > duration && !setByHandMove)
            {
                duration = lmp.getStrokeDuration();
            }
        }
        return duration;
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
    
    public void linkLMPSyncs()
    {
        for (String hSync : pegBoard.getSyncs(getBMLId(), getId()))
        {
            for (TimedAnimationUnit lmp: lmpQueue)
            {
                for(String sync:lmp.getAvailableSyncs())
                {
                    if (hSync.equals(sync) && !hSync.equals("end") && !hSync.equals("start"))
                    {
                        lmp.setTimePeg(sync, pegBoard.getTimePeg(getBMLId(), getId(), sync));
                    }
                }
            }
        }
    }
    
    @Override
    protected void resolveTimePegs(double time)
    {
        TimePeg strokeStartPeg = getTimePeg("strokeStart");
        TimePeg hStartPeg = getTimePeg("start");
        hStartPeg.setGlobalValue(strokeStartPeg.getGlobalValue()-getPreparationDuration());
        
        linkLMPSyncs();
        
        
        for (TimedAnimationUnit lmp: lmpQueue)
        {
            TimePeg startPeg = new AfterPeg(hStartPeg, 0, bmlBlockPeg);
            double startValue = strokeStartPeg.getGlobalValue() - lmp.getPreparationDuration();
            if (startValue > hStartPeg.getGlobalValue())
            {
                startPeg.setGlobalValue(startValue);
            }
            else
            {
                startPeg.setGlobalValue(hStartPeg.getGlobalValue());
            }
            lmp.setTimePeg("start", startPeg);
        }
        super.resolveTimePegs(time);
        for(TimedAnimationUnit lmp:lmpQueue)
        {
            if(lmp instanceof LMP)
            {
                ((LMP)lmp).resolveTimePegs(time);
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

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        
    }
}
