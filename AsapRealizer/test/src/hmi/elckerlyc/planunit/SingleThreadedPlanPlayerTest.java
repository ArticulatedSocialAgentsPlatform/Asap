package hmi.elckerlyc.planunit;


/**
 * SingleThreadedPlanPlayer unit test cases
 * @author Herwin
 *
 */
public class SingleThreadedPlanPlayerTest extends AbstractPlanPlayerTest
{
    protected PlanPlayer createPlanPlayer()
    {
        return new SingleThreadedPlanPlayer<TimedPlanUnit>(mockFeedbackManager, planManager);
    }
}
