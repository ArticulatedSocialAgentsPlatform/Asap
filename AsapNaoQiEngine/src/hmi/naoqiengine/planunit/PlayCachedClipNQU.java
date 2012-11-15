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
package hmi.shaderengine.planunit;


import java.util.*;

import hmi.shaderengine.*;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import hmi.renderenvironment.*;

import hmi.util.StringUtil;


/**
 * @author Dennis Reidsma
 */
public class SetShaderParameterSU implements ShaderUnit
{
private String mesh = null;
private String material = null;
private String parameter = null;
private float value = 0f;

    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
	private HmiRenderBodyEmbodiment hrbe = null;
    public SetShaderParameterSU()
    {
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        addKeyPosition(start);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(end);
    }
    
    public void setEmbodiment(HmiRenderBodyEmbodiment hrbe)
    {
      this.hrbe = hrbe;
    }
    
    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("value"))
        {
          this.value=value;
        }
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("mesh"))
        {
          mesh = value;
        }
        else if (name.equals("material"))
        {
          material = value;
        }
        else if (name.equals("parameter"))
        {
          parameter = value;
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
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("mesh"))
        {
          return mesh;
        }
        else if (name.equals("material"))
        {
          return material;
        }
        else if (name.equals("parameter"))
        {
          return parameter;
        }
        else if (name.equals("value"))
        {
          return ""+value;
        }
		else
		{
			throw new ParameterNotFoundException(name);
		}
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("value"))
        {
            return value;
        }
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return (mesh!=null && material!=null && parameter!=null);
    }

    public void startUnit(double time) throws TimedPlanUnitPlayException
    {         
      
    }

    /**
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws SUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws SUPlayException

    {
		hrbe.setShaderParameter(mesh,material,parameter,value);
    }
    
    public void cleanup()
    {
      //
    }
    
    /**
     * Creates the TimedShaderUnit
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TSU
     */
    @Override
    public TimedShaderUnit createTSU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedShaderUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public String getReplacementGroup()
    {
      return null;
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
