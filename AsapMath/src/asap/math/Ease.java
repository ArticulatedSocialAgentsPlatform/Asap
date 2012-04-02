package asap.math;

/**
 * From MURML/ACE ease in ease out in divmath.cc
 * Purpose : calculating ease in/out for interpolation values
 * Input : double
 * PreCond : input parameter t in range [0..1]
 * Return : param modified according to logistic function
 * @author hvanwelbergen
 * 
 */
public final class Ease
{
    private Ease()
    {
    }

    public static double ease(double t, double scale, double p)
    {
        // Note: The function is linearly distorted to meet the
        // f(0)=0 and f(1)=1 constraints :-)
        double dist = (2 * t - 1) / (1 + Math.exp(scale * p));
        return ((1 / (1 + Math.exp(-scale * (t - p)))) + dist);
    }

    public static double easeDiff(double t, double scale, double p)
    {
        return ((scale * Math.exp(-scale * (t - p))) / (Math.pow(1 + Math.exp(-scale * (t - p)), 2)));
    }
}
