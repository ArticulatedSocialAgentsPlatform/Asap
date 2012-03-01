package asap.animationengine.restpose;

import static org.junit.Assert.*;
import hmi.animation.Hanim;
import hmi.animation.SkeletonPose;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.math.Quat4f;

import org.junit.Test;

import com.google.common.collect.Sets;

import asap.animationengine.motionunit.TimedMotionUnit;
import static hmi.testutil.math.Quat4fTestUtil.*;
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
        
        SkeletonPoseRestPose restPose = new SkeletonPoseRestPose(pose, mockAnimationPlayer, NullFeedbackManager.getInstance(),pegBoard);
        TimedMotionUnit tmu = restPose.createTransitionToRest(Sets.newHashSet("l_shoulder", "l_wrist"), 1, 2, "bml1", "transition1",
                BMLBlockPeg.GLOBALPEG);
        assertEquals(BMLBlockPeg.GLOBALPEG, tmu.getBMLBlockPeg());
        assertEquals("transition1", tmu.getId());
        assertEquals("bml1", tmu.getBMLId());        
        
        tmu.start(1);
        tmu.play(2.99999);
        float[] r = Quat4f.getQuat4f();
        vNext.getPartBySid("l_elbow").getRotation(r);
        assertQuat4fEquals(1f,0f,0f,0f,r, 0.001f);
        
        vNext.getPartBySid("l_shoulder").getRotation(r);
        assertQuat4fEquals(1f,0f,0f,0f,r, 0.001f);
        
        vNext.getPartBySid("l_wrist").getRotation(r);
        assertQuat4fEquals(0f,1f,0f,0f,r, 0.001f);
    }
}
