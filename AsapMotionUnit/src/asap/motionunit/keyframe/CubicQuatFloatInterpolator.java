/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import hmi.util.Builder;
import asap.math.SquadInterpolator;

/**
 * QuatFloatInterpolator implementation using squad
 * @author hvanwelbergen
 *
 */
public class CubicQuatFloatInterpolator extends QuatFloatInterpolator<SquadInterpolator>
{
    public CubicQuatFloatInterpolator()
    {
        super(new Builder<SquadInterpolator>()
        {
            @Override
            public SquadInterpolator build()
            {
                return new SquadInterpolator();
            }
        });
    }

    public CubicQuatFloatInterpolator(Builder<SquadInterpolator> builder)
    {
        super(builder);

    }
}
