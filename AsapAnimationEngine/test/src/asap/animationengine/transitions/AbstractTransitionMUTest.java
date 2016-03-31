/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.transitions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.realizer.planunit.ParameterNotFoundException;
/**
 * generic TransitionMU testcases
 * @author welberge
 */
public abstract class AbstractTransitionMUTest
{
    protected AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class); 
    protected VJoint human = HanimBody.getLOA1HanimBody();
    protected VJoint humanCopy = HanimBody.getLOA1HanimBody();
    
    protected abstract TransitionMU createTransistionMU();
    protected abstract TransitionMU createTransistionMUConnectedToPlayer();
    @Before
    public void setup()
    {
        when(mockAnimationPlayer.getVNext()).thenReturn(humanCopy);
    }
    
    @Test
    public void testSetJoints() throws ParameterNotFoundException
    {
        TransitionMU mu = createTransistionMUConnectedToPlayer();
        mu.setParameterValue("joints", Hanim.HumanoidRoot+" "+Hanim.r_shoulder);
        assertEquals(Hanim.HumanoidRoot+" "+Hanim.r_shoulder, mu.getParameterValue("joints"));
    }
    
    @Test
    public void testCopy() throws ParameterNotFoundException
    {
        TransitionMU mu = createTransistionMU();
        TransitionMU muCopy = mu.copy(mockAnimationPlayer);
        assertEquals(human.getParts().size(),muCopy.getParameterValue("joints").split(" ").length);
    }
}
