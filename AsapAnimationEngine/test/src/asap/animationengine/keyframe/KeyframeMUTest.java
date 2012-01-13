package asap.animationengine.keyframe;

import static org.junit.Assert.*;
import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MotionUnit;

public class KeyframeMUTest
{
    private static final double SKI_STARTTIME = 1;
    private static final double SKI_ENDTIME = 2;
    private static final float[] HUMANOIDROOT_POS = Vec3f.getVec3f(0, 1, 1);
    private static final float[] HUMANOIDROOT_ROT = Quat4f.getQuat4f(1, 0, 0, 0);
    private static final float[] L_SHOULDER_ROT = Quat4f.getQuat4f(0, 1, 0, 0);
    private static final float[] R_SHOULDER_ROT = Quat4f.getQuat4f(0, 1, 0, 0);
    
    private SkeletonInterpolator ski;
    private VJoint human;
    private KeyframeMU kfmu;
    @Before
    public void setup()
    {
        String str =
        "<SkeletonInterpolator rotationEncoding=\"quaternions\" parts=\"HumanoidRoot r_shoulder l_shoulder\" encoding=\"T1R\">"+
        SKI_STARTTIME+" "+HUMANOIDROOT_POS[0]+" "+HUMANOIDROOT_POS[1]+" "+HUMANOIDROOT_POS[2]+" "+
        HUMANOIDROOT_ROT[0]+" "+HUMANOIDROOT_ROT[1]+" "+HUMANOIDROOT_ROT[2]+" "+HUMANOIDROOT_ROT[3]+" "+
        L_SHOULDER_ROT[0]+" "+L_SHOULDER_ROT[1]+" "+L_SHOULDER_ROT[2]+" "+L_SHOULDER_ROT[3]+" "+
        R_SHOULDER_ROT[0]+" "+R_SHOULDER_ROT[1]+" "+R_SHOULDER_ROT[2]+" "+R_SHOULDER_ROT[3]+"\n"+
        
        SKI_ENDTIME  +" "+HUMANOIDROOT_POS[0]+" "+HUMANOIDROOT_POS[1]+" "+HUMANOIDROOT_POS[2]+" "+
        HUMANOIDROOT_ROT[0]+" "+HUMANOIDROOT_ROT[1]+" "+HUMANOIDROOT_ROT[2]+" "+HUMANOIDROOT_ROT[3]+" "+
        L_SHOULDER_ROT[0]+" "+L_SHOULDER_ROT[1]+" "+L_SHOULDER_ROT[2]+" "+L_SHOULDER_ROT[3]+" "+
        R_SHOULDER_ROT[0]+" "+R_SHOULDER_ROT[1]+" "+R_SHOULDER_ROT[2]+" "+R_SHOULDER_ROT[3]+" "+        
        "</SkeletonInterpolator>";
        ski = new SkeletonInterpolator();
        ski.readXML(str);        
        human = HanimBody.getLOA1HanimBody();
        ski.setTarget(human);
        kfmu = new KeyframeMU(ski);
    }
    
    @Test
    public void testJointFilter()
    {
        human.setRotation(0,0,0,1);
        human.getPart(Hanim.r_shoulder).setRotation(0,0,0,1);        
        
        kfmu.setParameterValue("joints", "r_shoulder l_shoulder");
        kfmu.play(0);
        
        float q[]=Quat4f.getQuat4f();
        human.getPart("l_shoulder").getRotation(q);
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,0.001f);
    }
    
    @Test
    public void testFilterThenMirror()
    {
        human.setRotation(0,0,0,1);
        
        kfmu.setParameterValue("joints", "l_shoulder");        
        kfmu.setParameterValue("mirror", "true");
        
        kfmu.play(0);
        float q[]=Quat4f.getQuat4f();
        human.getPart(Hanim.HumanoidRoot).getRotation(q);
        assertQuat4fRotationEquivalent(0,0,0,1, q, 0.0001f);
        
        human.getPart(Hanim.r_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(1,0,0,0, q, 0.0001f);
        
        human.getPart(Hanim.l_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,0.001f);
    }
    
    @Test
    public void testMirrorThenFilter()
    {
        human.setRotation(0,0,0,1);
        
        kfmu.setParameterValue("mirror", "true");
        kfmu.setParameterValue("joints", "l_shoulder");
        
        kfmu.play(0);
        float q[]=Quat4f.getQuat4f();
        human.getPart(Hanim.HumanoidRoot).getRotation(q);
        assertQuat4fRotationEquivalent(0,0,0,1, q, 0.0001f);
        
        human.getPart(Hanim.r_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(1,0,0,0, q, 0.0001f);
        
        human.getPart(Hanim.l_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,0.001f);
    }
    @Test
    public void testCopy() throws MUPlayException
    {
        VJoint humanCopy = HanimBody.getLOA1HanimBody();
        MotionUnit kfmuCopy = kfmu.copy(humanCopy);
        
        kfmuCopy.play(0);        
        float q[]=Quat4f.getQuat4f();
        humanCopy.getPart(Hanim.HumanoidRoot).getRotation(q);
        assertQuat4fRotationEquivalent(HUMANOIDROOT_ROT, q, 0.0001f);
        
        humanCopy.getPart(Hanim.r_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT, q, 0.0001f);
        
        humanCopy.getPart(Hanim.l_shoulder).getRotation(q);
        assertQuat4fRotationEquivalent(L_SHOULDER_ROT, q, 0.0001f);
        
        assertEquals(1,kfmuCopy.getPreferedDuration(),0.001f);
    }
    
    @Test
    public void testGetDuration()
    {
        assertEquals(1,kfmu.getPreferedDuration(),0.001f);
    }
}
