package asap.motionunit.keyframe;

import java.util.ArrayList;
import java.util.List;

import asap.math.LinearInterpolator;

public class LinearFloatInterpolator implements Interpolator
{
    List<LinearInterpolator> linInterPolators = new ArrayList<LinearInterpolator>();
    
    @Override
    public void setKeyFrames(List<KeyFrame> frames, int nrOfDof)
    {
        linInterPolators.clear();
        for(int i=0;i<nrOfDof;i++)
        {
            double p[][]=new double[frames.size()][];
            for(KeyFrame kf:frames)
            {
                p[i][0] = kf.getFrameTime();
                p[i][1] = kf.getDofs()[i];
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
}
