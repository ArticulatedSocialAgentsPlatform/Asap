package asap.motionunit.keyframe;

import static org.junit.Assert.assertEquals;
import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the LinearQuatFloatInterpolator
 * @author hvanwelbergen
 *
 */
public class LinearQuatFloatInterpolatorTest
{
    private static final float INTERPOLATION_PRECISION = 0.0001f;
    private LinearQuatFloatInterpolator interp = new LinearQuatFloatInterpolator();

    @Before
    public void setup()
    {
        KeyFrame kf0 = new KeyFrame(0, new float[] { 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 });
        KeyFrame kf1 = new KeyFrame(0.2, new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 });
        KeyFrame kf2 = new KeyFrame(0.8, new float[] { 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0 });
        KeyFrame kf3 = new KeyFrame(1, new float[] { 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0 });
        interp.setKeyFrames(ImmutableList.of(kf0, kf1, kf2, kf3), 3*4);
    }

    @Test
    public void testInterpolateToStart()
    {
        KeyFrame kf = interp.interpolate(0);
        assertEquals(0, kf.getFrameTime(), 0.0001);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolateToEnd()
    {
        KeyFrame kf = interp.interpolate(1);
        assertEquals(1, kf.getFrameTime(), 0.0001);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 0, 1), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 1, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolate02()
    {
        KeyFrame kf = interp.interpolate(0.2);
        assertEquals(0.2, kf.getFrameTime(), 0.0001);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(1, 0, 0, 0), 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 1, 0, 0), 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4f(0, 0, 1, 0), 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }

    @Test
    public void testInterpolate01()
    {
        KeyFrame kf = interp.interpolate(0.1);
        assertEquals(0.1, kf.getFrameTime(), 0.0001);
        float q0[] = Quat4f.getQuat4f();
        float q1[] = Quat4f.getQuat4f();
        float q2[] = Quat4f.getQuat4f();
        Quat4f.interpolate(q0, Quat4f.getQuat4f(0, 1, 0, 0), Quat4f.getQuat4f(1, 0, 0, 0), 0.5f);
        Quat4f.interpolate(q1, Quat4f.getQuat4f(1, 0, 0, 0), Quat4f.getQuat4f(0, 1, 0, 0), 0.5f);
        Quat4f.interpolate(q2, Quat4f.getQuat4f(0, 0, 0, 1), Quat4f.getQuat4f(0, 0, 1, 0), 0.5f);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(q0, 0, kf.getDofs(), 0, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(q1, 0, kf.getDofs(), 4, INTERPOLATION_PRECISION);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(q2, 0, kf.getDofs(), 8, INTERPOLATION_PRECISION);
    }
}
