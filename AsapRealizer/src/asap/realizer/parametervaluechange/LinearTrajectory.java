/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

/**
 * Specifies a Linear trajectory:<br>
 * value = initialValue+(endValue-initialValue)*t
 * @author Herwin van Welbergen
 *
 */
public class LinearTrajectory implements ParameterValueTrajectory
{

    @Override
    public float getValue(float initialValue, float endValue, float t)
    {
        return initialValue+(endValue-initialValue)*t;
    }
    
}
