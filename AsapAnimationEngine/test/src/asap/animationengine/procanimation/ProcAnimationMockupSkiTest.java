package asap.animationengine.procanimation;

import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import org.powermock.core.classloader.annotations.PrepareForTest;

import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.procanimation.IKBody;
import asap.animationengine.procanimation.Parameter;
import asap.animationengine.procanimation.ProcAnimationMU;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.mockito.Mockito.*;
/**
 * Tests a ProcAnimation acting upon an embedded SkeletonInterpolator. 
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KeyframeMU.class)
public class ProcAnimationMockupSkiTest
{
    private SkeletonInterpolator mockSki = mock(SkeletonInterpolator.class);
    private IKBody ikBody;
    private VJoint human;
    
    @Before
    public void loadDaeHuman() throws IOException
    {
        human = HanimBody.getLOA1HanimBody();
        ikBody = new IKBody(human);
    }
    
    @Test
    public void testSkeletonInterpolator2() throws Exception
    {
        ProcAnimationMU pu = new ProcAnimationMU();
        when(mockSki.getStartTime()).thenReturn(1d);
        when(mockSki.getEndTime()).thenReturn(2d);
        when(mockSki.getPartIds()).thenReturn(new String[0]);
        whenNew(SkeletonInterpolator.class).withArguments(mockSki).thenReturn(mockSki);
        
        pu.addSkeletonInterpolator(mockSki);
        pu.setup(new ArrayList<Parameter>(), ikBody);
        
        pu.play(0);
        verify(mockSki,times(1)).time(1);
    }
    
    @Test
    public void testSkeletonInterpolator() throws Exception
    {
        ProcAnimationMU pu = new ProcAnimationMU();
        when(mockSki.getStartTime()).thenReturn(1d);
        when(mockSki.getEndTime()).thenReturn(2d);
        when(mockSki.getPartIds()).thenReturn(new String[0]);
        whenNew(SkeletonInterpolator.class).withArguments(mockSki).thenReturn(mockSki);
        
        pu.addSkeletonInterpolator(mockSki);
        pu.setup(new ArrayList<Parameter>(), ikBody);
        
        pu.play(1);
        verify(mockSki,times(1)).time(2);
    }

}
