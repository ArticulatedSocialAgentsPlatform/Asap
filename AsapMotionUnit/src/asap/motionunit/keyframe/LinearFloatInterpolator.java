/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import java.util.ArrayList;
import java.util.List;

import asap.math.LinearInterpolator;

import com.google.common.collect.ImmutableList;

/**
 * Linear interpolator for of a list of keyframes. Interpolates each dof seperately. 
 * @author hvanwelbergen
 *
 */
public class LinearFloatInterpolator implements Interpolator
{
    private List<LinearInterpolator> linInterPolators = new ArrayList<LinearInterpolator>();
    private List<KeyFrame> keyFrames;
    private int nrOfDof;
    
    @Override
    public void setKeyFrames(List<KeyFrame> frames, int nrOfDof)
    {
        keyFrames = frames;
        this.nrOfDof = nrOfDof;
        linInterPolators.clear();
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
            linInterPolators.add(new LinearInterpolator(p));            
        }
    }
    
    @Override
    public KeyFrame interpolate(double time)
    {
        float dofs[]=new float [linInterPolators.size()];
        int i=0;
        for(LinearInterpolator inter:linInterPolators)
        {
            dofs[i] = (float)inter.interpolate(time);
            i++;
        }
        return new KeyFrame(time,dofs);
    }
    
    @Override
    public LinearFloatInterpolator copy()
    {
        LinearFloatInterpolator interp = new LinearFloatInterpolator();
        interp.setKeyFrames(ImmutableList.copyOf(keyFrames), nrOfDof);
        return interp;
    }
}
