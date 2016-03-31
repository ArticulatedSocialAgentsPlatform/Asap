/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import static org.junit.Assert.assertEquals;
import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Before;
import org.junit.Test;

import asap.math.QuatInterpolator;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the LinearQuatFloatInterpolator
 * @author hvanwelbergen
 *
 */
public abstract class AbstractQuatFloatInterpolatorTest<E extends QuatInterpolator>
{
    protected static final float INTERPOLATION_PRECISION = 0.0001f;
    protected static final float FRAMETIME_PRECISION = 0.0001f;
    

    abstract protected QuatFloatInterpolator<E>getInterpolator();
    protected QuatFloatInterpolator<E> interp;
    @Before
    public void setup()
    {
        KeyFrame kf0 = new KeyFrame(0, new float[] { 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 });
        KeyFrame kf1 = new KeyFrame(0.2, new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 });
        KeyFrame kf2 = new KeyFrame(0.8, new float[] { 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0 });
        KeyFrame kf3 = new KeyFrame(1, new float[] { 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0 });
        interp = getInterpolator();
        interp.setKeyFrames(ImmutableList.of(kf0, kf1, kf2, kf3), 3*4);
    }

    @Test
    public void testInterpolateToStart()
    {
        KeyFrame kf = interp.interpolate(0);
        assertEquals(0, kf.getFrameTime(), FRAMETIME_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolateToEnd()
    {
        KeyFrame kf = interp.interpolate(1);
        assertEquals(1, kf.getFrameTime(), FRAMETIME_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 1, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolate02()
    {
        KeyFrame kf = interp.interpolate(0.2);
        assertEquals(0.2, kf.getFrameTime(), FRAMETIME_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 1, 0), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolate08()
    {
        KeyFrame kf = interp.interpolate(0.8);
        assertEquals(0.8, kf.getFrameTime(), FRAMETIME_PRECISION);        
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 1, 0), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }
}
