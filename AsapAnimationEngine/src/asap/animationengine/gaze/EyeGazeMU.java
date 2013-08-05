package asap.animationengine.gaze;
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
import hmi.animation.Hanim;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.neurophysics.Saccade;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

import com.google.common.collect.ImmutableSet;

/**
 * Constant velocity saccade to target.
 * Gaze is on target at ready, moves back to rest position at relax.
 * @author Herwin van Welbergen
 */
public class EyeGazeMU extends GazeMU
{
    private float qEyeLeft[] = Quat4f.getQuat4f();
    private float qEyeRight[] = Quat4f.getQuat4f();
    
    @Override    
    public EyeGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        EyeGazeMU gmu = new EyeGazeMU();
        gmu.lEye = p.getVNextPartBySid(Hanim.l_eyeball_joint);
        gmu.rEye = p.getVNextPartBySid(Hanim.r_eyeball_joint);
        if(gmu.lEye == null || gmu.rEye==null)
        {
            throw new MUSetupException("Eyegaze MU requested, but no eyeball joint in skeleton.",this);
        }
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        gmu.offsetAngle = offsetAngle;
        gmu.offsetDirection = offsetDirection;
        return gmu;
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg,String bmlId, String id, PegBoard pb)
    {
        return new GazeTMU(bfm,bmlBlockPeg,bmlId, id, this,pb);
    }
    
    @Override
    void setEndRotation(float[] gazeDir)throws MUPlayException
    {
        woTarget = woManager.getWorldObject(target);
        if (woTarget == null)
        {
            throw new MUPlayException("Gaze target not found", this);
        }        
        woTarget.getTranslation2(gazeDir, rEye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);
        Vec3f.normalize(gazeDir);
        float q[]=Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, q);
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEyeRight);
        
        woTarget.getTranslation2(gazeDir, lEye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);
        Vec3f.normalize(gazeDir);        
        ListingsLaw.listingsEye(gazeDir, q);
        
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEyeLeft);
    }
    
    public double getPreferedReadyDuration()
    {
        float q[]=Quat4f.getQuat4f();
        Quat4f.mulConjugateRight(q, qStartLeftEye, qEyeLeft);
        float angle = Quat4f.getAngle(q);
        if(angle<0)angle = -angle;
        if(angle>Math.PI) angle-=Math.PI;
        return Saccade.getSaccadeDuration(angle);
    }
    
    
    
    @Override
    public void play(double t) throws MUPlayException
    {
        setEndEyeRotation(lEye, qEyeLeft);
        setEndEyeRotation(rEye, qEyeRight);
        
        float qLeft[]=Quat4f.getQuat4f();
        float qRight[]=Quat4f.getQuat4f();
        
        
        if (t < RELATIVE_READY_TIME)
        {
            float relT = (float)t/(float)RELATIVE_READY_TIME;
            Quat4f.interpolate(qLeft, qStartLeftEye, qEyeLeft, relT);            
            Quat4f.interpolate(qRight, qStartRightEye, qEyeRight, relT);
        }
        else if(t>RELATIVE_RELAX_TIME)
        {
            relaxUnit.play((t - RELATIVE_RELAX_TIME) / (1 - RELATIVE_RELAX_TIME));
            return;
        }
        else
        {
            Quat4f.set(qLeft,qEyeLeft);
            Quat4f.set(qRight,qEyeRight);
        }
        rEye.setRotation(qRight);        
        lEye.setRotation(qLeft);                
    }        
    
    private static final Set<String>KINJOINTS = ImmutableSet.of(Hanim.l_eyeball_joint, Hanim.r_eyeball_joint);    
    
    @Override
    public Set<String> getKinematicJoints()
    {
        return KINJOINTS;        
    }  
}
