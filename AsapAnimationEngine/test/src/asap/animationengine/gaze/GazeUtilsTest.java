/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import static org.junit.Assert.assertThat;
import hmi.animation.Hanim;
import hmi.testutil.animation.HanimBody;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit tests for GazeUtils
 * @author hvanwelbergen
 * 
 */
public class GazeUtilsTest
{
    @Test
    public void testGetJointsEyes()
    {
        assertThat(GazeUtils.getJoints(HanimBody.getLOA2HanimBodyWithEyes(), GazeInfluence.EYES),
                IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_eyeball_joint, Hanim.r_eyeball_joint));
    }

    @Test
    public void testGetJointsShoulder()
    {
        assertThat(GazeUtils.getJoints(HanimBody.getLOA2HanimBodyWithEyes(), GazeInfluence.SHOULDER),
                IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.vc2, Hanim.vt6, Hanim.vt1, Hanim.vt10, Hanim.vc4, Hanim.skullbase,
                        Hanim.l_eyeball_joint, Hanim.r_eyeball_joint));
    }
}
