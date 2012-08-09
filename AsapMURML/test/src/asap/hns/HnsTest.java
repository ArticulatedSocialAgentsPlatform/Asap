package asap.hns;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertTrue;
import hmi.math.Vec3f;

import org.junit.Test;

public class HnsTest
{
    private float[] location = Vec3f.getVec3f();
    private Hns hns = new Hns();
    private static final float PRECISION = 0.001f;
    
    @Test
    public void testGetHandLocationVec()
    {
        assertTrue(hns.getHandLocation("1 2 3", location));
        float [] loc = Vec3f.getVec3f(1,2,3);
        assertVec3fEquals(loc, location, PRECISION);
    }
}
