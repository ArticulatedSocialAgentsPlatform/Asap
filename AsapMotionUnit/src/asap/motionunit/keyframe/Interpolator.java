/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import java.util.List;

/**
 * Interpolates keyframes 
 * @author hvanwelbergen
 */
public interface Interpolator
{
    KeyFrame interpolate(double time);
    void setKeyFrames(List<KeyFrame> frames, int nrOfDof);
    Interpolator copy();
}
