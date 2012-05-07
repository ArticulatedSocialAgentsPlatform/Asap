package hmi.elckerlyc;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;


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
