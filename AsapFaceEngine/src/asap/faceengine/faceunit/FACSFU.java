/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.faceengine.faceunit;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.InvalidParameterException;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.FACSConfiguration;
import hmi.faceanimation.model.MPEG4Configuration;
import hmi.util.StringUtil;
import lombok.Delegate;

import asap.utils.AnimationSync;

/**
 * A basic facial animation unit consisting of one FACS configuration The key
 * positions are: start, ready, relax, end. This describes an apex-like
 * intensity development: Between start and ready, the face configuration is
 * blended in; between relax and end the face configuration is blended out.
 * 
 * Parameter constraint: facsConfig should not be null
 * 
 * @author Dennis Reidsma
 */
public class FACSFU implements FaceUnit
{
    @Delegate
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    protected float intensity = 1f;

    protected FACSConfiguration facsConfig = new FACSConfiguration();

    private FaceController faceController;
    private FACSConverter facsConverter;

    private final MPEG4Configuration mpeg4Config = new MPEG4Configuration(); // remember
                                                                             // last
                                                                             // face
                                                                             // configuration
                                                                             // sent
                                                                             // to
                                                                             // FaceController,
                                                                             // because
                                                                             // we
                                                                             // need
                                                                             // to
                                                                             // subtract
                                                                             // it
                                                                             // again
                                                                             // later
                                                                             // on!

    public FACSFU()
    {
        KeyPosition ready = new KeyPosition("ready", 0.1d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(ready);
        addKeyPosition(relax);
        addKeyPosition(end);
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }
    public void setFACSConverter(FACSConverter fc)
    {
        facsConverter = fc;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("intensity"))
            intensity = value;
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if(StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }
        else
        {
            throw new InvalidParameterException(name, value);
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return ""+getFloatParameterValue(name);        
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("intensity")) return intensity;
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return facsConfig != null;
    }

    public void setConfig(FACSConfiguration fc)
    {
        facsConfig = fc;
    }

    /**
     * Executes the face unit, by applying the face configuration. Linear
     * interpolate from intensity 0..max between start and ready; keep at max
     * till relax; then back to zero from relax till end.
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        // between where and where? Linear interpolate from intensity 0..max between start&Ready;
        // then down from relax till end
        double ready = getKeyPosition("ready").time;
        double relax = getKeyPosition("relax").time;
        float newAppliedWeight = 0;

        if (t < ready && t > 0)
        {
            newAppliedWeight = intensity * (float) (t / ready);
        }
        else if (t >= ready && t <= relax)
        {
            newAppliedWeight = intensity;
        }
        else if (t > relax && t < 1)
        {
            newAppliedWeight = intensity
                    * (float) (1 - ((t - relax) / (1 - relax)));
        }

        synchronized (AnimationSync.getSync())
        {

            faceController.removeMPEG4Configuration(mpeg4Config);
            facsConverter.convert(facsConfig, mpeg4Config);
            mpeg4Config.multiply(newAppliedWeight);
            faceController.addMPEG4Configuration(mpeg4Config);
        }
    }

    public void cleanup()
    {
        if (mpeg4Config != null) faceController.removeMPEG4Configuration(mpeg4Config);
    }

    /**
     * Creates the TimedFaceUnit corresponding to this face unit
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TFU
     */
    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm,BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public String getReplacementGroup()
    {
        return null;
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not
     *         determined/infinite
     */
    public double getPreferedDuration()
    {
        return 1d;
    }

    /**
     * Create a copy of this face unit and link it to the facecopntroller
     */
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        FACSFU result = new FACSFU();
        result.setFaceController(fc);
        result.setFACSConverter(fconv);
        result.intensity=intensity;
        result.setConfig(facsConfig);
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }
}
