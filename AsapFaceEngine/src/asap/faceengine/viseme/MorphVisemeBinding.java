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

import asap.faceengine.faceunit.MorphFU;
import asap.faceengine.faceunit.TimedFaceUnit;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.faceanimation.FaceController;

/**
 * Implementation that realizers visemes as morphs
 * 
 * @author Dennis Reidsma
 */
public class MorphVisemeBinding implements VisemeBinding
{
    private VisemeToMorphMapping visemeMapping;
    
    public MorphVisemeBinding(VisemeToMorphMapping mapping)
    {
        visemeMapping = mapping;
    }

    @Override
    public TimedFaceUnit getVisemeUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, Behaviour b, int viseme, FaceController fc) 
                                                                // dur, ..
    {   // times are relative, and just used to determine whether peak is
        // a-centric
        MorphFU visemeFU = new MorphFU();
        if (viseme == -1)
            viseme = 0;
        MorphVisemeDescription desc = visemeMapping.getMorphTargetForViseme(viseme);
        
        String targetName = "";
        visemeFU.setIntensity(1);
        if(desc!=null)
        {
            visemeFU.setIntensity(desc.getIntensity());
            targetName = desc.getMorphName();
        }
        
        if (!fc.getPossibleFaceMorphTargetNames().contains(targetName))
            targetName = "";
            
        visemeFU.setTargetName(targetName);

        TimedFaceUnit tfu = visemeFU.copy(fc, null, null).createTFU(bfm, bbPeg, b.getBmlId(), b.id);
        // time pegs not yet set. Here we just arrange relative timing
        tfu.getKeyPosition("ready").time = 0.5;
        tfu.getKeyPosition("relax").time = 0.5;
        
        return tfu;
    }

    @Override
    public TimedFaceUnit getVisemeUnit(BMLBlockPeg bbPeg, Behaviour b, int viseme, FaceController fc)
    {
        return getVisemeUnit(NullFeedbackManager.getInstance(),bbPeg,b,viseme,fc);
    }

}
