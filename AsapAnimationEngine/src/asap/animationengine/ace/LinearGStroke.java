/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import hmi.math.Vec3f;

/**
 * A linear stroke
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 *
 */
public class LinearGStroke extends GuidingStroke
{
    public LinearGStroke(GStrokePhaseID phaseId, float[]ep)
    {
        super(phaseId, ep, Vec3f.getZero());
    }
}
