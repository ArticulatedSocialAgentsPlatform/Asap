/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Test;

/**
 * Test cases for the trajectory binding
 * @author welberge
 */
public class TrajectoryBindingTest
{
    @Test
    public void testGetTrajectory() throws TrajectoryBindingException
    {
        TrajectoryBinding binding = new TrajectoryBinding();
        ParameterValueTrajectory t = binding.getTrajectory("linear");
        assertThat(t, instanceOf(LinearTrajectory.class));
    }
    
    @Test (expected=TrajectoryBindingException.class)
    public void testGetInvalidTrajectory() throws TrajectoryBindingException
    {
        TrajectoryBinding binding = new TrajectoryBinding();
        ParameterValueTrajectory t = binding.getTrajectory("invalid");
        assertThat(t, instanceOf(LinearTrajectory.class));
    }
}
