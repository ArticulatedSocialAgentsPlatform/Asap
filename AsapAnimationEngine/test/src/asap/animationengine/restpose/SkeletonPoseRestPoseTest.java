/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fEquals;
import static org.junit.Assert.assertEquals;
import hmi.animation.Hanim;
import hmi.animation.SkeletonPose;
import hmi.math.Quat4f;

import org.junit.Test;

import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.Sets;

/**
 * Unit tests for the SkeletonPoseRestPose
 * @author hvanwelbergen
 * 
 */
public class SkeletonPoseRestPoseTest extends AbstractRestPoseTest
{
    private PegBoard pegBoard = new PegBoard();

    @Test
    public void testcreateTransitionToRest() throws TimedPlanUnitPlayException
    {
        String[] poseJoints = { Hanim.l_elbow, Hanim.l_wrist };
        float[] rotations = { 0, 0, 0, 1, 0, 1, 0, 0 };
        SkeletonPose pose = new SkeletonPose(poseJoints, rotations, "R");

        RestPose restPose = new SkeletonPoseRestPose(pose);
        restPose.setAnimationPlayer(mockAnimationPlayer);
        TimedAnimationMotionUnit tmu = restPose.createTransitionToRest(NullFeedbackManager.getInstance(),
                Sets.newHashSet("l_shoulder", "l_wrist"), 1, 2, "bml1", "transition1", BMLBlockPeg.GLOBALPEG, pegBoard);
        assertEquals(BMLBlockPeg.GLOBALPEG, tmu.getBMLBlockPeg());
        assertEquals("transition1", tmu.getId());
        assertEquals("bml1", tmu.getBMLId());

        tmu.start(1);
        tmu.play(2.99999);
        float[] r = Quat4f.getQuat4f();
        vNext.getPartBySid("l_elbow").getRotation(r);
        assertQuat4fEquals(1f, 0f, 0f, 0f, r, 0.001f);

        vNext.getPartBySid("l_shoulder").getRotation(r);
        assertQuat4fEquals(1f, 0f, 0f, 0f, r, 0.001f);

        vNext.getPartBySid("l_wrist").getRotation(r);
        assertQuat4fEquals(0f, 1f, 0f, 0f, r, 0.001f);
    }
}
