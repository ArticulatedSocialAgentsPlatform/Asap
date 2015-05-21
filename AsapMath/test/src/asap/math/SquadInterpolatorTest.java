/*******************************************************************************
 *******************************************************************************/
package asap.math;


/**
 * Unit tests for the SquadInterpolator
 * @author hvanwelbergen
 */
public class SquadInterpolatorTest extends AbstractQuatInterpolatorTest
{

    @Override
    protected QuatInterpolator getInterpolator(double[][] p)
    {
        return new SquadInterpolator(p);
    }
    
}
