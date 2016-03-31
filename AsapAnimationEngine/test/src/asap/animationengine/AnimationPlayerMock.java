/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Provides a mockup for the AnimationPlayer that uses a 'real' set of joints for VNext and VCurr
 * @author hvanwelbergen
 *
 */
public final class AnimationPlayerMock
{
    private AnimationPlayerMock(){}
    
    private static final class PartBySidAnswer implements Answer<VJoint>
    {
        private final VJoint joint;
        
        public PartBySidAnswer(VJoint vj)
        {
            this.joint = vj;
        }
        @Override
        public VJoint answer(InvocationOnMock invocation) throws Throwable
        {
            String sid = (String)invocation.getArguments()[0];
            return joint.getPartBySid(sid);
        }        
    }
    
    public static AnimationPlayer createAnimationPlayerMock(VJoint vCurr, VJoint vNext)
    {
        AnimationPlayer mockAniplayer = mock(AnimationPlayer.class);
        when(mockAniplayer.getVCurr()).thenReturn(vCurr);
        when(mockAniplayer.getVNext()).thenReturn(vNext);
        when(mockAniplayer.getVCurrPartBySid(anyString())).thenAnswer(new PartBySidAnswer(vCurr));
        when(mockAniplayer.getVNextPartBySid(anyString())).thenAnswer(new PartBySidAnswer(vNext));
        return mockAniplayer;
    }
    
    public static AnimationPlayer createAnimationPlayerMock()
    {
        return createAnimationPlayerMock(HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody());
    }
}
