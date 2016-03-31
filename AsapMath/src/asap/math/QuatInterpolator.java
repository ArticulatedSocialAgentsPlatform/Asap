/*******************************************************************************
 *******************************************************************************/
package asap.math;

/**
 * Interface for quat interpolators
 * @author hvanwelbergen
 *
 */
public interface QuatInterpolator
{   
    void interpolate(double time, float q[]);
    void interpolate(double time, float q[],int i);
    void setPVal(double[][] pval);
}
