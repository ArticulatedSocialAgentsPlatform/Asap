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
package asap.animationengine.pointing;

import com.google.common.collect.ImmutableSet;

import hmi.animation.AnalyticalIKSolver;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;

/**
 * Motion unit that points at moving targets. 
 * @author Herwin van Welbergen
 */
public class DynamicPointingMU extends PointingMU
{
    private float[]qCurrSh=new float[4];
    private float[]qCurrElb=new float[4];
    
    private VJoint vCurrShoulder;
    private VJoint vCurrElbow;
    
    @Override
    public DynamicPointingMU copy(AnimationPlayer p)
    {
        DynamicPointingMU pmu = new DynamicPointingMU();
        pmu.player = p;        
        pmu.setHand(hand);
        pmu.shoulderId = shoulderId; 
        pmu.elbowId = elbowId;
        pmu.vjShoulder = p.getVNextPartBySid(shoulderId);
        pmu.vjElbow = p.getVNextPartBySid(elbowId);
        pmu.vjWrist = p.getVNextPartBySid(wristId);
        pmu.vCurrShoulder = p.getVCurrPartBySid(shoulderId);
        pmu.vCurrElbow = p.getVCurrPartBySid(elbowId);
        pmu.woManager = p.getWoManager();        
        pmu.target = target;
        return pmu;
    }
    
    public void setHand(String hand)
    {
        super.setHand(hand);
        vCurrShoulder = player.getVCurrPartBySid(shoulderId);
        vCurrElbow = player.getVCurrPartBySid(elbowId);       
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        woTarget.getTranslation(vecTemp, null);
        AnalyticalIKSolver.translateToLocalSystem(null, vjShoulder, vecTemp, vecTemp2);        
        setEndRotation(vecTemp2);
        
        if(t<0.25)
        {
            double remDuration = ( (0.25-t)/0.25)*preparationDuration;
            float deltaT = (float)(player.getStepTime()/remDuration);
            vCurrShoulder.getRotation(qCurrSh);
            Quat4f.interpolate(qTemp, qCurrSh, qShoulder,deltaT);
            vjShoulder.setRotation(qTemp);
            vCurrElbow.getRotation(qCurrElb);
            Quat4f.interpolate(qTemp, qCurrElb, qElbow,deltaT);
            vjElbow.setRotation(qTemp);
        }
        else if(t>0.75)
        {
            relaxUnit.play( (t-0.75)/0.25 );            
        }
        else
        {
            vjShoulder.setRotation(qShoulder);
            vjElbow.setRotation(qElbow);
        }
    }
}
