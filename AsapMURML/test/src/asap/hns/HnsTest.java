/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hmi.math.Vec3f;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
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
    private float[] direction = Vec3f.getVec3f();

    // @formatter:off
    private String hnsXML=
    "<hns>"+
      "<basejoint sid=\"vl5\"/>"+
      "<defaultPointingHandshape name=\"pointing\"/>"+
      "<symbols>"+
        "<symbol class=\"handReferences\" name=\"LocAboveHead\" value=\"60\"/>"+
        "<symbol class=\"handReferences\" name=\"LocHead\" value=\"54\"/>" +
        "<symbol class=\"handLocators\" name=\"LocCenterLeft\" value=\"25\"/>"+
        "<symbol class=\"handDistances\" name=\"LocFar\" value=\"25\"/>"+
        
        "<symbol class=\"offset\" name=\"ellipticDistance\" value=\"100\"/>"+
        "<symbol class=\"palmOrientations\" name=\"PalmL\" value=\"180.0\"/>"+
        "<symbol class=\"palmOrientations\" name=\"PalmLU\" value=\"-135.0\"/>"+
        
        "<symbol class=\"distances\" name=\"DistNorm\" value=\"20\"/>"+
        
        "<symbol class=\"basicHandShapes\" name=\"BSfist\" value=\"\"/>"+
        "<symbol class=\"basicHandShapes\" name=\"BSflat\" value=\"\"/>"+
        
        "<symbol class=\"specificHandShapes\" name=\"BSifinger\"  value=\"\"/>"+ 
        "<symbol class=\"specificHandShapes\" name=\"BSimcfinger\" value=\"\"/>"+
        
        "<symbol class=\"handShapes\" name=\"ASLy\" value=\"\"/>"+ 
        "<symbol class=\"handShapes\" name=\"ASLgrasp\" value=\"\"/>"+       
      "</symbols>"+
    "</hns>";
    // @formatter:on           

    @Before
    public void setup()
    {
        hns.readXML(hnsXML);
    }
    
    @Test
    public void getBaseJoint()
    {
        assertEquals("vl5", hns.getBaseJoint());
    }
    
    @Test
    public void getDefaultPointingHandshape()
    {
    	assertEquals("pointing",hns.getDefaultPointingHandshape());
    }
    
    @Test
    public void testGetSpecificHandShapes()
    {
        assertThat(hns.getSpecificHandShapes(),IsIterableContainingInAnyOrder.containsInAnyOrder("BSifinger","BSimcfinger"));
    }
    
    @Test
    public void testGetBasicHandShapes()
    {
        assertThat(hns.getBasicHandShapes(),IsIterableContainingInAnyOrder.containsInAnyOrder("BSfist","BSflat"));
    }
    
    @Test
    public void getHandShapes()
    {
        assertThat(hns.getHandShapes(),IsIterableContainingInAnyOrder.containsInAnyOrder("ASLy","ASLgrasp"));
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
        assertTrue(hns.getHandLocation("LocAboveHead dummy dummy", location));
        assertVec3fEquals(0,60,0,location,PRECISION);
    }
    
    @Test
    public void testGetHandLocation2()
    {
        assertTrue(hns.getHandLocation("LocAboveHead LocCenterLeft LocFar", location));
        //values from C++ version
        assertVec3fEquals(11.6322f,60f,24.9453f,location,PRECISION);
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
    
    @Test
    public void testGetDistance()
    {
        assertEquals(20, hns.getDistance("DistNorm"), PRECISION);
    }
    
    @Test
    public void testGetElementExtentVal()
    {
        assertEquals(0.1, hns.getElementExtent("0.1"),PRECISION);
    }
    
    @Test
    public void testGetElementExtentStr()
    {
        assertEquals(0.25, hns.getElementExtent("Flat"),PRECISION);
    }
    
    @Test
    public void testGetElementRoundnessVal()
    {
        assertEquals(0.1, hns.getElementRoundness("0.1"),PRECISION);
    }
    
    @Test
    public void testGetElementRoundnessStr()
    {
        assertEquals(0.9, hns.getElementRoundness("Sharp"),PRECISION);
    }
    
    @Test
    public void testGetElementSkewdnessVal()
    {
        assertEquals(0.1, hns.getElementSkewedness("0.1"),PRECISION);
    }
    
    @Test
    public void testGetElementSkewdnessStr()
    {
        assertEquals(-0.8, hns.getElementSkewedness("FunnelS"),PRECISION);
    }
    
    @Test
    public void testGetAbsoluteDirectionDirU()
    {
        assertTrue(hns.getAbsoluteDirection("DirU", direction));
        assertVec3fEquals(0,1,0, direction, PRECISION);
    }
    
    @Test
    public void testGetAbsoluteDirectionDirUL()
    {
        assertTrue(hns.getAbsoluteDirection("DirUL", direction));
        float [] dir = Vec3f.getVec3f(1,1,0);
        Vec3f.normalize(dir);
        assertVec3fEquals(dir, direction, PRECISION);
    }
    
    @Test
    public void testGetAbsoluteDirectionVec()
    {
        assertTrue(hns.getAbsoluteDirection("1 2 3", direction));
        float [] dir = Vec3f.getVec3f(1,2,3);
        Vec3f.normalize(dir);
        assertVec3fEquals(dir, direction, PRECISION);
    }
}
