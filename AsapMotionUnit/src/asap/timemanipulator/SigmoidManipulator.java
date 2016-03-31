/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

import hmi.math.MathUtils;

/**
 * Provides slow-fast-slow interpolation (=a bubble-shaped velocity profile)
 * manip(t)=0.5 . (1+tanh(a.x^p) - 0.5 a determines the steepness p determines
 * the lenght of the accelatory phase plausible values: a=3,p=4
 * 
 * @author welberge
 */
public class SigmoidManipulator implements TimeManipulator
{
    private double a, p;

    /**
     * Constructor
     * 
     * @param steepness
     * @param acclength
     *            length of the accelatory phase
     */
    public SigmoidManipulator(double steepness, double acclength)
    {
        a = steepness;
        p = acclength;
    }

    /**
     * manipulates t
     * 
     * @return manip(t)=0.5 . (1+tanh(a.x^p) - 0.5
     */
    public double manip(double t)
    {
        return 0.5 * (1 + MathUtils.tanh(a * (Math.pow(t, p) - 0.5)));
    }

}
