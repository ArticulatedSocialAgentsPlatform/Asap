/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.keyframe.KeyframeMU;
/**
 * Tests a ProcAnimation acting upon an embedded SkeletonInterpolator. 
 * @author Herwin
 *
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
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
