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

import asap.faceengine.faceunit.FACSFU;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import saiba.bml.core.Behaviour;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.FACSConfiguration;

/**
 * Implementation that realizers visemes as FACS expressions
 * 
 * @author Dennis Reidsma
 */
public class FACSVisemeBinding implements VisemeBinding
{
    private VisemeToFACSMapping visemeMapping;
    private FACSConverter fconv;
    
    public FACSVisemeBinding(VisemeToFACSMapping mapping, FACSConverter fconv)
    {
        visemeMapping = mapping;
        this.fconv = fconv;
    }

    @Override
    public TimedFaceUnit getVisemeUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, Behaviour b, int viseme, FaceController fc) 
                                                                // dur, ..
    {   // times are relative, and just used to determine whether peak is
        // a-centric
        FACSFU visemeFU = new FACSFU();

        if (viseme == -1)
            viseme = 0;
        FACSConfiguration facsConf = visemeMapping.getFACSConfigurationForViseme(viseme);
        
        String targetName = "";
        //visemeFU.setFloatParameterValue("intensity", 1f);
        if(facsConf!=null)
        {
            //visemeFU.setFloatParameterValue("intensity", desc.getIntensity());
            visemeFU.setConfig(facsConf);
        }
        
        TimedFaceUnit tfu = visemeFU.copy(fc, fconv, null).createTFU(bfm, bbPeg, b.getBmlId(), b.id);
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
