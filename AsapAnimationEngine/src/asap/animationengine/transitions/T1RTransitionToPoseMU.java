/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.transitions;

import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.AnimationPlayer;

/**
 * Slerp transition + root translation from current pose to predefined end pose, typically for use
 * in an animationplanplayer without using the animationplayer
 * 
 * @author welberge
 */
public class T1RTransitionToPoseMU extends TransitionToPoseMU
{
    public T1RTransitionToPoseMU()
    {
        super();
    }

    public T1RTransitionToPoseMU(List<VJoint> j, List<VJoint> startPoseJoints, float ep[])
    {
        super(j, startPoseJoints, ep);
    }

    @Override
    public T1RTransitionToPoseMU copy(AnimationPlayer player)
    {
        this.aniPlayer = player;
        ArrayList<VJoint> startPoseJoints = new ArrayList<VJoint>();
        float[] ep = null;
        if (endPose != null)
        {
            ep = Arrays.copyOf(endPose, endPose.length);
        }

        if (startJoints == null)
        {
            startPoseJoints.addAll(player.getVCurr().getParts());
        }
        else
        {
            for (VJoint vj : startJoints)
            {
                VJoint vNew = player.getVCurrPartBySid(vj.getSid());
                startPoseJoints.add(vNew);
            }
        }

        if (joints != null)
        {
            ArrayList<VJoint> newJoints = new ArrayList<VJoint>();
            for (VJoint vj : joints)
            {
                VJoint newJ = player.getVCurrPartBySid(vj.getSid());
                if (newJ != null)
                {
                    newJoints.add(newJ);
                }
            }
            return new T1RTransitionToPoseMU(newJoints, startPoseJoints, ep);
        }
        else
        {
            return new T1RTransitionToPoseMU(player.getVCurr().getParts(), startPoseJoints, ep);
        }
    }

    /**
     * Set the current pose of the associated set of joints
     */
    @Override
    public void setStartPose()
    {
        int i = 0;
        startPose = new float[joints.size() * 4 + 3];
        startJoints.get(0).getTranslation(startPose);
        i = 3;
        for (VJoint v : startJoints)
        {
            v.getRotation(startPose, i);
            i += 4;
        }
    }

    @Override
    public void play(double t)
    {
        if (startPose != null)
        {
            Vec3f.interpolate(result, startPose, endPose, (float) t);
            for (int i = 0; i < joints.size(); i++)
            {
                Quat4f.interpolate(result, i * 4 + 3, startPose, i * 4 + 3, endPose, i * 4 + 3, (float) t);
            }

            joints.get(0).setTranslation(result);
            int i = 3;
            for (VJoint vj : joints)
            {
                vj.setRotation(result, i);
                i += 4;
            }
        }
    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
