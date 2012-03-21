package asap.math;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;

/**
 * Unit testcases for CubicSplineInterpolator
 * @author hvanwelbergen
 *
 */
public class CubicSplineInterpolatorTest
{
    @Test
    public void testInterpolateStart()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(1,in.interpolate(0),0.001);
    }
    
    @Test
    public void testInterpolateEnd()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(3,in.interpolate(2),0.001);
    }
    
    @Test
    public void testInterpolateMiddle()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertEquals(2,in.interpolate(1),0.001);
    }
    
    @Test
    public void testInterpolateMiddle2()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        CubicSplineInterpolator in = new CubicSplineInterpolator(pval,0,0);
        assertThat(in.interpolate(0.5),greaterThan(1d));
        assertThat(in.interpolate(0.5),lessThan(2d));
    }
}
