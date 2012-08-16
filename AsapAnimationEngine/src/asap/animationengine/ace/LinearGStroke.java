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
    public LinearGStroke(GStrokePhaseID phaseId, TPConstraint et, float[]ep)
    {
        super(phaseId, et, ep, Vec3f.getZero());
    }
}
