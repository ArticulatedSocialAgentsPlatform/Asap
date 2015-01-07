/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import static org.junit.Assert.assertNotNull;
import hmi.testutil.animation.HanimBody;

import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;

/**
 * Unit testcases for EyeGazeMU
 * @author Herwin
 */
public class EyeGazeMUTest
{
    private AnimationPlayer mockAnimationPlayer;

    @Test(expected = MUSetupException.class)
    public void testNoEyes() throws MUPlayException, MUSetupException
    {
        mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody());
        EyeGazeMU mu = new EyeGazeMU();
        mu.copy(mockAnimationPlayer);
    }

    @Test
    public void testCopy() throws MUSetupException
    {
        mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(HanimBody.getLOA1HanimBodyWithEyes(),
                HanimBody.getLOA1HanimBodyWithEyes());
        EyeGazeMU mu = new EyeGazeMU();
        assertNotNull(mu.copy(mockAnimationPlayer));
    }
}
