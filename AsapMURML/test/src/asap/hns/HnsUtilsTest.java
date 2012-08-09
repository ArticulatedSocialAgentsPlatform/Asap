package asap.hns;

import hmi.math.Vec3f;
import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the HnsUtils
 * @author hvanwelbergen
 *
 */
public class HnsUtilsTest
{
    private float[] direction = Vec3f.getVec3f();
    private static final float PRECISION = 0.001f;
    
    @Test
    public void testGetAbsoluteDirectionDirU()
    {
        assertTrue(HnsUtils.getAbsoluteDirection("DirU", direction));
        assertVec3fEquals(0,1,0, direction, PRECISION);
    }
    
    @Test
    public void testGetAbsoluteDirectionDirUL()
    {
        assertTrue(HnsUtils.getAbsoluteDirection("DirUL", direction));
        float [] dir = Vec3f.getVec3f(1,1,0);
        Vec3f.normalize(dir);
        assertVec3fEquals(dir, direction, PRECISION);
    }
    
    @Test
    public void testGetAbsoluteDirectionVec()
    {
        assertTrue(HnsUtils.getAbsoluteDirection("1 2 3", direction));
        float [] dir = Vec3f.getVec3f(1,2,3);
        Vec3f.normalize(dir);
        assertVec3fEquals(dir, direction, PRECISION);
    }
    
    
}
