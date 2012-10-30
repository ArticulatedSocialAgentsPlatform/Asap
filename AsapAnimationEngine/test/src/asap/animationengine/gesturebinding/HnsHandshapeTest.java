package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    
    @Before
    public void setup()
    {
        when(mockHns.getBasicHandShapes()).thenReturn(ImmutableSet.of("id1","id2","id3"));
        when(mockSkeletonPose1.getId()).thenReturn("id1");
        when(mockSkeletonPose2.getId()).thenReturn("id2");
        when(mockSkeletonPose3.getId()).thenReturn("id3");
    }
    
    @Test
    public void test()
    {
        HnsHandshape s = new HnsHandshape(mockHns, ImmutableList.of(mockSkeletonPose1, mockSkeletonPose2, mockSkeletonPose3));
        assertEquals(mockSkeletonPose2, s.getHNSHandShape("id2"));
        assertNull(s.getHNSHandShape("unknown"));
    }
}
