/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;

/**
 * Unit tests for the DynamicGazeMU
 * @author hvanwelbergen
 *
 */
public class DynamicGazeMUTest
{
    AnimationPlayer mockPlayer = mock(AnimationPlayer.class);
    
    @Before
    public void setup()
    {
        when(mockPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
    }
    
    @Test
    public void testCopy() throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.setInfluence(GazeInfluence.WAIST);
        DynamicGazeMU muCopy = mu.copy(mockPlayer);
        assertEquals(muCopy.influence, GazeInfluence.WAIST);
    }
}
