package asap.animationengine.procanimation;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;
import hmi.testutil.animation.HanimBody;

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.motionunit.MUPlayException;

/**
 * Unit test case for ProcAnimationGestureMU
 * @author hvanwelbergen
 *
 */
public class ProcAnimationGestureMUTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);    
    private VJoint vCurr;
    private VJoint vNext;
    private ProcAnimationGestureMU pag;
    private PegBoard pegBoard = new PegBoard();
    private static final float TIMING_PRECISION = 0.01f;
    @Before
    public void setup()
    {
        vCurr = HanimBody.getLOA1HanimBody();
        vCurr.getPart(Hanim.l_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vCurr.getPart(Hanim.l_wrist).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vCurr.getPart(Hanim.r_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vCurr.getPart(Hanim.r_wrist).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        
        vNext = HanimBody.getLOA1HanimBody();
        vNext.getPart(Hanim.l_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vNext.getPart(Hanim.l_wrist).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vNext.getPart(Hanim.r_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vNext.getPart(Hanim.r_wrist).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        SkeletonPoseRestPose pose = new SkeletonPoseRestPose(pegBoard);
        pose.setAnimationPlayer(mockAnimationPlayer);
        when(mockAnimationPlayer.getRestPose()).thenReturn(pose);
        
        ProcAnimationMU procAnimation = new ProcAnimationMU();
        String xmlString = "<ProcAnimation prefDuration=\"1\" minDuration=\"1\" maxDuration=\"1\">"+
                            "<EndEffector local=\"true\" target=\"l_wrist\" translation=\"0.1;-0.5;0.1\"/>"+
                            "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">"+
                            "<Keyframe time=\"0.2\" local=\"false\" value=\"0.0;0.7071068;0.7071068;0.0\"/>"+
                            "<Keyframe time=\"0.8\" local=\"false\" value=\"0.0;0.7071068;0.7071068;0.0\"/>"+
                            "</Keyframes>"+
                            "<KeyPosition id=\"strokeStart\" weight=\"1\" time=\"0.2\"/>"+
                            "<KeyPosition id=\"strokeEnd\" weight=\"1\" time=\"0.8\"/>"+
                            "</ProcAnimation>";
        procAnimation.readXML(xmlString);
        pag = new ProcAnimationGestureMU();
        pag.setVNext(vNext);
        pag.setGestureUnit(procAnimation.copy(vNext));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), 0.8, 1.0));
        pag.setAnimationPlayer(mockAnimationPlayer);
        pag.setupTransitionUnits();
    }
    
    @Test
    public void testPlayStart() throws MUPlayException
    {
        pag.play(0);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(1,0,0,0,q,0.001f);
    }
    
    @Test
    public void testPlayPrepEnd() throws MUPlayException
    {
        pag.play(0.199);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f,0.7071068f,0.7071068f,0.0f,q,0.01f);
    }
    
    @Test
    public void testPlayStrokeStart() throws MUPlayException
    {
        pag.play(0.201);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f,0.7071068f,0.7071068f,0.0f,q,0.01f);
    }
    
    @Test
    public void testPlayPrepHalf() throws MUPlayException
    {
        pag.play(0.1);
        float q[]=Quat4f.getQuat4f();
        float qRef[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        Quat4f.interpolate(qRef, Quat4f.getIdentity(), Quat4f.getQuat4f(0.0f,0.7071068f,0.7071068f,0.0f),0.5f);
        assertQuat4fRotationEquivalent(qRef,q,0.01f);
    }
    
    @Test
    public void testPlayStrokeEnd() throws MUPlayException
    {
        pag.play(0.799);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f,0.7071068f,0.7071068f,0.0f,q,0.01f);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPlayEnd() throws MUPlayException
    {
        RestPose mockRestPose = mock(RestPose.class);
        AnimationUnit mockRelaxMU = mock(AnimationUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);        
        when(mockRestPose.createTransitionToRest((Set<String>)any())).thenReturn(mockRelaxMU);
        pag.setupRelaxUnit();
        pag.play(1);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(1,0,0,0,q,0.001f);
    }
    
    @Ignore
    @Test
    public void getRetractionDuration()
    {
        float res[] = new float[3];
        Vec3f.sub(res, Vec3f.getVec3f(0,-1,0), Vec3f.getVec3f(0.1f,-0.5f,0.1f));
        assertEquals(FittsLaw.getHandTrajectoryDuration(Vec3f.length(res)),pag.getRetractionDuration(),TIMING_PRECISION);
    }
}
