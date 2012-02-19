package asap.animationengine.gaze;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import static org.junit.Assert.*;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MUSetupException;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

/**
 * Unit testcases for EyeGazeMU
 * @author Herwin
 */
public class EyeGazeMUTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    
    @Test(expected=MUSetupException.class)
    public void testNoEyes() throws MUPlayException, MUSetupException
    {
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        
        EyeGazeMU mu = new EyeGazeMU();
        mu.copy(mockAnimationPlayer);        
    }
    
    @Test
    public void testCopy() throws MUSetupException
    {
        VJoint vCurr = HanimBody.getLOA1HanimBodyWithEyes();
        VJoint vNext = HanimBody.getLOA1HanimBodyWithEyes();
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        
        EyeGazeMU mu = new EyeGazeMU();
        assertNotNull(mu.copy(mockAnimationPlayer));
    }
}
