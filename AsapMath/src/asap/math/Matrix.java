/*******************************************************************************
 *******************************************************************************/
package asap.math;

/**
 * Class wrapper for a float array that provides the functions in Mat
 * @author hvanwelbergen
 * 
 */
public class Matrix
{
    private double mat[];
    private final int m;

    public Matrix(int n, int m)
    {
        this.m = m;
        mat = new double[n * m];
    }

    public double get(int i, int j)
    {
        return mat[i * m + j];
    }

    public void set(int i, int j, double val)
    {
        mat[i * m + j] = val;
    }
}
