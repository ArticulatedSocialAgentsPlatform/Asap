/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;

import java.util.List;

import lombok.Delegate;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

import com.google.common.collect.ImmutableList;

/**
 * Plays a sequence of motionunits. Currently does not do anything smart with key positions.
 * @author hvanwelbergen
 *
 */
public class MUSequence implements MotionUnit
{
    private ImmutableList<MotionUnit> mus;

    public MUSequence(List<MotionUnit> mus)
    {
        this.mus = ImmutableList.copyOf(mus);
    }
    
    @Delegate
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    @Override
    public void play(double t) throws MUPlayException
    {
        double totalDuration = getPreferedDuration();
        double prev = 0;
        for (MotionUnit mu : mus)
        {
            double next = prev+mu.getPreferedDuration()/totalDuration;
            if(t>=prev && t<next)
            {
                double time = (t-prev);
                double dur = mu.getPreferedDuration()/totalDuration;
                mu.play(time/dur);
                return;
            }
            prev += mu.getPreferedDuration()/totalDuration;
        }
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    @Override
    public double getPreferedDuration()
    {
        double duration = 0;
        for (MotionUnit mu : mus)
        {
            duration += mu.getPreferedDuration();
        }
        return duration;
    }

    @Override
    public void setFloatParameterValue(String name, float value)
    {
        for (MotionUnit mu : mus)
        {
            try
            {
                mu.setFloatParameterValue(name, value);
            }
            catch (ParameterException ex)
            {

            }
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        for (MotionUnit mu : mus)
        {
            try
            {
                mu.setParameterValue(name, value);
            }
            catch (ParameterException ex)
            {

            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        for (MotionUnit mu : mus)
        {
            
            try
            {
                String value = mu.getParameterValue(name);
                return value;
            }
            catch(ParameterException ex)
            {
                
            }
            
        }
        throw new ParameterException("No parameter "+name+" defined.");
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        for (MotionUnit mu : mus)
        {
            
            try
            {
                float value = mu.getFloatParameterValue(name);
                return value;
            }
            catch(ParameterException ex)
            {
                
            }
            
        }
        throw new ParameterException("No parameter "+name+" defined.");
    }

}
