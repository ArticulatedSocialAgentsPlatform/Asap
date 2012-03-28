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

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.motionunit.MotionUnit;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;

/**
 * An animation, typically with a peak-like structure, parameterized by a parameter set 
 * Contains a set of keys that map to 'world' time to animation time
 * @author welberge
 */
public interface AnimationUnit extends MotionUnit
{
    
    Set<String> getPhysicalJoints();
    Set<String> getKinematicJoints();
    
    /**
     * Creates the TimedMotionUnit corresponding to this motion unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TMU
     */
    TimedAnimationUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,String id, PegBoard pb);
    
    /**
     * Create a copy of this motion unit and link it to the animationplayer
     */
    AnimationUnit copy(AnimationPlayer p) throws MUSetupException;
}
