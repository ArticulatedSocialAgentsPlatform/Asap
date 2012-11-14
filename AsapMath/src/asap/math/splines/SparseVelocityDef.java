package asap.math.splines;

import hmi.math.Vec3f;
import lombok.Data;

/**
 * Defines a velocity at a certain index
 * @author hvanwelbergen
 *
 */
@Data
public class SparseVelocityDef
{
    private final int index;    
    private final float[] velocity;
    
    public float[] getVelocity()
    {
        return Vec3f.getVec3f(velocity);
    }
}
