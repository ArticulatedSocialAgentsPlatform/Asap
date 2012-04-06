package asap.motionunit.keyframe;

import java.util.List;
import asap.motionunit.MUPlayException;
import asap.motionunit.MotionUnit;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;

import lombok.Delegate;
import asap.timemanipulator.TimeManipulator;
import asap.timemanipulator.LinearManipulator;

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
    
    public KeyFrameMotionUnit(Interpolator interp, TimeManipulator m)
    {
        this.manip = m;
        interpolator = interp;
    }
    
    public KeyFrameMotionUnit(Interpolator interp)
    {
        this(interp, new LinearManipulator());
    }
    
    protected double unifyKeyFrames(List<KeyFrame> keyFrames)
    {
        if(keyFrames.size()<2)
        {
            return 1;
        }
        double start = keyFrames.get(0).getFrameTime();
        double end = keyFrames.get(keyFrames.size()-1).getFrameTime();
        double preferedDuration = end - start;
        
        for(KeyFrame kf:keyFrames)
        {
            kf.setFrameTime( (kf.getFrameTime()-start) / preferedDuration);
        }
        return preferedDuration;
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        KeyFrame kf = interpolator.interpolate(manip.manip(t));
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
