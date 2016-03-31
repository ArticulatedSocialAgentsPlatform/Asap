/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
/**
 * Generalized testcases for every RestPose implementation
 * @author hvanwelbergen
 *
 */
public abstract class AbstractRestPoseTest
{
    protected VJoint vNext = HanimBody.getLOA1HanimBody();
    protected VJoint vCurr = HanimBody.getLOA1HanimBody();
    protected AnimationPlayer mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(vCurr,vNext);
}
