/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;

import hmi.util.StringUtil;

import java.util.List;

import asap.emitterengine.Emitter;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Templated class; unit to create an emitter of type &lt;E&gt;
 * @author Dennis Reidsma
 */
public class CreateEmitterEU implements EmitterUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private Emitter theEmitter;

    public CreateEmitterEU()
    {
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        addKeyPosition(start);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(end);
    }

    public void setEmitter(Emitter emitter)
    {
        theEmitter = emitter;
    }

    public Emitter getEmitter()
    {
        return theEmitter;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (theEmitter.specifiesFloatParameter(name))
        {
            theEmitter.setFloatParameterValue(name, value);
        }
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (theEmitter.specifiesStringParameter(name))
        {
            theEmitter.setParameterValue(name, value);
        }
        else
        {
            if (StringUtil.isNumeric(value) && theEmitter.specifiesFloatParameter(name))
            {
                theEmitter.setFloatParameterValue(name, Float.parseFloat(value));
            }
            else
            {
                throw new InvalidParameterException(name, value);
            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return theEmitter.getParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (theEmitter.specifiesFloatParameter(name))
        {
            return theEmitter.getFloatParameterValue(name);
        }
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return theEmitter.hasValidParameters();
    }

    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        // start the emitter
        theEmitter.start();
    }

    /**
     * 
     * @param t
     *            execution time, 0 &lt; t &lt; 1
     * @throws EUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws EUPlayException
    {
    }

    public void cleanup()
    {
        theEmitter.stop();
    }

    /**
     * Creates the TimedEmitterUnit corresponding to this face unit
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TEU
     */
    @Override
    public TimedEmitterUnit createTEU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedEmitterUnit(bfm, bbPeg, bmlId, id, this);
    }

    /**
     * @return Prefered duration (in seconds) of this unit, 0 means not determined/infinite
     */
    public double getPreferedDuration()
    {
        return 0d;
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

}
