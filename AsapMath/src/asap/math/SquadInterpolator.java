/*******************************************************************************
 *******************************************************************************/
package asap.math;

import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.Arrays;

/**
 * Interpolates quaternions using a Catmull-Rom spline
 * @author hvanwelbergen
 * 
 */
public class SquadInterpolator implements QuatInterpolator
{
    private double keyTimes[];
    private float keyValues[][];

    public SquadInterpolator()
    {
    }
    
    public SquadInterpolator(double[][] pval)
    {
        setPVal(pval);
    }

    public void setPVal(double[][] pval)
    {
        keyValues = new float[pval.length][];
        keyTimes = new double[pval.length];
        for (int i = 0; i < pval.length; i++)
        {
            keyValues[i] = new float[4];
            keyTimes[i] = pval[i][0];
            for (int j = 0; j < 4; j++)
            {
                keyValues[i][j] = (float) pval[i][j + 1];
            }
        }
    }

    public void interpolate(double time, float q[])
    {
        interpolate(time, q, 0);
    }

    public void interpolate(double time, float q[], int index)
    {
        int q1index = Arrays.binarySearch(keyTimes, time);
        if (q1index < 0) // -q1index is insertion (-insertion)-1
        {
            q1index = q1index + 1;
            q1index = -q1index;
        }

        int q0index = 0;
        if (q1index > 0)
        {
            q0index = q1index - 1;
        }
        if (q1index == keyTimes.length)
        {
            q1index = keyTimes.length - 1;
        }
        int qmin1index = 0;
        if (q0index > 0)
        {
            qmin1index = q0index - 1;
        }
        if (qmin1index == keyTimes.length)
        {
            qmin1index = keyTimes.length - 1;
        }

        double deltan = 1;
        double deltanmin1 = 1;
        if (keyTimes[q1index] - keyTimes[q0index] > 0 && keyTimes[q0index] - keyTimes[qmin1index] > 0)
        {
            deltan = keyTimes[q1index] - keyTimes[q0index];
            deltanmin1 = keyTimes[q0index] - keyTimes[qmin1index];
        }
        else if (keyTimes[q0index] - keyTimes[qmin1index] > 0)
        {
            deltanmin1 = keyTimes[q0index] - keyTimes[qmin1index];
            deltan = deltanmin1; 
        }
        else if (keyTimes[q1index] - keyTimes[q0index] > 0)
        {
            deltan = keyTimes[q1index] - keyTimes[q0index];
            deltanmin1 = deltan;
        }

        
        double mul0 = 2 * deltanmin1 / (deltanmin1 + deltan);
        double mul1 = 2 * deltan / (deltanmin1 + deltan);

        // V0 = log(qn^-1 qn+1)
        // V1 = log(qn-1^-1 qn)
        // T=0.5*(V0+V1)

        float V0[] = Vec3f.getVec3f();
        float T0[] = Vec3f.getVec3f();
        float qTemp[] = Quat4f.getQuat4f(keyValues[q0index]);
        Quat4f.conjugate(qTemp);
        Quat4f.mul(qTemp, keyValues[q1index]);
        Quat4f.log(V0, qTemp);
        Vec3f.set(T0, V0);

        Quat4f.conjugate(qTemp, keyValues[qmin1index]);
        Quat4f.mul(qTemp, keyValues[q0index]);
        float V1[] = Vec3f.getVec3f();
        Quat4f.log(V1, qTemp);
        Vec3f.add(T0, V1);
        Vec3f.scale(0.5f, T0);

        float T1[] = Vec3f.getVec3f(T0);
        Vec3f.scale((float) mul0, T0);
        Vec3f.scale((float) mul1, T1);

        // an = qn exp((T0-V0)/2)
        Vec3f.sub(T0, V0);
        Vec3f.scale(0.5f, T0);
        float an[] = Quat4f.getQuat4f();
        Quat4f.exp(an, T0);
        Quat4f.mul(an, keyValues[q0index], an);

        // bn = qn exp((V1-T1)/2)
        Vec3f.sub(T1, V1, T1);
        Vec3f.scale(0.5f, T1);
        float bn[] = Quat4f.getQuat4f();
        Quat4f.exp(bn, T1);
        Quat4f.mul(bn, keyValues[q0index], bn);

        float t = (float)(time-keyTimes[q0index])/(float)deltan;
        Quat4f.squad(q, index, keyValues[q0index], 0, keyValues[q1index], 0, an, bn, t);
    }
}
