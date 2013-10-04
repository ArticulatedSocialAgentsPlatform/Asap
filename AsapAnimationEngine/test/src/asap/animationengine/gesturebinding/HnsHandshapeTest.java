package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import hmi.animation.SkeletonPose;

import org.junit.Before;
import org.junit.Test;

import asap.hns.Hns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for HnsHandshape
 * @author hvanwelbergen
 *
 */
public class HnsHandshapeTest
{
    private Hns mockHns = mock(Hns.class);
    private SkeletonPose mockSkeletonPose1 = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose2 = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose3 = mock(SkeletonPose.class);
    
    
    private SkeletonPose mockSkeletonPose1DeepCopy = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose2DeepCopy = mock(SkeletonPose.class);
    private SkeletonPose mockSkeletonPose3DeepCopy = mock(SkeletonPose.class);
    
    @Before
    public void setup()
    {
        when(mockHns.getBasicHandShapes()).thenReturn(ImmutableSet.of("id1","id2","id3"));
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
        HnsHandshape s = new HnsHandshape(mockHns, ImmutableList.of(mockSkeletonPose1, mockSkeletonPose2, mockSkeletonPose3));
        assertEquals(mockSkeletonPose2DeepCopy, s.getHNSHandShape("id2"));
        assertNull(s.getHNSHandShape("unknown"));
    }
    
    @Test
    public void testHandShape()
    {
        HnsHandshape s = new HnsHandshape(mockHns, ImmutableList.of(mockSkeletonPose1, mockSkeletonPose2, mockSkeletonPose3));
        assertEquals(mockSkeletonPose2DeepCopy, s.getHNSHandShape("id2"));        
    }
    
    @Test
    public void testHNSinJar() throws IOException
    {
        HnsHandshape s = new HnsHandshape(mockHns, "hns/handshapes");
        assertNotNull(s.getHNSHandShape("curved"));
    }
    
    @Test
    public void testHNSinResource() throws IOException
    {
        HnsHandshape s = new HnsHandshape(mockHns, "handshapes");
        assertNotNull(s.getHNSHandShape("ASLy"));
    }
}
