package hmi.elckerlyc.planunit;

import org.junit.After;

/**
 * MultiThreadedPlanPlayer unit test cases
 * @author Herwin
 *
 */
public class MultiThreadedPlanPlayerTest extends AbstractPlanPlayerTest
{
    MultiThreadedPlanPlayer<TimedPlanUnit> mpp = null;
    
    @After
    public void tearDown()
    {
        if(mpp!=null)
        {
            mpp.shutdown();
        }
    }
    
    protected PlanPlayer createPlanPlayer()
    {
        mpp = new MultiThreadedPlanPlayer<TimedPlanUnit>(mockFeedbackManager, planManager);
        return mpp; 
    }
}
