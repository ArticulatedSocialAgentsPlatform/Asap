package asap.realizer;

import java.util.List;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;


/**
 * Skeleton implementation of a Planner. Handles the feedbackListeners and delegates some
 * functionality to the players.
 * 
 * @author Herwin
 * 
 */
public abstract class AbstractPlanner<T extends TimedPlanUnit> implements Planner<T>
{
    protected final PlanManager<T> planManager;
    protected final FeedbackManager fbManager;
    
    protected TimePegAndConstraint getSacStart(List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                return sac;
            }
        }
        return null;
    }

    protected TimePegAndConstraint getSacEnd(List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("end"))
            {
                return sac;
            }
        }
        return null;
    }
    
    public AbstractPlanner(FeedbackManager fbm, PlanManager<T> planManager)
    {
        fbManager = fbm;
        this.planManager = planManager;
    } 
    
    @Override
    public void shutdown()
    {
        
    }
}
