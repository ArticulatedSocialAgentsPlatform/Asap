/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import asap.realizer.planunit.ParameterException;
/**
 * Unit test cases for the ProcAnimationMU
 * @author hvanwelbergen
 *
 */
public class ProcAnimationTest
{
    private IKBody ikBody;
    private VJoint human;
    private ProcAnimationMU procAnimation;
    
    private static final double SKI_STARTTIME = 1;
    private static final double SKI_ENDTIME = 2;
    private static final float[] HUMANOIDROOT_POS = Vec3f.getVec3f(0, 1, 1);
    private static final float[] HUMANOIDROOT_ROT = Quat4f.getQuat4f(1, 0, 0, 0);
    private static final float[] L_SHOULDER_ROT = Quat4f.getQuat4f(0, 0, 1, 0);
    private static final float[] R_SHOULDER_ROT = Quat4f.getQuat4f(0, 0, 0, 1);
    
    private static final float ANIMATION_PRECISION = 0.0001f;
    @Before
    public void loadDaeHuman()
    {
        human = HanimBody.getLOA1HanimBody();
        ikBody = new IKBody(human);
        
        String puString = "<ProcAnimation duration=\"3\" prefDuration=\"3\" minDuration=\"3\" maxDuration=\"3\">"+
        "<SkeletonInterpolator rotationEncoding=\"quaternions\" parts=\"HumanoidRoot l_shoulder r_shoulder\" encoding=\"T1R\">"+
        SKI_STARTTIME+" "+HUMANOIDROOT_POS[0]+" "+HUMANOIDROOT_POS[1]+" "+HUMANOIDROOT_POS[2]+" "+
        HUMANOIDROOT_ROT[0]+" "+HUMANOIDROOT_ROT[1]+" "+HUMANOIDROOT_ROT[2]+" "+HUMANOIDROOT_ROT[3]+" "+
        L_SHOULDER_ROT[0]+" "+L_SHOULDER_ROT[1]+" "+L_SHOULDER_ROT[2]+" "+L_SHOULDER_ROT[3]+" "+
        R_SHOULDER_ROT[0]+" "+R_SHOULDER_ROT[1]+" "+R_SHOULDER_ROT[2]+" "+R_SHOULDER_ROT[3]+"\n"+
        
        SKI_ENDTIME  +" "+HUMANOIDROOT_POS[0]+" "+HUMANOIDROOT_POS[1]+" "+HUMANOIDROOT_POS[2]+" "+
        HUMANOIDROOT_ROT[0]+" "+HUMANOIDROOT_ROT[1]+" "+HUMANOIDROOT_ROT[2]+" "+HUMANOIDROOT_ROT[3]+" "+
        L_SHOULDER_ROT[0]+" "+L_SHOULDER_ROT[1]+" "+L_SHOULDER_ROT[2]+" "+L_SHOULDER_ROT[3]+" "+
        R_SHOULDER_ROT[0]+" "+R_SHOULDER_ROT[1]+" "+R_SHOULDER_ROT[2]+" "+R_SHOULDER_ROT[3]+" "+        
        "</SkeletonInterpolator>"+
        "</ProcAnimation>";
        procAnimation = new ProcAnimationMU();
        procAnimation.readXML(puString);
        procAnimation.setup(new ArrayList<Parameter>(), ikBody);
    }
    
    @Test
    public void testJoints() throws ParameterException
    {
        procAnimation.setParameterValue("joints", "r_shoulder r_elbow");
        
        human.getPart("HumanoidRoot").setTranslation(0,0,0);
        procAnimation.play(0);
        float t[]=Vec3f.getVec3f();
        human.getPart("HumanoidRoot").getTranslation(t);
        assertVec3fEquals(0,0,0,t,ANIMATION_PRECISION);
    }
    
    @Test
    public void testJointsAfterCopy() throws MUPlayException, ParameterException, MUSetupException
    {
        procAnimation.setParameterValue("joints", "r_shoulder r_elbow");
        
        VJoint humanCopy = HanimBody.getLOA1HanimBody();
        humanCopy.getPart("HumanoidRoot").setTranslation(0,0,0);
        AnimationUnit mu = procAnimation.copy(humanCopy);
        mu.play(0);
        float t[]=Vec3f.getVec3f();
        humanCopy.getPart("HumanoidRoot").getTranslation(t);
        assertVec3fEquals(0,0,0,t,ANIMATION_PRECISION);
    }
    

    @Test
    public void testMirror() throws ParameterException
    {
        procAnimation.setParameterValue("mirror", "true");
        
        procAnimation.play(0);
        
        float q[]=Quat4f.getQuat4f();
        
        human.getPart("HumanoidRoot").getRotation(q);
        assertQuat4fRotationEquivalent(HUMANOIDROOT_ROT[0],HUMANOIDROOT_ROT[1],-HUMANOIDROOT_ROT[2],-HUMANOIDROOT_ROT[3],q,ANIMATION_PRECISION);
        
        human.getPart("l_shoulder").getRotation(q);
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,ANIMATION_PRECISION);
        
        human.getPart("r_shoulder").getRotation(q);
        assertQuat4fRotationEquivalent(L_SHOULDER_ROT[0],L_SHOULDER_ROT[1],-L_SHOULDER_ROT[2],-L_SHOULDER_ROT[3],q,ANIMATION_PRECISION);
    }
    
    @Test
    public void testMirrorThenJoints() throws ParameterException
    {
        procAnimation.setParameterValue("mirror", "true");
        procAnimation.setParameterValue("joints", "l_shoulder");
        
        
        procAnimation.play(0);
        float q[]=Quat4f.getQuat4f();
        human.getPart("l_shoulder").getRotation(q);       
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,ANIMATION_PRECISION);
    }
    
    @Test
    public void testJointsThenMirror() throws ParameterException
    {
        procAnimation.setParameterValue("joints", "l_shoulder");
        procAnimation.setParameterValue("mirror", "true");        
        
        procAnimation.play(0);
        float q[]=Quat4f.getQuat4f();
        human.getPart("l_shoulder").getRotation(q);       
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT[0],R_SHOULDER_ROT[1],-R_SHOULDER_ROT[2],-R_SHOULDER_ROT[3],q,ANIMATION_PRECISION);
    }
    
    @Test
    public void testAdditive() throws ParameterException, MUSetupException
    {
        procAnimation.setParameterValue("blending", "ADDITIVE");
        VJoint humanCopy = HanimBody.getLOA1HanimBody();
        VJoint additiveCopy = HanimBody.getLOA1HanimBody();
        procAnimation.copy(humanCopy,additiveCopy);
    }
    
    @Test
    public void testSkeletonInterpolator()
    {
        procAnimation.play(0);
        
        float q[]=Quat4f.getQuat4f();
        human.getPart("HumanoidRoot").getRotation(q);        
        assertQuat4fRotationEquivalent(HUMANOIDROOT_ROT,q,ANIMATION_PRECISION);        
    
        human.getPart("r_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT,q,ANIMATION_PRECISION);
        
        human.getPart("l_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(L_SHOULDER_ROT,q,ANIMATION_PRECISION);
    }
    
    @Test
    public void testSkeletonInterpolatorSet() throws MUSetupException
    {
        ProcAnimationMU puCopy = new ProcAnimationMU();
        puCopy.setup(new ArrayList<Parameter>(), ikBody);
        puCopy.set(procAnimation);
        //puCopy.setup(new ArrayList<Parameter>(), ikBody);
        
        puCopy.play(0);
        
        float q[]=Quat4f.getQuat4f();
        human.getPart("HumanoidRoot").getRotation(q);        
        assertQuat4fRotationEquivalent(HUMANOIDROOT_ROT,q,ANIMATION_PRECISION);        
    
        human.getPart("r_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT,q,ANIMATION_PRECISION);
        
        human.getPart("l_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(L_SHOULDER_ROT,q,ANIMATION_PRECISION);
    }
    
    @Test
    public void testCopy() throws MUPlayException, MUSetupException
    {
        VJoint humanCopy = HanimBody.getLOA1HanimBody();
        AnimationUnit puCopy = procAnimation.copy(humanCopy);
        
        puCopy.play(0);
        float q[]=Quat4f.getQuat4f();
        humanCopy.getPart("HumanoidRoot").getRotation(q);        
        assertQuat4fRotationEquivalent(HUMANOIDROOT_ROT,q,ANIMATION_PRECISION);        
    
        humanCopy.getPart("r_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(R_SHOULDER_ROT,q,ANIMATION_PRECISION);
        
        humanCopy.getPart("l_shoulder").getRotation(q);        
        assertQuat4fRotationEquivalent(L_SHOULDER_ROT,q,ANIMATION_PRECISION);
    }
}
