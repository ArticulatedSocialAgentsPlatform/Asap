/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

/**
 * Stores a keyframe time and a set of dof values (as float array)
 * @author hvanwelbergen
 *
 */
public class KeyFrame
{
    @Getter @Setter private double frameTime;
    private float dofs[];
    
    public KeyFrame(double ftime, float dofs[])
    {
        frameTime = ftime;
        this.dofs = Arrays.copyOf(dofs, dofs.length);
    }
    
    public float [] getDofs()
    {
        return Arrays.copyOf(dofs, dofs.length);
    }
}
