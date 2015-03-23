/*******************************************************************************
 *******************************************************************************/
package asap.math;

import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Test;


/**
 * Unit tests for the LinearQuatInterpolator
 * @author hvanwelbergen
 *
 */
public class LinearQuatInterpolatorTest extends AbstractQuatInterpolatorTest
{

    @Override
    protected QuatInterpolator getInterpolator(double[][] p)
    {
        return new LinearQuatInterpolator(p);
    }
    
    @Test
    public void testInterpolateMiddle2()
    {
        LinearQuatInterpolator in = new LinearQuatInterpolator(pval);
        in.interpolate(0.5,q);
        float qExpected[] = Quat4f.getQuat4f();
        Quat4f.interpolate(qExpected, Quat4f.getQuat4f(1,0,0,0), Quat4f.getQuat4f(0,0,0,1),0.5f);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, INTERPOLATION_PRECISION);
    }
}
