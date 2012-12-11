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

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.util.AnimationSync;
import hmi.util.StringUtil;

import java.util.Arrays;
import java.util.Set;

import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.base.Joiner;

/**
 * A basic facial animation unit consisting of one morph target. The key
 * positions are: start, attackPeak, relax, end. This descripes an apex-like
 * intensity development: The between start and attackPeak, the morph target is
 * blended in; between relax and end the morph target is blended out. The max
 * intensity for the morph target can also be specified.
 * 
 * More than one MorphFU can be active at the same time. Parameter constraints:
 * none
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public class MorphFU implements FaceUnit
{
    private float intensity = 1f;

    private String targetName = "";

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }
    @Override
    public void startUnit(double t)
    {
        
    }
    
    @Delegate private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private String[] morphTargets = new String[] { "" };

    private FaceController faceController;

    private volatile float prevMorphedWeight = 0; // remember last morph weight sent to
                                         // FaceController, because we need to
                                         // subtract it again later on!

    public void setMorphTargets(Set<String> targets)
    {
        morphTargets = targets.toArray(new String[targets.size()]);
        setTargetName(Joiner.on(",").join(morphTargets));
    }
    
    public MorphFU()
    {
        KeyPosition attackPeak = new KeyPosition("attackPeak", 0.1d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(attackPeak);
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
        morphTargets = targetName.split(",");        
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
        log.debug("Playing FU at time={}", t);
        // between where and where? Linear interpolate from intensity 0..max
        // between start&Ready; then down from relax till end

        double attackPeak = getKeyPosition("attackPeak").time;
        double relax = getKeyPosition("relax").time;
        // System.out.println("ready: "+ready);
        // System.out.println("relax: "+relax);
        float newMorphedWeight = 0;

        if (t < attackPeak && t > 0)
        {
            newMorphedWeight = intensity * (float) (t / attackPeak);
        }
        else if (t >= attackPeak && t <= relax)
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
            log.debug("RemoveWeight=" + prevMorphedWeight);
            log.debug("NewWeight=" + newMorphedWeight);
            log.debug("target: " + Arrays.toString(morphTargets));
            float[] newWeights = new float[morphTargets.length];
            for (int i = 0; i < newWeights.length; i++)
                newWeights[i] = newMorphedWeight;
            faceController.addMorphTargets(morphTargets, newWeights);
        }
        prevMorphedWeight = newMorphedWeight;
    }

    public void cleanup()
    {
        synchronized (AnimationSync.getSync())
        {
            float[] prevWeights = new float[morphTargets.length];
            for (int i = 0; i < prevWeights.length; i++)
                prevWeights[i] = prevMorphedWeight;
            faceController.removeMorphTargets(morphTargets, prevWeights);
        }
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

        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        result.updateMorphTargets();
        return result;
    }
}
