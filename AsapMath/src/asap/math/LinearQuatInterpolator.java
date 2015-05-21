/*******************************************************************************
 *******************************************************************************/
package asap.math;

import hmi.math.Quat4f;

import java.util.Arrays;

/**
 * SLERP interpolation between quaternions
 * @author hvanwelbergen
 *
 */
public class LinearQuatInterpolator implements QuatInterpolator
{
    private double keyTimes[];
    private float keyValues[][];
    
    public LinearQuatInterpolator()
    {
        
    }
    
    public LinearQuatInterpolator(double[][] pval)
    {
        setPVal(pval);
    }
    
    public void setPVal(double [][]pval)
    {
        keyValues = new float[pval.length][];
        keyTimes = new double[pval.length];
        for(int i=0;i<pval.length;i++)
        {
            keyValues[i] = new float[4];
            keyTimes[i] = pval[i][0];
            for(int j=0;j<4;j++)
            {
                keyValues[i][j] = (float)pval[i][j+1];   
            }                        
        }
    }
    
    public void interpolate(double time, float q[])
    {
        interpolate(time,q,0);
    }
    
    public void interpolate(double time, float q[], int index)
    {
        int q1index = Arrays.binarySearch(keyTimes, time);
        if(q1index < 0) //-q1index is insertion (-insertion)-1
        {
            q1index = q1index+1;
            q1index = -q1index; 
        }
        
        int q0index = 0;
        if(q1index > 0)
        {
            q0index = q1index - 1; 
        }
        if(q1index == keyTimes.length)
        {
            q1index = keyTimes.length - 1;
        }
        double dur = keyTimes[q1index]-keyTimes[q0index];
        float t = 0;
        if(dur>0)
        {
            t = (float) ((time - keyTimes[q0index])/dur);
        }
        
        Quat4f.interpolate(q, index, keyValues[q0index],0, keyValues[q1index],0, t);
    }
}
