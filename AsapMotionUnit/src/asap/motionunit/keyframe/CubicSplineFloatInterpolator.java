/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import java.util.ArrayList;
import java.util.List;

import asap.math.CubicSplineInterpolator;

import com.google.common.collect.ImmutableList;

/**
 * Cubic spline interpolator for of a list of keyframes. Interpolates each dof seperately.
 * @author hvanwelbergen
 *
 */
public class CubicSplineFloatInterpolator implements Interpolator
{
    private List<CubicSplineInterpolator> cubicSplineInterPolators = new ArrayList<CubicSplineInterpolator>();
    private List<KeyFrame> keyFrames;
    private int nrOfDof;
    
    @Override
    public void setKeyFrames(List<KeyFrame> frames, int nrOfDof)
    {
        keyFrames = frames;
        this.nrOfDof = nrOfDof;
        cubicSplineInterPolators.clear();
        for(int i=0;i<nrOfDof;i++)
        {
            double p[][]=new double[frames.size()][];
            int j=0;
            for(KeyFrame kf:frames)
            {
                p[j]=new double[2];
                p[j][0] = kf.getFrameTime();
                p[j][1] = kf.getDofs()[i];
                j++;
            }
            cubicSplineInterPolators.add(new CubicSplineInterpolator(p,0,0));            
        }
    }
    
    @Override
    public KeyFrame interpolate(double time)
    {
        float dofs[]=new float [cubicSplineInterPolators.size()];
        int i=0;
        for(CubicSplineInterpolator inter:cubicSplineInterPolators)
        {
            dofs[i] = (float)inter.interpolate(time);
            i++;
        }
        return new KeyFrame(time,dofs);
    }
    
    @Override
    public CubicSplineFloatInterpolator copy()
    {
        CubicSplineFloatInterpolator interp = new CubicSplineFloatInterpolator();
        interp.setKeyFrames(ImmutableList.copyOf(keyFrames), nrOfDof);
        return interp;
    }

}
