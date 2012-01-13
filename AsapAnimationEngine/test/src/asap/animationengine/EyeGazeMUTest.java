package asap.animationengine;

import static org.junit.Assert.*;
import hmi.animation.VJoint;
import static org.mockito.Mockito.*;

import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.gaze.EyeGazeMU;

/**
 * EyeGazeMU unit test cases
 * @author welberge
 */
public class EyeGazeMUTest
{
    AnimationPlayer mockPlayer = mock(AnimationPlayer.class);
    
    @Test
    public void testCopy()
    {
        EyeGazeMU mu = new EyeGazeMU();
        when(mockPlayer.getVNext()).thenReturn(new VJoint());
        EyeGazeMU muCopy = mu.copy(mockPlayer);
        assertNotNull(muCopy);
    }
}
