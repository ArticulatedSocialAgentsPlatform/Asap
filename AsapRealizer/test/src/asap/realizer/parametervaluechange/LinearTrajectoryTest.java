/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test cases for the LinearTrajectory trajectory
 * @author hvanwelbergen
 */
public class LinearTrajectoryTest
{
    private LinearTrajectory trajectory = new LinearTrajectory();
    private static final float START_VALUE = 20;
    private static final float END_VALUE = 100;
    private static final float INTERPOLATION_PRECISION = 0.0001f;
    
    @Test
    public void testStart()
    {
        assertEquals(START_VALUE, trajectory.getValue(START_VALUE, END_VALUE, 0), INTERPOLATION_PRECISION);
    }

    @Test
    public void testEnd()
    {
        assertEquals(END_VALUE, trajectory.getValue(START_VALUE, END_VALUE, 1), INTERPOLATION_PRECISION);
    }

    @Test
    public void testMiddle()
    {
        assertEquals(START_VALUE+(END_VALUE-START_VALUE)/2, trajectory.getValue(START_VALUE, END_VALUE, 0.5f), INTERPOLATION_PRECISION);        
    }
}
