/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Utils for gaze motionunits
 * @author herwinvw
 *
 */
public final class GazeUtils
{
    private GazeUtils()
    {
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
            value={"SF_SWITCH_FALLTHROUGH","SF_SWITCH_NO_DEFAULT"}, 
            justification="Fallthrough by design to add joints in order.")
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
        case EYES:            
        default:
            joints.addAll(VJointUtils.gatherJointSids(new String[] { Hanim.r_eyeball_joint, Hanim.l_eyeball_joint }, root));
        }
        return ImmutableSet.copyOf(joints);
    }
}
