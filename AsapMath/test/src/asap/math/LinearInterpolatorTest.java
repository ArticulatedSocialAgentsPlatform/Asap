/*******************************************************************************
 *******************************************************************************/
package asap.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit testcases for LinearInterpolator
 * @author hvanwelbergen
 *
 */
public class LinearInterpolatorTest
{
    private static final double INTERPOLATION_PRECISION = 0.0001;
    
    @Test
    public void testInterpolateStart()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        LinearInterpolator in = new LinearInterpolator(pval);
        assertEquals(1,in.interpolate(0),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateEnd()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        LinearInterpolator in = new LinearInterpolator(pval);
        assertEquals(3,in.interpolate(2),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        LinearInterpolator in = new LinearInterpolator(pval);
        assertEquals(2,in.interpolate(1),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateMiddle2()
    {
        double pval[][]={{0,1},{1,2},{2,3}};
        LinearInterpolator in = new LinearInterpolator(pval);
        assertEquals(1.5,in.interpolate(0.5),INTERPOLATION_PRECISION);
    }
    
    @Test
    public void testInterpolateOneValue()
    {
        double pval[][]={{0,1}};
        LinearInterpolator in = new LinearInterpolator(pval);
        assertEquals(1,in.interpolate(0),INTERPOLATION_PRECISION);
    }
}
