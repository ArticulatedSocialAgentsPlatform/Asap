package asap.realizer.planunit;

import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizertestutil.planunit.AbstractPlanPlayerTest;


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
