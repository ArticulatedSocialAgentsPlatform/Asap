/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import java.util.List;

import lombok.Delegate;
import asap.motionunit.MUPlayException;
import asap.motionunit.MotionUnit;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.timemanipulator.LinearManipulator;
import asap.timemanipulator.TimeManipulator;

/**
 * Generic implementation a motion motion unit that interpolates a set of keyframes
 * @author hvanwelbergen
 *
 */
public abstract class KeyFrameMotionUnit implements MotionUnit
{
    @Delegate private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private TimeManipulator manip = new LinearManipulator();
    
    private final Interpolator interpolator;
    private final boolean allowDynamicStart;
    
    public KeyFrameMotionUnit(Interpolator interp, TimeManipulator m, boolean allowDynamicStart)
    {
        this.allowDynamicStart = allowDynamicStart;
        this.manip = m;
        interpolator = interp;
    }
    
    public KeyFrameMotionUnit(Interpolator interp)
    {
        this(interp, new LinearManipulator(), true);
    }
    
    protected double unifyKeyFrames(List<KeyFrame> keyFrames)
    {
        double preferedDuration = keyFrames.get(keyFrames.size()-1).getFrameTime();
        
        for(KeyFrame kf:keyFrames)
        {
            kf.setFrameTime(kf.getFrameTime() / preferedDuration);
        }
        return preferedDuration;
    }
    
    protected void setupDynamicStart(List<KeyFrame> keyFrames)
    {
        if(allowDynamicStart)
        {
            if(keyFrames.size()>0) 
            {
                if(keyFrames.get(0).getFrameTime()>0)
                {
                    keyFrames.add(0,getStartKeyFrame());
                }
            }
            else
            {
                keyFrames.add(getStartKeyFrame());
            }            
        }
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        KeyFrame kf = interpolator.interpolate(manip.manip(t));
        applyKeyFrame(kf);
    }

    public abstract void applyKeyFrame(KeyFrame kf);
    
    public abstract KeyFrame getStartKeyFrame();
    
    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }
}
