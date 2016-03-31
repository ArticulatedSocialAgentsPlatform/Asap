/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import asap.realizertestutil.planunit.AbstractTimedPlanUnitPlayerTest;


/**
 * JUnit tests for the MultiThreadedTimedPlanUnitPlayer
 * @author welberge
 */
public class MultiThreadedTimedPlanUnitPlayerTest extends AbstractTimedPlanUnitPlayerTest
{
    @Override
    protected TimedPlanUnitPlayer createTimedPlanUnitPlayer()
    {
        return new MultiThreadedTimedPlanUnitPlayer();
    }
}
