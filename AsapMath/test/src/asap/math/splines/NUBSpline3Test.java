/*******************************************************************************
 *******************************************************************************/
package asap.math.splines;

import hmi.math.Vec3f;
import hmi.testutil.math.Vec3fTestUtil;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the NUBSpline
 * @author hvanwelbergen
 */
public class NUBSpline3Test
{
    private static final float PRECISION = 0.001f;
    @Test
    public void test()
    {
        NUBSpline3 spline = new NUBSpline3(3);
        List<float[]>data = new ArrayList<>();
        List<float[]>vel = new ArrayList<>();
        List<Double>times = new ArrayList<>();
        int points = 4;
        
        for(int i=0;i<points;i++)
        {
            data.add(Vec3f.getVec3f(i,i,i));
            vel.add(Vec3f.getVec3f(0,0,0));
            times.add((double)i);
        }        
        spline.interpolate(data,times, vel);
        
        //some test values from C++ version
        Vec3fTestUtil.assertVec3fEquals(0.51f,0.51f,0.51f,spline.getPosition(0.3),PRECISION);
        Vec3fTestUtil.assertVec3fEquals(1.04444f,1.04444f,1.04444f,spline.getPosition(1.3),PRECISION);
        Vec3fTestUtil.assertVec3fEquals(2f,2f,2f,spline.getPosition(2.2),PRECISION);
    }
}
