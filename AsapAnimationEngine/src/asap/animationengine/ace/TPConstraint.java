package asap.animationengine.ace;


/**
 * Temporal constraint given by a point in time
 * and an additional degree of rigorosity
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 * 
 */
public class TPConstraint implements Comparable<TPConstraint>
{
    enum Mode
    {
        Rigorous(0), Medium(0.5), Soft(1.0);

        private double value;

        Mode(double v)
        {
            value = v;
        }
    }

    private double time;
    private boolean defined;
    private double mode;

    public TPConstraint(double t, double m)
    {
        time = t;
        mode = m;
        defined = true;
    }

    @Override
    public boolean equals(Object tp)
    {
        if (tp instanceof TPConstraint)
        {
            return ((TPConstraint) tp).time == time;
        }
        return false;
    }

    public void add(double t)
    {
        time += t;
        defined = true;
    }

    @Override
    public int compareTo(TPConstraint tp)
    {
        return Double.valueOf(time).compareTo(tp.time);
    }
    
    public String toString()
    {
        return ""+time;
    }
}
