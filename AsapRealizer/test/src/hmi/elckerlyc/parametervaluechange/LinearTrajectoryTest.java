package hmi.elckerlyc.parametervaluechange;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test cases for the LinearTrajectory trajectory
 * @author hvanwelbergen
 */
public class LinearTrajectoryTest
{
    @Test
    public void test()
    {
        LinearTrajectory trajectory = new LinearTrajectory();
        assertEquals(20, trajectory.getValue(20, 100, 0),0.0001f);
        assertEquals(100, trajectory.getValue(20, 100, 1),0.0001f);
        assertEquals(60, trajectory.getValue(20, 100, 0.5f),0.0001f);
    }
}
