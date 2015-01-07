/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import com.google.common.collect.ImmutableMap;

/**
 * Binds a trajectory type name to a new trajectory instance
 * @author Herwin
 */
public class TrajectoryBinding
{
    private static final ImmutableMap<String, Class<? extends ParameterValueTrajectory>> TRAJECTORY_BINDING =
        new ImmutableMap.Builder<String, Class<? extends ParameterValueTrajectory>>()
        .put("linear", LinearTrajectory.class)
        .put("instant",InstantChangeTrajectory.class)
        .build();
    
    public ParameterValueTrajectory getTrajectory(String type) throws TrajectoryBindingException
    {
        if(TRAJECTORY_BINDING.get(type)!=null) try
        {
            return TRAJECTORY_BINDING.get(type).newInstance();
        }
        catch (InstantiationException e)
        {
            TrajectoryBindingException ex = new TrajectoryBindingException(type);
            ex.initCause(e);
            throw ex;
        }
        catch (IllegalAccessException e)
        {
            TrajectoryBindingException ex = new TrajectoryBindingException(type);
            ex.initCause(e);
            throw ex;
        }
        throw new TrajectoryBindingException(type);
    }
}
