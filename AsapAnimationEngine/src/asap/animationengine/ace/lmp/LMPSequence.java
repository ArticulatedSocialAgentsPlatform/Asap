/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
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
        double startTime = lmpQueue.get(0).getStartTime();
        double strokeStartTime = lmpQueue.get(0).getTime("strokeStart");
        if (startTime != TimePeg.VALUE_UNKNOWN && strokeStartTime != TimePeg.VALUE_UNKNOWN && isPlaying())
        {
            return strokeStartTime - startTime;
        }
        return lmpQueue.get(0).getPreparationDuration();
    }

    @Override
    public double getRetractionDuration()
    {
        return lmpQueue.get(lmpQueue.size() - 1).getRetractionDuration();
    }

    @Override
    public double getStrokeDuration(double time)
    {
        double duration = 0;
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (tmu.getStartTime() > time || tmu.getStartTime() == TimePeg.VALUE_UNKNOWN)
            {
                duration += tmu.getPreparationDuration();
                duration += tmu.getStrokeDuration();
            }
            else if (tmu.getTime("strokeStart") > time || tmu.getTime("strokeStart") == TimePeg.VALUE_UNKNOWN)
            {
                duration += tmu.getStrokeDuration();
                duration += tmu.getTime("strokeStart") - tmu.getStartTime();
            }
            else
            {
                duration += tmu.getTime("strokeEnd") - tmu.getStartTime();
            }
        }
        double firstPrep = lmpQueue.get(0).getPreparationDuration();
        if (isPlaying())
        {
            firstPrep = lmpQueue.get(0).getTime("strokeStart") - lmpQueue.get(0).getStartTime();
        }
        duration -= firstPrep;
        return duration;
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
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        if (!lmpQueue.isEmpty() && lmpQueue.get(0).getStartTime() >= time)
        {
            lmpQueue.get(0).start(time);
        }
        super.startUnit(time);
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        for (TimedAnimationUnit tmu : lmpQueue)
        {
            if (time > tmu.getStartTime())
            {
                if (time < tmu.getTime("strokeEnd"))
                {
                    if (!tmu.isPlaying())
                    {
                        tmu.start(time);                        
                    }
                    tmu.play(time);                    
                }
                else if (!tmu.isPlaying())
                {
                    tmu.setState(TimedPlanUnitState.DONE);
                    System.out.println("DONE :"+tmu.getId());
                }
            }
        }
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    private void createInternalPegs(TimedAnimationUnit lmp)
    {
        if (lmp.getTimePeg("strokeStart") == null)
        {
            lmp.setTimePeg("strokeStart", new TimePeg(bmlBlockPeg));
        }
    }

    @Override
    protected void setInternalStrokeTiming(double time)
    {
        TimePeg startPeg = pegBoard.getTimePeg(getBMLId(), getId(), "start");
        TimePeg strokeStartPeg = pegBoard.getTimePeg(getBMLId(), getId(), "strokeStart");
        TimePeg strokeEndPeg = pegBoard.getTimePeg(getBMLId(), getId(), "strokeEnd");

        lmpQueue.get(0).setTimePeg("start", startPeg);
        lmpQueue.get(0).setTimePeg("strokeStart", strokeStartPeg);
        lmpQueue.get(lmpQueue.size() - 1).setTimePeg("strokeEnd", strokeEndPeg);

        for (int i = 0; i < lmpQueue.size() - 1; i++)
        {
            TimePeg tp = lmpQueue.get(i).getTimePeg("strokeEnd");
            if (tp == null)
            {
                tp = new TimePeg(bmlBlockPeg);
            }
            lmpQueue.get(i).setTimePeg("strokeEnd", tp);
            lmpQueue.get(i + 1).setTimePeg("start", tp);
        }

        for (TimedAnimationUnit lmp : lmpQueue)
        {
            createInternalPegs(lmp);
        }

        TimePeg startStretchPeg = strokeStartPeg;
        int iPrepStretchStart = lmpQueue.size();
        int iStrokeStretchStart = lmpQueue.size();
        for (int i = 0; i < lmpQueue.size(); i++)
        {
            if (i > 0)
            {
                startStretchPeg = lmpQueue.get(i).getTimePeg("start");
                if (startStretchPeg.getGlobalValue() > time || isLurking())
                {
                    iStrokeStretchStart = i;
                    iPrepStretchStart = i;
                    break;
                }
            }
            startStretchPeg = lmpQueue.get(i).getTimePeg("strokeStart");
            if (startStretchPeg.getGlobalValue() > time || isLurking())
            {
                iStrokeStretchStart = i;
                iPrepStretchStart = i + 1;
                break;
            }
        }

        // pegs assumed to be set: 0:strokeStart, last:strokeEnd
        double defaultDuration = strokeStartPeg.getGlobalValue() + getStrokeDuration(time) - startStretchPeg.getGlobalValue();
        double duration = strokeEndPeg.getGlobalValue() - startStretchPeg.getGlobalValue();

        double stretch;
        if (defaultDuration > 0)
        {
            stretch = duration / defaultDuration;
        }
        else
        {
            stretch = 1;
        }        
        //System.out.println("stretch 1: "+stretch+ " duration "+duration+" defaultDuration="+defaultDuration);
        
        double durPrep[] = new double[lmpQueue.size()];
        double prefPrepDur = 0;
        for (int i = iPrepStretchStart; i < lmpQueue.size(); i++)
        {
            System.out.println("lmpprefprep "+i+"="+lmpQueue.get(i).getPreparationDuration());
            durPrep[i] = lmpQueue.get(i).getPreparationDuration() * stretch;
            
            prefPrepDur += lmpQueue.get(i).getPreparationDuration();
            if (stretch < 1 && durPrep[i] < MINIMUM_PREPARATION_TIME)
            {
                durPrep[i] = MINIMUM_PREPARATION_TIME;
            }
            System.out.println("durPrep "+i+"="+durPrep[i]);
            
            duration -= durPrep[i];
        }

        if (defaultDuration - prefPrepDur > 0)
        {
            stretch = duration / (defaultDuration - prefPrepDur);
        }
        else
        {
            stretch = 1;
        }
        //System.out.println("stretch 2: "+stretch+ "duration "+duration+" defaultDuration-prefPrepDur="+(defaultDuration-prefPrepDur));

        if (iPrepStretchStart > iStrokeStretchStart)
        {
            TimePeg tpStrokeStart = lmpQueue.get(iStrokeStretchStart).getTimePeg("strokeStart");
            lmpQueue.get(iStrokeStretchStart).getTimePeg("strokeEnd")
                    .setGlobalValue(tpStrokeStart.getGlobalValue() + lmpQueue.get(iStrokeStretchStart).getStrokeDuration() * stretch);
        }

        for (int i = iPrepStretchStart; i < lmpQueue.size(); i++)
        {
            TimePeg tpStart = lmpQueue.get(i).getTimePeg("start");
            TimePeg tpStrokeStart = lmpQueue.get(i).getTimePeg("strokeStart");
            tpStrokeStart.setGlobalValue(tpStart.getGlobalValue() + durPrep[i]);
            lmpQueue.get(i).getTimePeg("strokeEnd")
                    .setGlobalValue(tpStrokeStart.getGlobalValue() + lmpQueue.get(i).getStrokeDuration() * stretch);
        }
    }

    @Override
    protected void resolveTimePegs(double time)
    {
        super.resolveTimePegs(time);
        for (TimedAnimationUnit lmp : lmpQueue)
        {
            if (lmp instanceof LMP)
            {
                ((LMP) lmp).resolveTimePegs(time);
            }
        }
    }
}
