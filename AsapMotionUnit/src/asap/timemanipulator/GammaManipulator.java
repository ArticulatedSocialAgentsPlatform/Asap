/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * manip(t)=t^gamma gamma &lt; 1 gives a fast-to-slow interpolation gamma &gt; 1
 * gives a slow-to-fast interpolation
 * 
 * @author welberge
 */
public class GammaManipulator implements TimeManipulator
{
    private double gamma;

    /**
     * Constructor
     * 
     * @param g
     *            the gamma value
     */
    public GammaManipulator(double g)
    {
        gamma = g;
    }

    /**
     * manipulates t
     * 
     * @param t
     *            : time to manipulate
     * @return t^gamma
     */
    public double manip(double t)
    {
        return Math.pow(t, gamma);
    }
}
