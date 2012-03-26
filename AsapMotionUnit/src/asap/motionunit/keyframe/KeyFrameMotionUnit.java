package asap.motionunit.keyframe;

import java.util.List;
import java.util.ArrayList;
import asap.motionunit.MUPlayException;
import asap.motionunit.MotionUnit;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;

import lombok.Delegate;

/**
 * Generic implementation a motion motion unit that interpolates a set of keyframes
 * @author hvanwelbergen
 *
 */
public abstract class KeyFrameMotionUnit implements MotionUnit
{
    @Delegate private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    
    private List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
    
    private final Interpolator interpolator;
    
    public KeyFrameMotionUnit(Interpolator interp)
    {
        interpolator = interp;
    }
    
    public void addKeyFrame(KeyFrame kf)
    {
        keyFrames.add(kf);
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        KeyFrame kf = interpolator.interpolate(t);
        applyKeyFrame(kf);
    }

    public abstract void applyKeyFrame(KeyFrame kf);
    
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
