/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import hmi.math.Vec3f;

import org.junit.Before;
import org.junit.Test;
import org.lsmp.djep.xjep.XJep;

/**
 * Unit test cases for the endeffector
 * @author hvanwelbergen
 * 
 */
public class EndEffectorTest
{
    private static final float POSITION_PRECISION = 0.01f;
    private XJep parser = new XJep();
    private EndEffector eff;
    
    @Before
    public void setup()
    {
        parser.addVariable("t", 0);
        eff = new EndEffector(parser);        
    }
    
    @Test
    public void testEvaluateStart()
    {
        eff.setTranslation(0, "t");
        eff.setTranslation(1, "t+1");
        eff.setTranslation(2, "t+2");
        float [] dst = Vec3f.getVec3f();
        eff.evaluateTrans(dst, 0);        
        assertVec3fEquals(0,1,2,dst,POSITION_PRECISION);
    }
    
    @Test
    public void testEvaluateEnd()
    {
        eff.setTranslation(0, "t");
        eff.setTranslation(1, "t+1");
        eff.setTranslation(2, "t+2");
        float [] dst = Vec3f.getVec3f();
        eff.evaluateTrans(dst, 1);        
        assertVec3fEquals(1,2,3,dst,POSITION_PRECISION);
    }
    
    @Test
    public void testEvaluateHalfway()
    {
        eff.setTranslation(0, "t");
        eff.setTranslation(1, "t+1");
        eff.setTranslation(2, "t+2");
        float [] dst = Vec3f.getVec3f();
        eff.evaluateTrans(dst, 0.5);        
        assertVec3fEquals(0.5f,1.5f,2.5f,dst,POSITION_PRECISION);
    }
    
    @Test
    public void testEvaluateFromXML()
    {
        String str = "<EndEffector local=\"true\" target=\"l_wrist\" translation=\"0.1+t;-0.5;0.1\"/>";
        eff.readXML(str);
        float [] dst = Vec3f.getVec3f();
        eff.evaluateTrans(dst, 0.5);        
        assertVec3fEquals(0.6f,-0.5f,0.1f,dst,POSITION_PRECISION);
    }
}
