package asap.animationengine.gaze;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * Unit tests for the DynamicRestGaze
 * @author hvanwelbergen
 * 
 */
public class DynamicRestGazeTest
{
    private BMLBlockPeg bbPeg = new BMLBlockPeg("bml1",0);
    private PegBoard pb = new PegBoard();
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class); 
    
    @Before
    public void setup()
    {
        when(mockAnimationPlayer.getVNext()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAnimationPlayer.getVCurr()).thenReturn(HanimBody.getLOA1HanimBody());
    }
    
    @Test
    public void testCreateGazeShiftTMU() throws MUSetupException
    {
        DynamicRestGaze g = new DynamicRestGaze();
        g = g.copy(mockAnimationPlayer);
        assertNotNull(g.createGazeShiftTMU(NullFeedbackManager.getInstance(), bbPeg, "bml1", "gaze1", pb));
    }
}
