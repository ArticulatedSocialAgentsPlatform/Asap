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
import asap.srnao.robot.NaoJoint;

import com.google.common.primitives.Floats;

/**
 * Sets a certain joint of the nao robot to a specified angle
 * @author davisond
 *
 */
public class MoveJointNU implements NaoUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private static Logger logger = LoggerFactory.getLogger(NaoPlanner.class.getName());
    private String jointName = "";
    private float angle = 0.0f;

    // the unique id of this NU as specified in the BML
    private String nuId;

    /**
     * The joint angle must be within this range before the behavior is considered to be finished
     */
    private static final float FLOATEQUAL_THRESHOLD = 0.005f;
    
	private StompROSNaoEmbodiment srne;

	private float startAngle;
	private float speed;
	
	private int framenr = 0;

	private float prevAngle;

    public MoveJointNU()
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
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
    	//joint name valid: should be a correct joint name as specified by the NAO
    	boolean jointName_valid = srne.getNao().getJointState(jointName) != null;
    	
        return jointName_valid;
    }

    /** start the unit. */
    public void startUnit(double time) throws NUPlayException
    {
    	startAngle = srne.getNao().getJointState(jointName).getAngle();
    	
    	//start at a very low speed, and speed it up gradually as the play progresses
    	speed = 0.01f;
    	srne.setJointAngle(jointName, angle, speed);
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
    	framenr++;
    	if(framenr%50 == 0)
    	{
	    	float currentAngle = srne.getNao().getJointState(jointName).getAngle();	
	    	float progress = (float) ((Math.max(angle, startAngle) - Math.min(angle, startAngle)) * t);
	    	float expectedAngle = (startAngle < angle) ? startAngle + progress : startAngle - progress;
	    	
	    	float diff = Math.abs(Math.max(currentAngle, expectedAngle) - Math.min(currentAngle, expectedAngle));
	    	
	    	//now figure out whether to increase or decrease the speed
	    	if(startAngle < angle)
	    	{
	    		if(prevAngle < currentAngle && currentAngle < expectedAngle)
	    		{
	    			//do nothing, speed is ok
	    		}
	    		else if(currentAngle < prevAngle)
	    		{
	    			speed = speed * 1.1f;
	    		} 
	    		else if (currentAngle > expectedAngle)
	    		{
	    			speed = speed / 2;
	    		}
	    	}
	    	else if(startAngle > angle)
	    	{
	    		if(prevAngle > currentAngle && currentAngle > expectedAngle)
	    		{
	    			//do nothing, speed is ok
	    		}
	    		else if(currentAngle < expectedAngle)
	    		{
	    			speed = speed / 2;
	    		} 
	    		else if (currentAngle > prevAngle)
	    		{
	    			speed = speed * 1.1f;
	    		}
	    	}
	    	//speed should always be between 0..1
	    	speed = Math.max(0.00001f, Math.min(0.99999f, speed));
	    	System.out.printf("SA: %.6f TA: %.6f T: %.6f Progress: %.6f Expected: %.6f Current: %.6f Diff: %.6f Speed: %.6f \r\n", startAngle, angle, t, progress, expectedAngle, currentAngle, diff, speed);
	    	srne.setJointAngle(jointName, angle, speed);
	    	prevAngle = expectedAngle;
    	}
    }

    public void cleanup()
    {
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
        MoveJointNU result = new MoveJointNU();
        result.jointName = jointName;
        result.angle = angle;
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
