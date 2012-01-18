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
package asap.animationengine.controller;

import hmi.animation.VJoint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.InvalidParameterException;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.ControllerParameterNotFoundException;
import hmi.physics.controller.PhysicalController;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.*;


/**
 * MotionUnit for a physical controller
 * @author Herwin van Welbergen
 *
 */
public class ControllerMU implements MotionUnit
{
    private PhysicalController controller;
    private String replacementgroup = null;
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private AnimationPlayer aPlayer;
    
    /**
     * Constructor
     * @param pc physical controller linked to the motion unit
     * @param pcl physical controller list. This  ControllerMU adds the controller to this list whenever it's playing.
     */
    public ControllerMU(PhysicalController pc, AnimationPlayer player)
    {
        controller = pc;
        aPlayer = player;
        addKeyPosition(new KeyPosition("start",0,1));
        addKeyPosition(new KeyPosition("end",1,1));
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public void play(double t)
    {
        aPlayer.addController(controller);
        
    }

    /**
     * Create a copy for use in animation player p (that is, link up to its
     * VNext, PhysicalHumanoid, physically steered joint list
     * 
     * @param p
     *            animation player to use the MotionUnit in
     */
    @Override
    public MotionUnit copy(AnimationPlayer p)
    {
        PhysicalController pc = controller.copy(p.getPHuman());
        ControllerMU c = new ControllerMU(pc, p);
        return c;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        try
        {
            controller.setParameterValue(name, value);
        }
        catch (ControllerParameterNotFoundException e)
        {
            throw new ParameterNotFoundException(e.getParamId(),e);            
        }
        catch (ControllerParameterException e)
        {
            throw new InvalidParameterException(name,""+value,e);
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("replacementgroup"))
        {
            replacementgroup = value;
        } else
        {
            try
            {
                controller.setParameterValue(name, value);
            }
            catch (ControllerParameterNotFoundException e)
            {
                throw new ParameterNotFoundException(e.getParamId(),e);                
            }
            catch (ControllerParameterException e)
            {
                throw new InvalidParameterException(name,value,e);
            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("replacementgroup"))
        {
            return replacementgroup;
        }
        try
        {
            return controller.getParameterValue(name);
        }
        catch (ControllerParameterNotFoundException e)
        {
            throw new ParameterNotFoundException(e.getParamId(),e);            
        }        
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        try
        {
            return controller.getFloatParameterValue(name);
        }
        catch (ControllerParameterNotFoundException e)
        {
            throw new ParameterNotFoundException(e.getParamId(),e);            
        }
    }
    
    @Override
    public TimedMotionUnit createTMU(FeedbackManager bfm,BMLBlockPeg bbPeg,String bmlId, String id)
    {
        return new PhysicalTMU(bfm,bbPeg, bmlId, id, this);
    }

    @Override
    public String getReplacementGroup()
    {
        return replacementgroup;
    }

    public void reset()
    {
        controller.reset();
    }
    
    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);        
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
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }
    
    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    private static final Set<String> KINJOINTS = ImmutableSet.of();
    @Override
    public Set<String> getKinematicJoints()    
    {
        return KINJOINTS;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return controller.getJoints();        
    } 
}
