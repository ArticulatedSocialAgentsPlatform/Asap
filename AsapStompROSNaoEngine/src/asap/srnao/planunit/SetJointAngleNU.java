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
package asap.srnao.planunit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.srnao.NaoPlanner;
import asap.srnao.loader.StompROSNaoEmbodiment;

import com.google.common.primitives.Floats;

/**
 * Sets a certain joint of the nao robot to a specified angle
 * This NU directly controls a primitive offered by the nao embodiment, which allows for a "fire-and-forget" method of changing joint rotations with a certain speed. 
 * The nao offers an additional more fine-grained control primitive with which it is possible to specify a trajectory which is then interpolated (currently not implemented)
 * @author davisond
 *
 */
public class SetJointAngleNU implements NaoUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private static Logger logger = LoggerFactory.getLogger(NaoPlanner.class.getName());
    private String jointName = "";
    private float angle = 0.0f;
    private float speed = 0.5f;

    /**
     * The joint angle must be within this range before the behavior is considered to be finished
     */
    private static final float FLOATEQUAL_THRESHOLD = 0.005f;
    
    // the unique id of this NU as specified in the BML
    private String nuId;

	private StompROSNaoEmbodiment srne;

    public SetJointAngleNU()
    {
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(end);
    }

    public void setEmbodiment(StompROSNaoEmbodiment srne)
    {
    	this.srne = srne;
    }
    
    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("angle"))
        {
            angle = value;
        }
        if (name.equals("speed"))
        {
            speed = value;
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {

        if (name.equals("jointName"))
        {
            jointName = value;
        }
        else
        {
            Float f = Floats.tryParse(value);
            if (f!=null)
            {
                setFloatParameterValue(name, f);
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
        if (name.equals("jointName"))
        {
            return jointName.toString();
        }
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
    	if(name.equals("angle"))
    	{
    		return angle;
    	}
    	if(name.equals("speed"))
    	{
    		return speed;
    	}
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
    	//joint name valid: should be a correct joint name as specified by the NAO
    	boolean jointName_valid = srne.getNao().getJointState(jointName) != null;
    	
    	//speed valid: between 0.0 and 1.0
    	boolean  speed_valid = (0.0f <= speed && speed <= 1.0f);
    			
        return jointName_valid && speed_valid;
    }

    /** start the unit. */
    public void startUnit(double time) throws NUPlayException
    {
    	srne.setJointAngle(jointName, angle, speed);
        //logger.debug("Now displaying image {}", imageId);
        //display.addImage(nuId, imageId, layer);
    	
    }

    /**
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws NUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws NUPlayException
    {
    }

    public void cleanup()
    {
        // remove the current image from the layer
        //display.removeImage(nuId, layer);

    }

    /**
     * Creates the TimedNaoUnit
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TPU
     */
    @Override
    public TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        this.nuId = id;
        return new TimedNaoUnit(bfm, bbPeg, bmlId, id, this);
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not determined/infinite
     */
    public double getPreferedDuration()
    {
        return 0d;
    }

    /**
     * Create a copy of this nao unit and link it to the display
     */
    @Override
    public NaoUnit copy(StompROSNaoEmbodiment naoEmbodiment)
    {
        SetJointAngleNU result = new SetJointAngleNU();
        result.jointName = jointName;
        result.setEmbodiment(naoEmbodiment);
       
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

	@Override
	public boolean isBehaviorRunning() {
    	//monitor the joint to see if target position has been reached
    	float currentAngle = srne.getNao().getJointState(jointName).getAngle();
    	return (Math.abs(currentAngle-angle) > FLOATEQUAL_THRESHOLD);
	}

}
