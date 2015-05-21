/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.keyframe;

import java.util.List;

import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.Priority;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Automatically generates the appropriate retraction movement+timing
 * @author hvanwelbergen
 */
public class MURMLKeyframeTMU extends TimedAnimationMotionUnit
{
    private final MURMLKeyframeMU mu;

    public MURMLKeyframeTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, MURMLKeyframeMU mu, PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bbf, bmlBlockPeg, bmlId, id, mu, pb, aniPlayer);
        this.mu = mu;
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        TimePeg startPeg = null;
        TimePeg endPeg = null;
        TimePeg relaxPeg = null;
        TimePeg readyPeg = null;
        for (TimePegAndConstraint sac : sacs)
        {
            switch (sac.syncId)
            {
            case "start":
                startPeg = sac.peg;
                break;
            case "ready":
            case "strokeStart":
            case "stroke":
                readyPeg = sac.peg;
                break;
            case "strokeEnd":
            case "relax":
                relaxPeg = sac.peg;
                break;
            case "end":
                endPeg = sac.peg;
                break;
            default:
                throw new BehaviourPlanningException(b, "Invalid sync " + sac.syncId + " for standalone MURMLKeyframeTMU.");
            }
        }

        if (startPeg == null)
        {
            startPeg = new TimePeg(bbPeg);
        }
        if (endPeg == null)
        {
            endPeg = new TimePeg(bbPeg);
        }
        if (readyPeg == null)
        {
            if (mu.getKeyPositions().size() > 2)
            {
                readyPeg = new TimePeg(bbPeg);
            }
            else
            {
                readyPeg = startPeg;
            }
        }
        if (relaxPeg == null)
        {
            relaxPeg = new TimePeg(bbPeg);
            setTimePeg("relax", relaxPeg);
        }

        /*
        double readyDuration = 0;
        if (mu.getKeyPositions().size() > 2)
        {
            readyDuration = mu.getPreparationDuration();
        }
        */
        
        //startPeg        
        if (startPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            if (readyPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                startPeg.setGlobalValue(readyPeg.getGlobalValue()-mu.getPreparationDuration());
            }
            else if(relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                startPeg.setGlobalValue(readyPeg.getGlobalValue()-mu.getPreferedDuration());
            }
            else if(endPeg.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
            {
                startPeg.setGlobalValue(endPeg.getGlobalValue()-mu.getPreferedDuration()-mu.getRetractionDuration());
            }
            else
            {
                startPeg.setLocalValue(0);
            }
        }
        //readyPeg
        if (readyPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            readyPeg.setGlobalValue(startPeg.getGlobalValue()+mu.getPreparationDuration());
        }
        double prepDur = readyPeg.getGlobalValue()-startPeg.getGlobalValue();
        if (relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            relaxPeg.setGlobalValue(readyPeg.getGlobalValue()+mu.getPreferedDuration()-prepDur);
        }
        if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            endPeg.setGlobalValue(relaxPeg.getGlobalValue()+mu.getRetractionDuration());
        }
        /*
         * if (startPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN &&
         * relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN &&
         * readyPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN &&
         * endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * startPeg.setLocalValue(0);
         * readyPeg.setLocalValue(readyDuration);
         * relaxPeg.setLocalValue(mu.getPreferedDuration());
         * endPeg.setLocalValue(mu.getPreferedDuration() + mu.getRetractionDuration());
         * }
         * else if (startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
         * {
         * if (relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * relaxPeg.setGlobalValue(startPeg.getGlobalValue() + mu.getPreferedDuration());
         * }
         * if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * endPeg.setGlobalValue(relaxPeg.getGlobalValue() + mu.getRetractionDuration());
         * }
         * if (readyPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * readyPeg.setGlobalValue(startPeg.getGlobalValue()+readyDuration);
         * }
         * }
         * else if (relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
         * {
         * startPeg.setGlobalValue(relaxPeg.getGlobalValue()-mu.getRetractionDuration());
         * if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * endPeg.setGlobalValue(relaxPeg.getGlobalValue() + mu.getRetractionDuration());
         * }
         * if (readyPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * readyPeg.setGlobalValue(startPeg.getGlobalValue()+readyDuration);
         * }
         * }
         * else if (readyPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
         * {
         * startPeg.setGlobalValue(readyPeg.getGlobalValue()-readyDuration);
         * if (relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * relaxPeg.setGlobalValue(startPeg.getGlobalValue() + mu.getPreferedDuration());
         * }
         * if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
         * {
         * endPeg.setGlobalValue(relaxPeg.getGlobalValue() + mu.getRetractionDuration());
         * }
         * }
         * else
         * {
         * relaxPeg.setGlobalValue(endPeg.getGlobalValue()-mu.getRetractionDuration());
         * startPeg.setGlobalValue(relaxPeg.getGlobalValue()-mu.getPreferedDuration());
         * readyPeg.setGlobalValue(startPeg.getGlobalValue()+readyDuration);
         * }
         */
        setTimePeg("start", startPeg);
        setTimePeg("ready", readyPeg);
        setTimePeg("strokeStart", readyPeg);
        setTimePeg("stroke", readyPeg);
        setTimePeg("strokeEnd", relaxPeg);
        setTimePeg("relax", relaxPeg);
        setTimePeg("end", endPeg);
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        if(getPriority()>=Priority.GESTURE_RETRACTION)
        {
            setPriority(Priority.GESTURE_RETRACTION);
        }
        getTimePeg("end").setGlobalValue(time + mu.getRetractionDurationFromCurrent());
        mu.setupRelaxUnit();
        super.relaxUnit(time);
    }

    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        // interrupted = true;
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + mu.getRetractionDurationFromCurrent());
    }
}
