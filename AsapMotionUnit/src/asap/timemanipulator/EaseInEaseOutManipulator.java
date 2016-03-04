/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * From MURML/ACE ease in ease out in divmath.cc
 * @author hvanwelbergen
 * 
 */
public class EaseInEaseOutManipulator implements TimeManipulator
{
    private final double p;
    private final double scale;
    
    public EaseInEaseOutManipulator()
    {
        this(0,0);
    }
    
    public EaseInEaseOutManipulator(double scale, double p)
    {
        this.p = p;
        this.scale = scale;
    }

    /**
     * @param t  Time [0..1]    
     */
    public double manip(double t)
    {
        // Note: The function is linearly distorted to meet the
        // f(0)=0 and f(1)=1 constraints :-)
        double dist = (2 * t - 1) / (1 + Math.exp(scale * p));
        return ((1 / (1 + Math.exp(-scale * (t - p)))) + dist);
    }

    public double easeDiff(double t)
    {
        return ((scale * Math.exp(-scale * (t - p))) / (Math.pow(1 + Math.exp(-scale * (t - p)), 2)));
    }    
}
