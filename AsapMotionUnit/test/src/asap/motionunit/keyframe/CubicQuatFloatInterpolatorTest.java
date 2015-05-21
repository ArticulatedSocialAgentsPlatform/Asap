/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import asap.math.SquadInterpolator;

/**
 * Unit tests for the CubicQuatFloatInterpolator
 * @author hvanwelbergen
 *
 */
public class CubicQuatFloatInterpolatorTest extends AbstractQuatFloatInterpolatorTest<SquadInterpolator>
{

    @Override
    protected QuatFloatInterpolator<SquadInterpolator> getInterpolator()
    {
        return new CubicQuatFloatInterpolator();
    }
    
}
