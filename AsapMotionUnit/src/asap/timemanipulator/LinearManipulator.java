/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * Linear time manipulator: manip(t)=t
 * 
 * @author welberge
 */
public class LinearManipulator implements TimeManipulator
{
    /**
     * Linear interpolation
     * 
     * @param t
     *            time
     * @return t
     */
    public double manip(double t)
    {
        return t;
    }

}
