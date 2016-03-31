/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;

/**
 * Unit test cases for LinearFloatInterpolator in 3D
 * @author hvanwelbergen
 *
 */
public class LinearFloatInterpolator3DTest
{
    private LinearFloatInterpolator interp3d = new LinearFloatInterpolator();
    private static final double TIME_PRECISION = 0.0001;
    
    @Before
    public void setup()
    {
        KeyFrame kf0 = new KeyFrame(0,new float[]{1,2,3});
        KeyFrame kf1 = new KeyFrame(0.2,new float[]{2,3,4});
        KeyFrame kf2 = new KeyFrame(0.8,new float[]{3,4,5});
        KeyFrame kf3 = new KeyFrame(1,new float[]{4,5,6});
        interp3d.setKeyFrames(ImmutableList.of(kf0,kf1,kf2,kf3), 3);
    }
    
    @Test
    public void testInterpolateToStart()
    {
        KeyFrame kf = interp3d.interpolate(0);
        assertEquals(0, kf.getFrameTime(),TIME_PRECISION);
        assertThat(Floats.asList(kf.getDofs()), contains(1f,2f,3f));
    }
    
    @Test
    public void testInterpolateToEnd()
    {
        KeyFrame kf = interp3d.interpolate(1);
        assertEquals(1, kf.getFrameTime(),TIME_PRECISION);
        assertThat(Floats.asList(kf.getDofs()), contains(4f,5f,6f));
    }
    
    @Test
    public void testInterpolate02()
    {
        KeyFrame kf = interp3d.interpolate(0.2);
        assertEquals(0.2, kf.getFrameTime(),TIME_PRECISION);
        assertThat(Floats.asList(kf.getDofs()), contains(2f,3f,4f));
    }
    
    @Test
    public void testInterpolate01()
    {
        KeyFrame kf = interp3d.interpolate(0.1);
        assertEquals(0.1, kf.getFrameTime(),TIME_PRECISION);
        assertThat(Floats.asList(kf.getDofs()), contains(1.5f,2.5f,3.5f));
    }
}
