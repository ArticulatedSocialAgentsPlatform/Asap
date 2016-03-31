/*******************************************************************************
 *******************************************************************************/
package asap.math;

/**
 * Interpolates between values set through pval using a cubic spline.<br>
 * pval[][0] is time at knot <br>
 * pval[][1] is value at knot<br>
 * Assumes that values in pval are ordered in time
 * @author hvanwelbergen
 */
public class CubicSplineInterpolator
{
    double pval[][];
    double nr_xv[], nr_yv[], nr_y2drv[];

    public CubicSplineInterpolator()
    {

    }

    public CubicSplineInterpolator(double[][] pval, double yfd1, double yfdn)
    {
        setPVal(pval, yfd1, yfdn);
    }

    public void setPVal(double pval[][])
    {
        setPVal(pval, 0, 0);
    }

    public void setPVal(double pval[][], double yfd1, double yfdn)
    {
        this.pval = new double[pval.length][];
        for (int i = 0; i < pval.length; i++)
        {
            this.pval[i] = new double[2];
            this.pval[i][0] = pval[i][0];
            this.pval[i][1] = pval[i][1];
        }
        init(yfd1, yfdn);
    }

    private void init(double yfd1, double yfdn)
    {
        nr_xv = new double[pval.length + 1];
        nr_yv = new double[pval.length + 1];
        nr_y2drv = new double[pval.length + 1];

        // transfer control point values to numrep array
        for (int i = 0; i < pval.length; i++)
        {
            nr_xv[i + 1] = pval[i][0];
            nr_yv[i + 1] = pval[i][1];
        }
        spline(nr_xv, nr_yv, pval.length, yfd1, yfdn, nr_y2drv);
    }

    private void spline(double x[], double y[], int n, double yp1, double ypn, double y2[])
    {
        int i, k;
        double p, qn, sig, un;
        // double u[] = new double[n - 1];
        double u[] = new double[n];

        if (yp1 > 0.99e30)
        {
            y2[1] = u[1] = 0.0;
        }
        else if(n>1)
        {
            y2[1] = -0.5;
            u[1] = (3.0 / (x[2] - x[1])) * ((y[2] - y[1]) / (x[2] - x[1]) - yp1);
        }
        for (i = 2; i <= n - 1; i++)
        {
            sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
            p = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1.0) / p;
            u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
            u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
        }
        if (ypn > 0.99e30 || n <= 2)
        {
            qn = un = 0.0;
        }        
        else
        {
            qn = 0.5;
            un = (3.0 / (x[n] - x[n - 1])) * (ypn - (y[n] - y[n - 1]) / (x[n] - x[n - 1]));
        }
        y2[n] = (un - qn * u[n - 1]) / (qn * y2[n - 1] + 1.0);
        for (k = n - 1; k >= 1; k--)
            y2[k] = y2[k] * y2[k + 1] + u[k];
    }

    public double interpolate(double time)
    {
        return splint(nr_xv, nr_yv, nr_y2drv, pval.length, time);
    }

    private double splint(double xa[], double ya[], double y2a[], int n, double x)
    {
        if(n==1) return ya[1];
        int klo, khi, k;
        double h, b, a;

        klo = 1;
        khi = n;

        while (khi - klo > 1)
        {
            k = (khi + klo) >>> 1;
            if (xa[k] > x) khi = k;
            else klo = k;
        }
        h = xa[khi] - xa[klo];
        a = (xa[khi] - x) / h;
        b = (x - xa[klo]) / h;
        return a * ya[klo] + b * ya[khi] + ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi]) * (h * h) / 6.0;
    }
}
