/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import hmi.animation.SkeletonPose;
import lombok.Data;

/**
 * Describes a posture configuration
 * @author hvanwelbergen
 *
 */
@Data
public class PostureConstraint
{
    private final String id;
    private final SkeletonPose posture;
}
