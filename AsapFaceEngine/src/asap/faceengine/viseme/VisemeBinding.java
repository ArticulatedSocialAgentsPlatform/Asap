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
package asap.faceengine.viseme;

import asap.faceengine.faceunit.TimedFaceUnit;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.faceanimation.FaceController;

/**
 * The VisemeBinding maps from visemes to FaceUnits. different avatars have
 * really different VisemeBindings, because some avatars only support morphing,
 * or other only FAPs, etc....
 * 
 * @author Dennis Reidsma
 */
public interface VisemeBinding
{
    /**
     * Get a viseme unit for viseme viseme. If the viseme is not found, an 'empty' TimedFaceUnit is returned.
     * 
     * note: each viseme has attackPeak=relax=peak, and start=prev.peak and
     * end=next.peak for timing. Ugly but effective.<br>
     * 
     */
    TimedFaceUnit getVisemeUnit(FeedbackManager bfm,BMLBlockPeg bbPeg, Behaviour b, int viseme,
            FaceController fc);
    
    /**
     * Get a visime unit that is not hooked up to the feedbackmanager
     * If the viseme is not found, an 'empty' TimedFaceUnit is returned.
     * 
     * note: each viseme has attackPeak=relax=peak, and start=prev.peak and
     * end=next.peak for timing. Ugly but effective.<br>
     */
    TimedFaceUnit getVisemeUnit(BMLBlockPeg bbPeg, Behaviour b, int viseme,            
            FaceController fc);
}
