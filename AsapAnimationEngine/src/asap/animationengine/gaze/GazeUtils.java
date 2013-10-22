package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class GazeUtils
{
    private GazeUtils()
    {
    }

    public static ImmutableSet<String> getJoints(VJoint root, GazeInfluence influence)
    {
        Set<String> joints = new HashSet<String>();
        switch (influence)
        {
        case WAIST:
            joints.addAll(VJointUtils.gatherJointSids(Hanim.LUMBAR_JOINTS, root));
        case SHOULDER:
            joints.addAll(VJointUtils.gatherJointSids(Hanim.THORACIC_JOINTS, root));
        case NECK:
            joints.addAll(VJointUtils.gatherJointSids(Hanim.CERVICAL_JOINTS, root));
        default:
        case EYES:
            joints.addAll(VJointUtils.gatherJointSids(new String[] { Hanim.r_eyeball_joint, Hanim.l_eyeball_joint }, root));
        }
        return ImmutableSet.copyOf(joints);
    }
}
