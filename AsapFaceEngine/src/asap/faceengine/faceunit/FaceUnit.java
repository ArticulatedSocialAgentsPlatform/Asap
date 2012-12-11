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
import asap.motionunit.MotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;

/**
 * A facial animation, typically with a peak-like structure. 
 * Contains a set of keys that map to 'world' time to animation time.
 * @author Dennis Reidsma
 */
public interface FaceUnit extends MotionUnit
{
    boolean hasValidParameters();    
   
    /** Clean up the face - i.e. remove traces of this faceunit */    
    void cleanup();    
    
    /**
     * Creates the TimedFaceUnit corresponding to this face unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TFU
     */
    TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id);    
    
    /**
     * Create a copy of this face unit and link it to the faceplayer
     */
    FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv); 
}