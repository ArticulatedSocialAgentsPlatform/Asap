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
package hmi.elckerlyc.faceengine.viseme;

import hmi.elckerlyc.TimePeg;

/** A Viseme FaceUnit represents the facial configuration for one viseme. */
public class VisemeFU
{
  /* =====================================
           INFO ABOUT THIS VISEME
     ===================================== */

    public int prevViseme = V_NULL; //not used yet
    public int curViseme  = V_SIL;
    public int nextViseme = V_NULL; //not used yet
    public double visemeDuration = -1; //not used yet
    
    protected TimePeg timePeg = null;    

    /** ID of the viseme, unique in combination with speechUnitID and BMLBlockID */
    private String id;
    /** ID of speechunit to which this viseme belongs */
    private String speechUnitId;
    /** ID of the BML Block of the speechunit to which this viseme belongs */
    private String bmlBlockId;

de viseme units hebben als replacement Group 'visemeunits' ofzo.
    
    public VisemeUnit(int prevVis, int curVis, int nextVis, double dur, TimePeg tp, String id, String speechId, String bmlId)
    {
        prevViseme = prevVis;       
        curViseme  = curVis;       
        nextViseme = nextVis;       
        visemeDuration = dur;
        timePeg = tp;
        this.id = id;
        speechUnitId = speechId;
        bmlBlockId = bmlId;
    }
    

    public TimePeg getTimePeg()
    {
        return timePeg;
    }

    public void setTimePeg(TimePeg tp)
    {
        timePeg = tp;
    }
    public String getId()
    {
      return id;
    }
    public String getSpeechUnitId()
    {
      return speechUnitId;
    }
    public String getBMLBlockId()
    {
      return bmlBlockId;
    }


        if (visemeUnits.size() == 0)
        {
            // System.out.println("NO VISEMES");
            return;
        }
        // find last VisemeUnit before t
        // find first visemeunit after t
        // blend the two
        VisemeUnit prevViseme = null;
        VisemeUnit nextViseme = null;
        for (VisemeUnit vu : visemeUnits)
        {
            if ((vu.getTimePeg().getValue() <= t) && prevViseme == null)
                prevViseme = vu;
            if ((vu.getTimePeg().getValue() > t) && (nextViseme == null))
                nextViseme = vu;

            if ((vu.getTimePeg().getValue() <= t)
                    && (vu.getTimePeg().getValue() > prevViseme.getTimePeg().getValue()))
                prevViseme = vu;
            if ((vu.getTimePeg().getValue() > t)
                    && (vu.getTimePeg().getValue() < nextViseme.getTimePeg().getValue()))
                nextViseme = vu;
        }
        double alpha = 0d;
        if (nextViseme == null)
        {
            alpha = 0;
        } else if (prevViseme == null)
        {
            alpha = 1;
        } else if (prevViseme.getTimePeg().getValue() == nextViseme.getTimePeg().getValue())
        {
            alpha = 1d;
        } else
        {
            alpha = (t - prevViseme.getTimePeg().getValue())
                    / (nextViseme.getTimePeg().getValue() - prevViseme.getTimePeg().getValue());
        }

        // remove old morph targets:
        String[] names = new String[morphTargetNames.size()];
        float[] weights = new float[morphTargetNames.size()];
        for (int i = 0; i < morphTargetNames.size(); i++)
        {
            names[i] = morphTargetNames.get(i);
            weights[i] = morphTargetWeights.get(i).floatValue();
        }

        theGLScene.removeMorphTargets(names, weights);

        morphTargetNames.clear();
        morphTargetWeights.clear();

        int vis = 0;
        if (prevViseme != null)
        {
            vis = prevViseme.curViseme;
            if (vis == -1)
                vis = 0;
            if (alpha != 1d && !visemeMappingToArmandia[vis].equals(""))
            {
                morphTargetNames.add(visemeMappingToArmandia[vis]);
                morphTargetWeights.add(new Float(1f - (float) alpha));
            }
        }
        if (nextViseme != null)
        {
            vis = nextViseme.curViseme;
            if (vis == -1)
                vis = 0;
            if (alpha != 1d && !visemeMappingToArmandia[vis].equals(""))
            {
                morphTargetNames.add(visemeMappingToArmandia[vis]);
                morphTargetWeights.add(new Float((float) alpha));
            }
        }

        names = new String[morphTargetNames.size()];
        weights = new float[morphTargetNames.size()];
        for (int i = 0; i < morphTargetNames.size(); i++)
        {
            names[i] = morphTargetNames.get(i);
            weights[i] = morphTargetWeights.get(i).floatValue();
        }

        theGLScene.addMorphTargets(names, weights);

    }
}
