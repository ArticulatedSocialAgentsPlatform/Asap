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
package asap.animationengine.motionunit;

import asap.animationengine.AnimationPlayer;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.ParameterException;

/**
 * An animation, typically with a peak-like structure, parameterized by a parameter set 
 * Contains a set of keys that map to 'world' time to animation time
 * @author welberge
 */
public interface MotionUnit extends KeyPositionManager
{
    void setFloatParameterValue(String name, float value)throws ParameterException;;
    void setParameterValue(String name, String value)throws ParameterException;;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
   
    
    /**
     * Executes the motion unit, typically by rotating some VJoints
     * @param t execution time, 0 &lt t &lt 1
     * @throws MUPlayException if the play fails for some reason
     */
    void play(double t)throws MUPlayException;    
    
    /**
     * Creates the TimedMotionUnit corresponding to this motion unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TMU
     */
    TimedMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,String id);
    
    /**
     * @return Prefered duration (in seconds) of this motion unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
    
    /**
     * Create a copy of this motion unit and link it to the animationplayer
     */
    MotionUnit copy(AnimationPlayer p);
    
    /**
     * Get the motionunit replacement group (=typically the BML behavior)
     * Used to determine the currently active persistent TMU for this group in the player
     * Only one group is active at a time
     * returns null if none
     */
    String getReplacementGroup();       
}
