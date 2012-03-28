package asap.motionunit.keyframe;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import asap.math.LinearQuatInterpolator;

public class LinearQuatFloatInterpolator implements Interpolator
{
    private List<LinearQuatInterpolator> linearQuatInterPolators = new ArrayList<LinearQuatInterpolator>();
    private List<KeyFrame> keyFrames;
    private int nrOfDof;
    
    @Override
    public void setKeyFrames(List<KeyFrame> frames, int nrOfDof)
    {
        keyFrames = frames;
        this.nrOfDof = nrOfDof;
        linearQuatInterPolators.clear();
        
        for(int i=0;i<nrOfDof;i++)
        {
            double p[][]=new double[frames.size()][];
            int j=0;
            for(KeyFrame kf:frames)
            {
                p[j]=new double[5];
                p[j][0] = kf.getFrameTime();
                for(int k=0;k<4;k++)
                {
                    p[j][k+1] = kf.getDofs()[i*4+k];
                }
                j++;
            }
            linearQuatInterPolators.add(new LinearQuatInterpolator(p));            
        }        
    }

    @Override
    public KeyFrame interpolate(double time)
    {
        float dofs[]=new float [linearQuatInterPolators.size()*4];
        int i=0;
        for(LinearQuatInterpolator inter:linearQuatInterPolators)
        {
            inter.interpolate(time, dofs,i);
            i+=4;
        }
        return new KeyFrame(time,dofs);
    }

    @Override
    public Interpolator copy()
    {
        LinearQuatFloatInterpolator copy = new LinearQuatFloatInterpolator();
        copy.setKeyFrames(ImmutableList.copyOf(keyFrames), nrOfDof);
        return copy;
    }
}
