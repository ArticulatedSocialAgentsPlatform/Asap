package asap.animationengine;

import java.util.Set;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

/**
 * utility class to determine movement timing 
 * @author hvanwelbergen
 */
public final class MovementTimingUtils
{
    private MovementTimingUtils(){}
    
    /**
     * Gets the duration of limb movement of limb id from its position in vSource to its position in vTarget. 
     * Both positions are relative to the root joint identified by rootId
     * returns -1 if id or rootId are not available in vSource and/or vTarget
     * XXX: setup exceptions for this?
     */
    public static final double getFittsDuration(String id, String rootId, VJoint vSource, VJoint vTarget)
    {
        VJoint srcJoint = vSource.getPartBySid(id);
        VJoint srcRootJoint = vSource.getPartBySid(rootId);
        VJoint targetJoint = vTarget.getPartBySid(id);
        VJoint targetRootJoint = vTarget.getPartBySid(rootId);
        if (srcJoint != null && targetJoint != null && srcRootJoint != null && targetRootJoint != null)
        {
            float[] relPos = Vec3f.getVec3f();
            float[] restPos = Vec3f.getVec3f();
            srcJoint.getPathTranslation(srcRootJoint, relPos);
            targetJoint.getPathTranslation(targetRootJoint, restPos);
            Vec3f.sub(relPos, restPos);
            return FittsLaw.getHandTrajectoryDuration(Vec3f.length(relPos));
        }
        return -1;
    }
    
    /**
     * Get the maximum limb movement duration duration (as determined by Fitts' law) to move the set of joints
     * determined by joints from vSource to vTarget. returns -1 if no wrists are present in vSource and/or vTarget. 
     */
    public static final double getFittsMaximumLimbMovementDuration(VJoint vSource, VJoint vTarget, Set<String> joints)
    {
        double lastSetDur = -1;
        if (joints.contains(Hanim.r_wrist))
        {
            double d = getFittsDuration(Hanim.r_wrist, Hanim.r_shoulder, vSource, vTarget);
            if (d > 0) lastSetDur = d;
        }
        if (joints.contains(Hanim.l_wrist))
        {
            double d = getFittsDuration(Hanim.l_wrist, Hanim.l_shoulder, vSource, vTarget);
            if (d > 0 && d > lastSetDur) lastSetDur = d;
        }        
        if(lastSetDur>0)return lastSetDur;
        return -1;
    }
}
