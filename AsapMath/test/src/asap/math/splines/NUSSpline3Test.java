package asap.math.splines;

import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit test cases for the NUSSpline3
 * @author hvanwelbergen
 *
 */
public class NUSSpline3Test
{
    @Test
    public void test()
    {
        NUSSpline3 spline = new NUSSpline3(3);
        List<float[]>data = new ArrayList<>();
        List<Double>times = new ArrayList<>();
        int points = 4;
        
        for(int i=0;i<points;i++)
        {
            data.add(Vec3f.getVec3f(i,i,i));
            times.add((double)i);
        }
        
        List<SparseVelocityDef> vel = new ArrayList<>();
        vel.add(new SparseVelocityDef(0, Vec3f.getVec3f(0,0,0)));
        spline.interpolate3(data,times, vel);
        
        for(double i=0;i<points;i+=0.1)
        {
            System.out.println("spline("+i+")="+Vec3f.toString(spline.getPosition(i)));
        }
    }
}
