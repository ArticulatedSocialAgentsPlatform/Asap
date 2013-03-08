package asap.animationengine.ace.lmp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableList;

/**
 * A sequence of TimedAnimationUnits, connecting the start of the next TAU with the strokeEnd of the previous one (that is: retractions are skipped).
 * @author hvanwelbergen
 * 
 */
public class LMPSequence extends LMP
{
    private List<TimedAnimationUnit> lmpQueue;
    
    private static final double MINIMUM_PREPARATION_TIME = 0.2;
    
    public LMPSequence(FeedbackManager fbm, BMLBlockPeg bbPeg, String bmlId, String behId, PegBoard pegBoard,
            List<TimedAnimationUnit> lmpList)
    {
        super(fbm, bbPeg, bmlId, behId, pegBoard);
        lmpQueue = ImmutableList.copyOf(lmpList);    
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        // could be done in an interactive manner as well, using only the actually active tmu
        Set<String> kinJoints = new HashSet<>();
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            kinJoints.addAll(tmu.getKinematicJoints());
        }
        return kinJoints;
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
    public Set<String> getPhysicalJoints()
    {
        Set<String> phJoints = new HashSet<>();
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            phJoints.addAll(tmu.getPhysicalJoints());
        }
        return phJoints;
    }

    @Override
    public double getPreparationDuration()
    {
        return lmpQueue.get(0).getPreparationDuration();
    }

    @Override
    public double getRetractionDuration()
    {
        return lmpQueue.get(lmpQueue.size()-1).getRetractionDuration();
    }

    @Override
    public double getStrokeDuration()
    {
        double duration = 0;
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            duration += tmu.getPreparationDuration();
            duration += tmu.getStrokeDuration();
        }
        duration -= lmpQueue.get(0).getPreparationDuration();
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

    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (time > tmu.getStartTime() && time<tmu.getTime("strokeEnd"))
            {
                if (!tmu.isPlaying())
                {
                    tmu.start(time);
                }
                //tmu.updateTiming(time);
                tmu.play(time);
            }
        }
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    private void createInternalPegs(TimedAnimationUnit lmp)
    {
        if(lmp.getTimePeg("strokeStart")==null)
        {
            lmp.setTimePeg("strokeStart", new TimePeg(bmlBlockPeg));
        }
        lmp.setTimePeg("end", new TimePeg(bmlBlockPeg));
    }

    @Override
    protected void setInternalStrokeTiming(double time)
    {
        TimePeg startPeg = pegBoard.getTimePeg(getBMLId(), getId(),"start");
        TimePeg strokeStartPeg = pegBoard.getTimePeg(getBMLId(), getId(),"strokeStart");
        TimePeg strokeEndPeg = pegBoard.getTimePeg(getBMLId(), getId(),"strokeEnd");
        
        lmpQueue.get(0).setTimePeg("start", startPeg);
        lmpQueue.get(0).setTimePeg("strokeStart", strokeStartPeg);
        lmpQueue.get(lmpQueue.size() - 1).setTimePeg("strokeEnd", strokeEndPeg);
        for (int i = 0; i < lmpQueue.size() - 1; i++)
        {
            TimePeg tp = new TimePeg(bmlBlockPeg);
            lmpQueue.get(i).setTimePeg("strokeEnd", tp);
            lmpQueue.get(i + 1).setTimePeg("start", tp);
        }        

        for (TimedAnimationUnit lmp : lmpQueue)
        {
            createInternalPegs(lmp);            
        }
        
        //pegs assumed to be set: 0:strokeStart, last:strokeEnd
        double defaultDuration = getStrokeDuration();
        double duration = strokeEndPeg.getGlobalValue()-strokeStartPeg.getGlobalValue();
        double stretch = duration/defaultDuration;
        
        TimePeg currentStrokeStart = strokeStartPeg;  
        for (int i = 0; i < lmpQueue.size() - 1; i++)
        {
            double durNeeded = lmpQueue.get(i).getStrokeDuration()+lmpQueue.get(i+1).getPreparationDuration();
            durNeeded *= stretch;
            double durPrep = lmpQueue.get(i+1).getPreparationDuration()*stretch;
            if(durPrep<MINIMUM_PREPARATION_TIME)
            {
                durPrep = MINIMUM_PREPARATION_TIME;
            }
            durNeeded-=durPrep;            
            
            TimePeg strokeEnd = lmpQueue.get(i).getTimePeg("strokeEnd");
            strokeEnd.setGlobalValue(currentStrokeStart.getGlobalValue()+durNeeded);
            currentStrokeStart = lmpQueue.get(i+1).getTimePeg("strokeStart");
            
            //XXX: the preparation duration can (and should) dynamically change here            
            currentStrokeStart.setGlobalValue(strokeEnd.getGlobalValue()+durPrep);
        }
    }
    
    @Override
    protected void resolveTimePegs(double time)
    {
        super.resolveTimePegs(time);
        for(TimedAnimationUnit lmp:lmpQueue)
        {
            if(lmp instanceof LMP)
            {
                ((LMP)lmp).resolveTimePegs(time);
            }
        }
    }
}
