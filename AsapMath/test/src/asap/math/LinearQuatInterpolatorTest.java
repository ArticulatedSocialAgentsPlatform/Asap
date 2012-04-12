package asap.math;

import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Test;

/**
 * Unit tests for the LinearQuatInterpolator
 * @author hvanwelbergen
 *
 */
public class LinearQuatInterpolatorTest
{
    private static final float INTERPOLATION_PRECISION = 0.001f;
    private float q[] = Quat4f.getQuat4f();
    @Test
    public void testInterpolateStart()
    {
        double pval[][]={{0,1,0,0,0},{1,0,0,0,1},{2,0,1,0,0}};
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);        
        in.interpolate(0,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateEnd()
    {
        double pval[][]={{0,1,0,0,0},{1,0,0,0,1},{2,0,1,0,0}};
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);
        in.interpolate(2,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle()
    {
        double pval[][]={{0,1,0,0,0},{1,0,0,0,1},{2,0,1,0,0}};
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);
        in.interpolate(1,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle2()
    {
        double pval[][]={{0,1,0,0,0},{1,0,0,0,1},{2,0,1,0,0}};
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);
        in.interpolate(0.5,q);
        float qExpected[] = Quat4f.getQuat4f();
        Quat4f.interpolate(qExpected, Quat4f.getQuat4f(1,0,0,0), Quat4f.getQuat4f(0,0,0,1),0.5f);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateOneValue()
    {
        double pval[][]={{0,1,0,0,0}};
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);
        in.interpolate(0.5,q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1,0,0,0), q, INTERPOLATION_PRECISION);
    }
}
