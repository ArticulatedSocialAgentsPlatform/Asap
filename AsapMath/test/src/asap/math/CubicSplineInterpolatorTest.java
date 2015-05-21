/*******************************************************************************
 *******************************************************************************/
package asap.math;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit testcases for CubicSplineInterpolator
 * @author hvanwelbergen
 *
 */
public class CubicSplineInterpolatorTest
{
    private static final double INTERPOLATION_PRECISION = 0.0001;
    @Test
    public void testInterpolateStart()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(1,in.interpolate(0),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateEnd()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(3,in.interpolate(2),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(2,in.interpolate(1),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle2()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertThat(in.interpolate(0.5),greaterThan(1d));
        assertThat(in.interpolate(0.5),lessThan(2d));
    }
    
    @Test
    public void testInterpolateOneValue()
    {
        double pval[][]={{0,1}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(1,in.interpolate(0),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateTwoValues()
    {
        double pval[][]={{0,1},{1,2}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(1,in.interpolate(0),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateTwoValuesEnd()
    {
        double pval[][]={{0,1},{1,2}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(2,in.interpolate(1),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateInbetweenTwoValues()
    {
        double pval[][]={{0,1},{1,2}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertThat(in.interpolate(0.5),greaterThan(1d));
        assertThat(in.interpolate(0.5),lessThan(2d));
    }
}
