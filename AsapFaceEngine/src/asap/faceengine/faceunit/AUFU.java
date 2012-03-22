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
import hmi.faceanimation.model.ActionUnit;
import hmi.faceanimation.model.ActionUnit.Symmetry;
import hmi.faceanimation.model.FACS;
import hmi.faceanimation.model.FACS.Side;
import hmi.faceanimation.model.FACSConfiguration;
import hmi.faceanimation.model.MPEG4Configuration;
import hmi.util.StringUtil;

import java.util.List;

import asap.utils.AnimationSync;

/**
 * A basic facial animation unit consisting of one AU value. The key positions are: start, ready,
 * relax, end. This descripes an apex-like intensity development: Between start and ready, the face
 * configuration is blended in; between relax and end the face configuration is blended out.
 * 
 * Parameter cosntraints: side valuefits with AU... (e.g., no null for asymmetric AU, no RIGHT, LEFT
 * for symmetric)
 * 
 * @author Dennis Reidsma
 */
public class AUFU implements FaceUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    enum AUFUSide
    {
        LEFT, RIGHT, BOTH
    }

    private AUFUSide side = null;

    private int aunr = -1;

    private float intensity = -1;;

    private FACSConfiguration facsConfig = new FACSConfiguration();

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

    public AUFU()
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
        if (name.equals("intensity")) intensity = value;
        else throw new ParameterNotFoundException(name);
        setAU(side, aunr, intensity);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("au"))
        {
            aunr = Integer.parseInt(value);
            // System.out.println("aunr: " + aunr);
        }
        else if (name.equals("side"))
        {
            side = AUFUSide.BOTH;
            if (value.equals("LEFT")) side = AUFUSide.LEFT;
            else if (value.equals("RIGHT")) side = AUFUSide.RIGHT;
            // System.out.println("side: " + side);
        }
        else
        {
            if(StringUtil.isNumeric(value))
            {
                setFloatParameterValue(name, Float.parseFloat(value));
            }
            else
            {
                throw new InvalidParameterException(name,value);
            }
        }
        setAU(side, aunr, intensity);
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("side"))
        {
            if (side == null)
            {
                throw new ParameterNotFoundException(name);
            }
            return "" + side;
        }
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("intensity")) return intensity;
        if (name.equals("au")) return aunr;
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        ActionUnit au = FACS.getActionUnit(aunr);
        if (au == null) return false;
        if (intensity > 1) return false;
        if (au.getSymmetry() != Symmetry.ASYMMETRIC) return (side == AUFUSide.BOTH) || (side == null);

        return (side != null);
    }

    public void setAU(AUFUSide s, int i, float intens)
    {
        side = s;
        aunr = i;
        intensity = intens;
        if (!hasValidParameters())
        {
            return;
        }
        facsConfig = new FACSConfiguration();
        ActionUnit au = FACS.getActionUnit(aunr);
        if (au.getSymmetry() != Symmetry.ASYMMETRIC)
        {
            facsConfig.setValue(Side.NONE, au.getIndex(), intensity);
        }
        else
        {
            if (side == AUFUSide.LEFT) facsConfig.setValue(Side.LEFT, au.getIndex(), intensity);
            if (side == AUFUSide.RIGHT) facsConfig.setValue(Side.RIGHT, au.getIndex(), intensity);
            if (side == AUFUSide.BOTH)
            {
                facsConfig.setValue(Side.LEFT, au.getIndex(), intensity);
                facsConfig.setValue(Side.RIGHT, au.getIndex(), intensity);
            }
        }
        // System.out.println("AU: " + au.getNumber() + ", " + au.getIndex() +
        // ", " + au.getSymmetry() + ", " + intensity);
    }

    /**
     * Executes the face unit, by applying the face configuration. Linear interpolate from intensity
     * 0..max between start and ready; keep at max till relax; then back to zero from relax till
     * end.
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        // between where and where? Linear interpolate from intensity 0..max
        // between start&Ready; then down from relax till end
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
            newAppliedWeight = intensity * (float) (1 - ((t - relax) / (1 - relax)));
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
     * 
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
     * @return Prefered duration (in seconds) of this face unit, 0 means not determined/infinite
     */
    public double getPreferedDuration()
    {
        return 1d;
    }

    /**
     * Create a copy of this face unit and link it to the faceContrller
     */
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        AUFU result = new AUFU();
        result.setFaceController(fc);        
        result.setFACSConverter(fconv);        
        result.setAU(side, aunr, intensity);
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
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
