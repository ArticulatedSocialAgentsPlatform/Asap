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
package asap.animationengine.transitions;

import java.util.ArrayList;
import java.util.Collection;

import asap.animationengine.AnimationPlayer;

import hmi.animation.VJoint;
import hmi.math.Quat4f;

/**
 * Slerp transition from current pose to predefined end pose, typically for use
 * in an animationplanplayer without using the animationplayer
 * 
 * @author welberge
 */
public class SlerpTransitionToPoseMU extends TransitionMU
{
    private Collection<VJoint> startJoints;
    private float endPose[];
    private float startPose[] = null;
    private float qResult[];

    public SlerpTransitionToPoseMU()
    {
        super();
    }

    /**
     * Constructor
     * 
     * @param j
     *            joints to slerp (usually taken from animationPlayer.getVNext()
     * @param startPoseJoints
     *            joints to take start pose from (usually taken from the final
     *            VJoint human on which animation and physics are combined)
     * @param ep
     *            end pose, contains quaternion joint rotations for each joint
     */
    public SlerpTransitionToPoseMU(Collection<VJoint> j,
            Collection<VJoint> startPoseJoints, float ep[])
    {
        super();
        joints = j;
        startJoints = startPoseJoints;
        endPose = ep;
        qResult = new float[joints.size() * 4];
    }

    @Override
    public TransitionMU copy(AnimationPlayer player)
    {
        ArrayList<VJoint> startPoseJoints = new ArrayList<VJoint>();
        float[] ep = null;
        if (endPose != null)
        {
            ep = new float[endPose.length];
            for (int i = 0; i < endPose.length; i++)
            {
                ep[i] = endPose[i];
            }
        }

        if (startJoints == null)
        {
            startPoseJoints.addAll(player.getVNext().getParts());
        }
        else
        {
            for (VJoint vj : startJoints)
            {
                VJoint vNew = player.getVNext().getPart(vj.getSid());
                startPoseJoints.add(vNew);
            }
        }

        if (joints != null)
        {
            ArrayList<VJoint> newJoints = new ArrayList<VJoint>();
            for (VJoint vj : joints)
            {
                VJoint newJ = player.getVNext().getPart(vj.getSid());
                if (newJ != null)
                {
                    joints.add(newJ);
                }
            }
            return new SlerpTransitionToPoseMU(newJoints, startPoseJoints, ep);
        }
        else
        {
            return new SlerpTransitionToPoseMU(player.getVNext().getParts(),
                    startPoseJoints, ep);
        }
    }

    /**
     * Set the start pose
     * 
     * @param sp
     *            the new start pose
     */
    public void setStartPose(float sp[])
    {
        startPose = sp;
    }

    /**
     * Set the current pose of the associated set of joints
     */
    @Override
    public void setStartPose()
    {
        // System.out.println("Starting Starting Starting Starting Starting  ");
        int i = 0;
        startPose = new float[joints.size() * 4];
        for (VJoint v : startJoints)
        {
            v.getRotation(startPose, i);
            i += 4;
        }
    }

    @Override
    public void play(double t)
    {
        // System.out.println("Slerping "+t);
        if (startPose != null)
        {
            Quat4f.interpolateArrays(qResult, startPose, endPose, (float) t);
            int i = 0;
            for (VJoint vj : joints)
            {
                vj.setRotation(qResult, i);
                i += 4;
            }
        }
    }

    @Override
    public void setEndPose(double endTime, double duration)
    {

    }
}
