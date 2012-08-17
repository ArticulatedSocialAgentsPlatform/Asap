package asap.math.splines;

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
}
