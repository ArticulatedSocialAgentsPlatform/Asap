/*******************************************************************************
 *******************************************************************************/
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
    private final float[] velocity = Vec3f.getVec3f();
    
    public SparseVelocityDef(int index, float []vel)
    {
        this.index = index;
        Vec3f.set(velocity,vel);
    }
    public float[] getVelocity()
    {
        return Vec3f.getVec3f(velocity);
    }
}
