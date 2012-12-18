package asap.animationengine.keyframe;

import java.util.List;

import saiba.bml.core.Behaviour;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Automatically generates the appropriate retraction movement+timing
 * @author hvanwelbergen
 */
public class MURMLKeyframeTMU extends TimedAnimationMotionUnit
{
    private final MURMLKeyframeMU mu;

    public MURMLKeyframeTMU(BMLBlockPeg bmlBlockPeg, String bmlId, String id, MURMLKeyframeMU mu, PegBoard pb)
    {
        super(bmlBlockPeg, bmlId, id, mu, pb);
        this.mu = mu;
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        TimePeg startPeg = null;
        TimePeg endPeg = null;
        TimePeg relaxPeg = null;
        for (TimePegAndConstraint sac : sacs)
        {
            switch (sac.syncId)
            {
            case "start":
                startPeg = sac.peg;
                break;
            case "end":
                endPeg = sac.peg;
                break;
            case "relax":
                relaxPeg = sac.peg;
                break;
            default:
                throw new BehaviourPlanningException(b, "Invalid sync " + sac.syncId + " for standalone MURMLKeyframeTMU.");
            }
        }

        if (startPeg == null)
        {
            startPeg = new TimePeg(bbPeg);
            this.setTimePeg("start",startPeg);
        }        
        if (endPeg == null)
        {
            endPeg = new TimePeg(bbPeg);
            this.setTimePeg("end",endPeg);
        }
        if (relaxPeg == null)
        {
            relaxPeg = new TimePeg(bbPeg);
            this.setTimePeg("relax",relaxPeg);
        }

        if (startPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN &&
            relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN &&
            endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            startPeg.setLocalValue(0);
            relaxPeg.setLocalValue(mu.getPreferedDuration());
            endPeg.setLocalValue(mu.getPreferedDuration() + mu.getRetractionDuration());
        }
        else if (startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            if (relaxPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                relaxPeg.setGlobalValue(startPeg.getGlobalValue() + mu.getPreferedDuration());                
            }
            if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                endPeg.setGlobalValue(relaxPeg.getGlobalValue() + mu.getRetractionDuration());                
            }
        }
        else if (relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            startPeg.setGlobalValue(relaxPeg.getGlobalValue()-mu.getRetractionDuration());
            if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                endPeg.setGlobalValue(relaxPeg.getGlobalValue() + mu.getRetractionDuration());                
            }
        }
        else
        {
            relaxPeg.setGlobalValue(endPeg.getGlobalValue()-mu.getRetractionDuration());
            startPeg.setGlobalValue(relaxPeg.getGlobalValue()-mu.getPreferedDuration());
        }
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        getTimePeg("end").setGlobalValue(time + mu.getRetractionDurationFromCurrent());
        mu.setupRelaxUnit();
        super.relaxUnit(time);
    }

    private void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        // interrupted = true;
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + mu.getRetractionDurationFromCurrent());
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
