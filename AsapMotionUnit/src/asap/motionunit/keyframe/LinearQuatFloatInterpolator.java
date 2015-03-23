/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import hmi.util.Builder;
import asap.math.LinearQuatInterpolator;

/**
 * QuatFloatInterpolator implementation using slerp
 * @author hvanwelbergen
 *
 */
public class LinearQuatFloatInterpolator extends QuatFloatInterpolator<LinearQuatInterpolator>
{
    public LinearQuatFloatInterpolator()
    {
        super(new Builder<LinearQuatInterpolator>()
        {
            @Override
            public LinearQuatInterpolator build()
            {
                return new LinearQuatInterpolator();
            }
        });
    }

    public LinearQuatFloatInterpolator(Builder<LinearQuatInterpolator> builder)
    {
        super(builder);

    }
}
