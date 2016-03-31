/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.SkeletonPose;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for HnsHandshape
 * @author hvanwelbergen
 *
 */
public class HnsHandshapeTest
{
    private SkeletonPose mockSkeletonPose1 = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose2 = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose3 = mock(SkeletonPose.class);
    
    
    private SkeletonPose mockSkeletonPose1DeepCopy = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose2DeepCopy = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose3DeepCopy = mock(SkeletonPose.class);
    
    @Before
    public void setup()
    {
        when(mockSkeletonPose1.getId()).thenReturn("id1");
        when(mockSkeletonPose2.getId()).thenReturn("id2");
        when(mockSkeletonPose3.getId()).thenReturn("id3");
        
        when(mockSkeletonPose1.untargettedDeepCopy()).thenReturn(mockSkeletonPose1DeepCopy);
        when(mockSkeletonPose2.untargettedDeepCopy()).thenReturn(mockSkeletonPose2DeepCopy);
        when(mockSkeletonPose3.untargettedDeepCopy()).thenReturn(mockSkeletonPose3DeepCopy);
    }
    
    @Test
    public void testUnknownHNS()
    {
        HnsHandshape s = new HnsHandshape(ImmutableList.of(mockSkeletonPose1, mockSkeletonPose2, mockSkeletonPose3));
        assertEquals(mockSkeletonPose2DeepCopy, s.getHNSHandShape("id2"));
        assertNull(s.getHNSHandShape("unknown"));
    }
    
    @Test
    public void testHandShape()
    {
        HnsHandshape s = new HnsHandshape(ImmutableList.of(mockSkeletonPose1, mockSkeletonPose2, mockSkeletonPose3));
        assertEquals(mockSkeletonPose2DeepCopy, s.getHNSHandShape("id2"));        
    }
    
    @Test
    public void testHNSinJar() throws IOException
    {
        HnsHandshape s = new HnsHandshape("hns/handshapes");
        assertNotNull(s.getHNSHandShape("curved"));
    }
    
    @Test
    public void testHNSinResource() throws IOException
    {
        HnsHandshape s = new HnsHandshape("handshapes");
        assertNotNull(s.getHNSHandShape("ASLy"));
    }
}
