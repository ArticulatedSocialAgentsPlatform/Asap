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

import hmi.faceanimation.FaceController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.MorphFU;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;

/**
 * Implementation that realizers visemes as morphs
 * 
 * @author Dennis Reidsma
 */
@Slf4j
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
        
        Set<String> targetNames = new HashSet<String>();
        visemeFU.setIntensity(1);
        if(desc!=null)
        {
            visemeFU.setIntensity(desc.getIntensity());
            targetNames.addAll(desc.getMorphNames());
        }
        
        List<String>removeTargets = new ArrayList<String>();
        for(String target:targetNames)
        {
            if (!fc.getPossibleFaceMorphTargetNames().contains(target))
            {
                removeTargets.add(target);
                log.warn("Morphvisemebinding refers to non-existing morph {}", target);
            }
        }
        targetNames.removeAll(removeTargets);
            
        visemeFU.setMorphTargets(targetNames);
        
        TimedFaceUnit tfu = visemeFU.copy(fc, null, null).createTFU(bfm, bbPeg, b.getBmlId(), b.id);
        // time pegs not yet set. Here we just arrange relative timing
        tfu.getKeyPosition("attackPeak").time = 0.5;
        tfu.getKeyPosition("relax").time = 0.5;
        
        return tfu;
    }

    @Override
    public TimedFaceUnit getVisemeUnit(BMLBlockPeg bbPeg, Behaviour b, int viseme, FaceController fc)
    {
        return getVisemeUnit(NullFeedbackManager.getInstance(),bbPeg,b,viseme,fc);
    }

}
