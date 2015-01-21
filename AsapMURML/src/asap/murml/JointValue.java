/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import java.util.Arrays;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value class for a joint + DoF values
 * @author hvanwelbergen
 *
 */
@EqualsAndHashCode
public final class JointValue
{
    @Getter
    public final String jointSid;
    
    public final float[] dofs;
    
    public float[] getDofs()
    {
        return Arrays.copyOf(dofs, dofs.length);
    }
    
    public JointValue(String id, float[] dofs)
    {
        this.dofs = Arrays.copyOf(dofs, dofs.length);
        jointSid = id;
    }
    
    public String toString()
    {
        return jointSid+":"+Arrays.toString(dofs);
    }
}
