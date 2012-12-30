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
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;
import asap.realizer.planunit.KeyPosition;
import asap.timemanipulator.SigmoidManipulator;

/**
 * Very simple dynamic gaze tracker, does not use any prediction. Interpolates
 * between current neck pose and desired (moving) neck pose with interpolation
 * weight timestep/remainingduration when t &lt; 0.25 Perfectly tracks target
 * when 0.25 &lt; t &lt; 0.75
 * no eye rotation
 * 
 * @author welberge
 */
public class DynamicGazeMU extends GazeMU
{
    private float targetPosCurr[] = new float[3];
    private float qCurr[] = new float[4];
    private float qEndCurr[] = new float[4];
    private VJoint neckCurr;
    
    public DynamicGazeMU()
    {
        qGaze = new float[4];
        qTemp = new float[4];        

        vecTemp = new float[3];
        ready = new KeyPosition("ready", 0.25, 1);
        relax = new KeyPosition("relax", 0.75, 1);
        addKeyPosition(ready);
        addKeyPosition(relax);
        target = "";
        // defaults from presenter
        tmp = new SigmoidManipulator(5, 1);
    }

    @Override
    public DynamicGazeMU copy(AnimationPlayer p)
    {
        DynamicGazeMU gmu = new DynamicGazeMU();
        gmu.neck = p.getVNext().getPart(Hanim.skullbase);
        gmu.neckCurr = p.getVCurr().getPart(Hanim.skullbase);
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        gmu.player = p;
        return gmu;
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        woTarget.getTranslation2(targetPosCurr, neck);
        Quat4f.transformVec3f(getOffsetRotation(), targetPosCurr);

        if (t < RELATIVE_READY_TIME)
        {
            double remDuration = ((RELATIVE_READY_TIME - t) / RELATIVE_READY_TIME) * preparationDuration;
            setEndRotation(targetPosCurr, qEndCurr);
            neckCurr.getRotation(qCurr);
            float deltaT = (float) (player.getStepTime() / remDuration);
            Quat4f.interpolate(qGaze, qCurr, qEndCurr, deltaT);
            neck.setRotation(qGaze);
        }
        else if (t > RELATIVE_RELAX_TIME)
        {
            relaxUnit.play( (t-RELATIVE_RELAX_TIME)/(1-RELATIVE_RELAX_TIME));
        }
        else
        {
            setEndRotation(targetPosCurr);
            neck.setRotation(qGaze);
        }
    }
}
