package asap.realizer.planunit;

import asap.realizertestutil.planunit.AbstractTimedPlanUnitPlayerTest;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.TimedPlanUnitPlayer;


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
