/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hmi.math.Vec3f;
import hmi.util.Resources;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Hns integration test: checks if a large example hns file can be parsed
 * @author hvanwelbergen
 * 
 */
public class HnsIntegrationTest
{
    private static final float PRECISION = 0.0001f;
    private Hns hns = new Hns();    

    @Before
    public void setup() throws IOException
    {
        hns.readXML(new Resources("").getReader("billie_hns.xml"));
    }

    @Test
    public void testPalmOrientation()
    {
        assertEquals(-90.0, hns.getSymbolValue("palmOrientations", "PalmU"), PRECISION);
    }

    @Test
    public void testInvalidHandLocation()
    {
        float vec[] = Vec3f.getVec3f();
        assertFalse(hns.getHandLocation("LocBelowStomach", vec));
    }

    @Test
    public void testAbsoluteHandLocation()
    {
        float vec[] = Vec3f.getVec3f();
        assertTrue(hns.getHandLocation("1 2 3", vec));
        assertVec3fEquals(1, 2, 3, vec, PRECISION);
    }

    @Test
    public void testHandLocationInvalid()
    {
        float vec[] = Vec3f.getVec3f();
        hns.getHandLocation("LocBelowStomach LocCenterRight LocNorm", vec);
    }
    
    @Test
    public void testDistanceAbs()
    {
        assertEquals(0.2, hns.getDistance("0.2"), PRECISION);
    }
    
    @Test
    public void testDistance()
    {
        assertEquals(0.4, hns.getDistance("DistFFar"), PRECISION);
    }
}
