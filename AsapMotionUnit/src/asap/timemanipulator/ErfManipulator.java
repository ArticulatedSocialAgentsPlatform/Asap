/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

import org.apache.commons.math3.special.Erf;

/**
 * An ease-in ease out manipulator based on the Gaussian error function
 * 
 * As used in:<br>
 * Helena Grillon and Daniel Thalmann, Simulating gaze attention behaviors for crowds (2009), in: Computer Animation and Virtual Worlds, 20 2-3(111-- 119)
 * 
 * @author Herwin 
 */
public class ErfManipulator implements TimeManipulator
{
    private final int N;

    public ErfManipulator(int N)
    {
        this.N = N;
    }

    @Override
    public double manip(double t)
    {
        double x = t*N;
        return 0.5 + Erf.erf(x-(N/2.0))*0.5;
    }
}
