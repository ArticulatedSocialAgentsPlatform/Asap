/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import org.junit.After;

import asap.realizertestutil.planunit.AbstractPlanPlayerTest;

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
