/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

/**
 * Specifies a trajectory that instantly sets the endValue.
 * @author Herwin van Welbergen
 *
 */
public class InstantChangeTrajectory implements ParameterValueTrajectory
{
    @Override
    public float getValue(float initialValue, float endValue, float t)
    {
        return endValue;
    }
}
