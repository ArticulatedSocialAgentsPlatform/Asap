/*******************************************************************************
 *******************************************************************************/
package asap.math;

import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Test;

public abstract class AbstractQuatInterpolatorTest
{
    protected abstract QuatInterpolator getInterpolator(double p[][]);
    
    protected static final float INTERPOLATION_PRECISION = 0.001f;
    protected float q[] = Quat4f.getQuat4f();
    protected double pval[][]={{0,1,0,0,0},{1,0,0,0,1},{2,0,1,0,0}};
    
    @Test
    public void testInterpolateStart()
    {
        QuatInterpolator in = getInterpolator(pval);        
        in.interpolate(0,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateEnd()
    {
        QuatInterpolator in = getInterpolator(pval);
        in.interpolate(2,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle()
    {
        QuatInterpolator in = getInterpolator(pval);
        in.interpolate(1,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), q, INTERPOLATION_PRECISION);
    }
    
    
    
    @Test
    public void testInterpolateOneValue()
    {
        double pval[][]={{0,1,0,0,0}};
        QuatInterpolator in = getInterpolator(pval);
        in.interpolate(0.5,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1,0,0,0), q, INTERPOLATION_PRECISION);
    }
}
