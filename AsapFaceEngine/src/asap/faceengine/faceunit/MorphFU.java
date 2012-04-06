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
import hmi.util.StringUtil;

import java.util.Arrays;
import lombok.Delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.utils.AnimationSync;

/**
 * A basic facial animation unit consisting of one morph target. The key
 * positions are: start, ready, relax, end. This descripes an apex-like
 * intensity development: The between start and ready, the morph target is
 * blended in; between relax and end the morph target is blended out. The max
 * intensity for the morph target can also be specified.
 * 
 * More than one MorphFU can be active at the same time. Parameter constraints:
 * none
 * 
 * @author Dennis Reidsma
 */
public class MorphFU implements FaceUnit
{
    private float intensity = 1f;

    private String targetName = "";

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    private static Logger logger = LoggerFactory.getLogger(MorphFU.class.getName());
    private boolean multiple = false;
    @Delegate private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private String[] morphTargets = new String[] { "" };

    private FaceController faceController;

    private float prevMorphedWeight = 0; // remember last morph weight sent to
                                         // FaceController, because we need to
                                         // subtract it again later on!

    public MorphFU()
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

    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        if (name.equals("intensity")) intensity = value;
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("targetname"))
        {
            targetName = value;
            updateMorphTargets();
        }
        else if (name.equals("multiple"))
        {
            multiple = Boolean.parseBoolean(value);
            updateMorphTargets();
        }
        else
        {
            if (StringUtil.isNumeric(value))
            {
                setFloatParameterValue(name, Float.parseFloat(value));
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
        if (name.equals("targetname")) return "" + targetName;
        if (name.equals("multiple")) return "" + multiple;
        return "" + getFloatParameterValue(name);
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
        return true;
    }

    private void updateMorphTargets()
    {
        if (!multiple)
        {
            morphTargets = new String[] { targetName };
        }
        else
        {
            morphTargets = targetName.split(",");
        }
    }

    /**
     * Executes the face unit, by morphing the face. Linear interpolate from
     * intensity 0..max between start and ready; keep at max till relax; then
     * back to zero from relax till end.
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        logger.debug("Playing FU at time={}", t);
        // between where and where? Linear interpolate from intensity 0..max
        // between start&Ready; then down from relax till end

        double ready = getKeyPosition("ready").time;
        double relax = getKeyPosition("relax").time;
        // System.out.println("ready: "+ready);
        // System.out.println("relax: "+relax);
        float newMorphedWeight = 0;

        if (t < ready && t > 0)
        {
            newMorphedWeight = intensity * (float) (t / ready);
        }
        else if (t >= ready && t <= relax)
        {
            newMorphedWeight = intensity;
        }
        else if (t > relax && t < 1)
        {
            newMorphedWeight = intensity * (float) (1 - ((t - relax) / (1 - relax)));
        }
        float[] prevWeights = new float[morphTargets.length];
        for (int i = 0; i < prevWeights.length; i++)
            prevWeights[i] = prevMorphedWeight;

        synchronized (AnimationSync.getSync())
        {
            faceController.removeMorphTargets(morphTargets, prevWeights);
            // System.out.println("Playing FU at time=" + t);
            logger.debug("RemoveWeight=" + prevMorphedWeight);
            logger.debug("NewWeight=" + newMorphedWeight);
            logger.debug("target: " + Arrays.toString(morphTargets));
            float[] newWeights = new float[morphTargets.length];
            for (int i = 0; i < newWeights.length; i++)
                newWeights[i] = newMorphedWeight;
            faceController.addMorphTargets(morphTargets, newWeights);
        }
        prevMorphedWeight = newMorphedWeight;
    }

    public void cleanup()
    {
        float[] prevWeights = new float[morphTargets.length];
        for (int i = 0; i < prevWeights.length; i++)
            prevWeights[i] = prevMorphedWeight;
        faceController.removeMorphTargets(morphTargets, prevWeights);
        prevMorphedWeight = 0;
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
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
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
     * Create a copy of this face unit and link it to the facecontroller
     */
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        MorphFU result = new MorphFU();
        result.setFaceController(fc);
        result.intensity = intensity;
        result.targetName = targetName;
        result.multiple = multiple;

        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        result.updateMorphTargets();
        return result;
    }
}
