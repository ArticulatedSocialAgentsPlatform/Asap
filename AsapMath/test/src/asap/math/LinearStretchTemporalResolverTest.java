/*******************************************************************************
 *******************************************************************************/
package asap.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the LinearStretchTemporalResolver
 * @author hvanwelbergen
 * 
 */
public class LinearStretchTemporalResolverTest
{
    private static final double PRECISION = 0.001;

    @Test
    public void testTwoNoConstraints()
    {
        double times[] = new double[] { LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN };
        double weights[] = new double[] { 1 };
        double preferedDurations[] = new double[] { 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 1);
        assertArrayEquals(new double[] { 1d, 2d }, result, PRECISION);
    }

    @Test
    public void testTwoStartConstraint()
    {
        double times[] = new double[] { 1, LinearStretchTemporalResolver.TIME_UNKNOWN };
        double weights[] = new double[] { 1 };
        double preferedDurations[] = new double[] { 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 1d, 2d }, result, PRECISION);
    }

    @Test
    public void testTwoEndConstraint()
    {
        double times[] = new double[] { LinearStretchTemporalResolver.TIME_UNKNOWN, 2 };
        double weights[] = new double[] { 1 };
        double preferedDurations[] = new double[] { 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 1d, 2d }, result, PRECISION);
    }

    @Test
    public void testTwoEndConstraintStartSkew()
    {
        double times[] = new double[] { LinearStretchTemporalResolver.TIME_UNKNOWN, 2 };
        double weights[] = new double[] { 1 };
        double preferedDurations[] = new double[] { 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 1.5);
        assertArrayEquals(new double[] { 1.5d, 2d }, result, PRECISION);
    }

    @Test
    public void testMidForwardResolveStretch()
    {
        double times[] = new double[] { 0, LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 6 };
        double weights[] = new double[] { 1, 1, 1 };
        double preferedDurations[] = new double[] { 1, 1, 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0, 2, 4, 6 }, result, PRECISION);
    }

    @Test
    public void testMidForwardResolveWeightsStretch()
    {
        double times[] = new double[] { 0, LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 6 };
        double weights[] = new double[] { 2, 1, 1 };
        double preferedDurations[] = new double[] { 1, 1, 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0, 4, 5, 6 }, result, PRECISION);
    }

    @Test
    public void testMidForwardResolveSkew()
    {
        double times[] = new double[] { 0, LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 1 };
        double weights[] = new double[] { 1, 1, 1 };
        double preferedDurations[] = new double[] { 1, 1, 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0, 1d / 3d, 2d / 3d, 1 }, result, PRECISION);
    }

    @Test
    public void testMidForwardResolveSkewWeighted()
    {
        double times[] = new double[] { 0, LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 2.5 };
        double weights[] = new double[] { 2, 1, 1 };
        double preferedDurations[] = new double[] { 1, 1, 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0, 0.5, 1.5, 2.5 }, result, PRECISION);
    }

    @Test
    public void testMidForwardResolveSkewWeightedMultiple()
    {
        double times[] = new double[] { 0, LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 1.5 };
        double weights[] = new double[] { 3, 2, 1 };
        double preferedDurations[] = new double[] { 1, 1, 1 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0, 0, 0.5, 1.5 }, result, PRECISION);
    }

    @Test
    public void testResolveEmpty()
    {
        double times[] = new double[] {};
        double weights[] = new double[] {};
        double preferedDurations[] = new double[] {};
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertEquals(0, result.length);
    }

    @Test
    public void testResolveOneUnknown()
    {
        double times[] = new double[] { LinearStretchTemporalResolver.TIME_UNKNOWN };
        double weights[] = new double[] {};
        double preferedDurations[] = new double[] {};
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 0 }, result, PRECISION);
    }

    @Test
    public void testResolveOneSet()
    {
        double times[] = new double[] { 1 };
        double weights[] = new double[] {};
        double preferedDurations[] = new double[] {};
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, 0);
        assertArrayEquals(new double[] { 1 }, result, PRECISION);
    }

    @Test
    public void testStretchZeroDuration()
    {
        double weights[] = { 2, 1, 1, 1, 3, 2 };
        double times[] = { LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 2.0,
                LinearStretchTemporalResolver.TIME_UNKNOWN, 3.0, LinearStretchTemporalResolver.TIME_UNKNOWN, 8.0 };
        double preferedDurations[] = { 0.4, 0.0, 0.0, 1.0, 0.0, 0.3 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, Double.NEGATIVE_INFINITY);
        assertArrayEquals(new double[] { 1.6, 2, 2, 2, 3, 7.7, 8 }, result, PRECISION);
    }
    
    @Test
    public void testSkewZeroDuration()
    {
        double weights[] = { 2, 1, 1, 1, 3, 2 };
        double times[] = { LinearStretchTemporalResolver.TIME_UNKNOWN, LinearStretchTemporalResolver.TIME_UNKNOWN, 2.0,
                LinearStretchTemporalResolver.TIME_UNKNOWN, 3.0, LinearStretchTemporalResolver.TIME_UNKNOWN, 8.0 };
        double preferedDurations[] = { 0.4, 0.0, 0.0, 1.0, 0.0, 10 };
        double result[] = LinearStretchTemporalResolver.solve(times, preferedDurations, weights, Double.NEGATIVE_INFINITY);
        assertArrayEquals(new double[] { 1.6, 2, 2, 2, 3, 3, 8 }, result, PRECISION);
    }
}
