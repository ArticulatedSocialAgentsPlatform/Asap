/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

/**
 * Trajectory for the parameter steered by a TimedParameterValueChangeUnit 
 * @author welberge
 */
public interface ParameterValueTrajectory 
{
    /**
     * @param t relative time 0..1
     * @return the parameter value at time t
     */
    float getValue(float initialValue, float endValue, float t);
}
