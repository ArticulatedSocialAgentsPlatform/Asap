package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;

import asap.animationengine.AnimationPlayer;
import static org.mockito.Mockito.*;
/**
 * Generalized testcases for every RestPose implementation
 * @author hvanwelbergen
 *
 */
public abstract class AbstractRestPoseTest
{
    protected AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    protected VJoint vNext = HanimBody.getLOA1HanimBody();
    protected VJoint vCurr = HanimBody.getLOA1HanimBody();
    
    @Before
    public void setup()
    {
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
    }
}
