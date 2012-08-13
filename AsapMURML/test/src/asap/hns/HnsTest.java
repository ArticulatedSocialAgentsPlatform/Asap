package asap.hns;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import hmi.math.Vec3f;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Hns
 * @author hvanwelbergen
 *
 */
public class HnsTest
{
    private float[] location = Vec3f.getVec3f();
    private Hns hns = new Hns();
    private static final float PRECISION = 0.001f;

    // @formatter:off
    private String hnsXML=
    "<hns>"+
      "<symbols>"+
        "<symbol class=\"handReferences\" name=\"LocAboveHead\" value=\"60\"/>"+
        "<symbol class=\"handReferences\" name=\"LocHead\" value=\"54\"/>" +
        
        "<symbol class=\"offset\" name=\"ellipticDistance\" value=\"100\"/>"+
        "<symbol class=\"palmOrientations\" name=\"PalmL\" value=\"180.0\"/>"+
        "<symbol class=\"palmOrientations\" name=\"PalmLU\" value=\"-135.0\"/>"+
      "</symbols>"+
    "</hns>";
    // @formatter:on           

    @Before
    public void setup()
    {
        hns.readXML(hnsXML);
    }

    @Test
    public void testGetHandLocationVec()
    {
        assertTrue(hns.getHandLocation("1 2 3", location));
        float[] loc = Vec3f.getVec3f(1, 2, 3);
        assertVec3fEquals(loc, location, PRECISION);
    }

    @Test
    public void testGetHandLocation()
    {
        assertTrue(hns.getHandLocation("LocAboveHead", location));
        assertVec3fEquals(0,0,60,location,PRECISION);
    }
    
    @Test
    public void testGetPalmOrientationDouble()
    {
        assertEquals(10d,hns.getPalmOrientation("10","left_arm"),PRECISION);
    }
    
    @Test
    public void testGetPalmOrientation()
    {
        assertEquals(-135.0d+180,hns.getPalmOrientation("PalmLU","right_arm"),PRECISION);
    }
}
