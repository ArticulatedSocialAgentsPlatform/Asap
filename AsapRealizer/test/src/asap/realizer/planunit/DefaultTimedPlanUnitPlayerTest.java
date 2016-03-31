/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import asap.realizertestutil.planunit.AbstractTimedPlanUnitPlayerTest;


/**
 * JUnit tests for the DefaultTimedPlanUnitPlayer
 * @author welberge
 */
public class DefaultTimedPlanUnitPlayerTest extends AbstractTimedPlanUnitPlayerTest
{
    @Override
    protected TimedPlanUnitPlayer createTimedPlanUnitPlayer()
    {
        return new DefaultTimedPlanUnitPlayer();
    }
}
