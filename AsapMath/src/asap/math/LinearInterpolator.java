/*******************************************************************************
 *******************************************************************************/
package asap.math;

/**
 * Interpolates linearly between values set through pval.<br>
 * pval[][0] is time at knot <br>
 * pval[][1] is value at knot<br>
 * Assumes that values in pval are ordered in time
 * @author hvanwelbergen
 *
 */
public class LinearInterpolator
{
    private double pval[][];
    
    
    public LinearInterpolator()
    {
        
    }
    
    public LinearInterpolator(double[][] pval)
    {
        setPVal(pval);
    }
    
    public void setPVal(double [][]pval)
    {
        this.pval = new double[pval.length][];
        for(int i=0;i<pval.length;i++)
        {
            this.pval[i] = new double[2];
            this.pval[i][0] = pval[i][0];
            this.pval[i][1] = pval[i][1];
        }
    }
    
    public double interpolate(double time)
    {
        int x0index = 0;
        for(int i=0;i<pval.length;i++)
        {
            if(pval[i][0]>time)break;
            x0index = i;
        }
        int x1index=x0index+1;
        
        if(x1index>=pval.length)
        {
            return pval[x0index][1];
        }
        double x0 = pval[x0index][0];
        double x1 = pval[x1index][0];
        double y0 = pval[x0index][1];
        double y1 = pval[x1index][1];
        double alpha = (time-x0)/(x1-x0);
        return (1-alpha)*y0+alpha*y1;
    }        
}
