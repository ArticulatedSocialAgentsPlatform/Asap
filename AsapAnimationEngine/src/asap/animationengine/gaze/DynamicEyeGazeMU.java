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
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;

import com.google.common.collect.ImmutableSet;

/**
 * Makes the eyes dynamically track a target.
 * Very simple dynamic gaze tracker, does not use any prediction. Interpolates
 * between current neck pose and desired (moving) neck pose with interpolation
 * weight timestep/remainingduration when t &lt; 0.25 Perfectly tracks target
 * when 0.25 &lt; t &lt; 0.75
 * 
 * @author Herwin
 */
public class DynamicEyeGazeMU extends DynamicGazeMU
{
    @Override
    public DynamicEyeGazeMU copy(AnimationPlayer p)
    {
        DynamicEyeGazeMU gmu = new DynamicEyeGazeMU();
        gmu.lEye = p.getVNext().getPart(Hanim.l_eyeball_joint);
        gmu.rEye = p.getVNext().getPart(Hanim.r_eyeball_joint);
        gmu.lEyeCurr = p.getVCurr().getPart(Hanim.l_eyeball_joint);
        gmu.rEyeCurr = p.getVCurr().getPart(Hanim.r_eyeball_joint);
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        gmu.offsetAngle = offsetAngle;
        gmu.offsetDirection = offsetDirection;
        return gmu;
    }
    
    private void setEndRotation(float[] gazeDir, VJoint eye, float qEye[])throws MUPlayException
    {
        woTarget = woManager.getWorldObject(target);
        if (woTarget == null)
        {
            throw new MUPlayException("Gaze target not found", this);
        }         
        woTarget.getTranslation2(gazeDir, eye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);        
        Vec3f.normalize(gazeDir);
        float q[]=Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, q);
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEye);
    }
    
    @Override
    public void play(double t)throws MUPlayException
    {
        float targetPosCurrL[]=Vec3f.getVec3f();
        float targetPosCurrR[]=Vec3f.getVec3f();
        woTarget.getTranslation2(targetPosCurrL, lEye);
        woTarget.getTranslation2(targetPosCurrR, rEye);
        float qGazeL[] = Quat4f.getQuat4f();
        float qGazeR[] = Quat4f.getQuat4f();
        float qEndCurr[] = Quat4f.getQuat4f();
        float qCurr[] = Quat4f.getQuat4f();
        if (t < RELATIVE_READY_TIME)
        {
            double remDuration = ((RELATIVE_READY_TIME - t) / RELATIVE_READY_TIME) * preparationDuration;
            float deltaT = (float) (player.getStepTime() / remDuration);
            
            setEndRotation(targetPosCurrL, lEye, qEndCurr);
            lEyeCurr.getRotation(qCurr);
            Quat4f.interpolate(qGazeL, qCurr, qEndCurr, deltaT);
            
            setEndRotation(targetPosCurrR, rEye, qEndCurr);
            rEyeCurr.getRotation(qCurr);
            Quat4f.interpolate(qGazeR, qCurr, qEndCurr, deltaT);
        } 
        else if (t > RELATIVE_RELAX_TIME)
        {
            float tManip = (float) tmp.manip((t - RELATIVE_RELAX_TIME) / (1-RELATIVE_RELAX_TIME));
            
            lEyeCurr.getRotation(qCurr);
            Quat4f.interpolate(qGazeL, qCurr, qStartLeftEye, tManip);
            
            rEyeCurr.getRotation(qCurr);
            Quat4f.interpolate(qGazeR, qCurr, qStartRightEye, tManip);            
        } 
        else
        {
            setEndRotation(targetPosCurrL, lEye, qGazeL);
            setEndRotation(targetPosCurrR, rEye, qGazeR);                        
        }
        lEye.setRotation(qGazeL);
        rEye.setRotation(qGazeR);
    }
    
    private static final Set<String>KINJOINTS = ImmutableSet.of(Hanim.l_eyeball_joint, Hanim.r_eyeball_joint);   
    
    @Override
    public Set<String> getKinematicJoints()
    {
        return KINJOINTS;        
    } 
}
