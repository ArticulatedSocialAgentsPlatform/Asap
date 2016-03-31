/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.motionunit.MUPlayException;
import asap.realizer.planunit.KeyPosition;

/**
 * Unit tests for a ProcAnimationGestureMU that specifies an endeffector translation
 * @author hvanwelbergen
 * 
 */
public class ProcAnimationGestureMUEndEffectorTest
{
    private VJoint vNext = HanimBody.getLOA1HanimBody();
    private VJoint vAdditive = HanimBody.getLOA1HanimBody();
    private VJoint vCurr = vNext;//small hack so we don't have to copy curr to next
    private AnimationPlayer mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(vCurr,vNext);
    private static final float POSITION_PRECISION = 0.01f;
    private static final float TIMING_PRECISION = 0.01f;
    private ProcAnimationGestureMU pag;

    @Before
    public void setup() throws MUPlayException, MUSetupException
    {
        vCurr.getPart(Hanim.l_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vCurr.getPart(Hanim.l_wrist).setTranslation(Vec3f.getVec3f(0, -0.3f, 0));
        vCurr.getPart(Hanim.r_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vCurr.getPart(Hanim.r_wrist).setTranslation(Vec3f.getVec3f(0, -0.3f, 0));

        vNext.getPart(Hanim.l_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vNext.getPart(Hanim.l_wrist).setTranslation(Vec3f.getVec3f(0, -0.3f, 0));
        vNext.getPart(Hanim.r_elbow).setTranslation(Vec3f.getVec3f(0, -0.5f, 0));
        vNext.getPart(Hanim.r_wrist).setTranslation(Vec3f.getVec3f(0, -0.3f, 0));        
        
        RestPose pose = new SkeletonPoseRestPose();
        pose.setAnimationPlayer(mockAnimationPlayer);
        when(mockAnimationPlayer.getRestPose()).thenReturn(pose);

        ProcAnimationMU procAnimation = new ProcAnimationMU();
        String xmlString = "<ProcAnimation prefDuration=\"1\" minDuration=\"1\" maxDuration=\"1\">"
                + "<EndEffector local=\"true\" target=\"l_wrist\" translation=\"0.1+t*0.1;-0.5;0.1\"/>"
                + "<KeyPosition id=\"strokeStart\" weight=\"1\" time=\"0.2\"/>"
                + "<KeyPosition id=\"strokeEnd\" weight=\"1\" time=\"0.8\"/>" + "</ProcAnimation>";
        procAnimation.readXML(xmlString);
        pag = new ProcAnimationGestureMU();
        pag.setVNext(vNext);
        pag.setGestureUnit(procAnimation.copy(vNext, vAdditive));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), 0.2, 1.0));
        pag.addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), 0.8, 1.0));
        pag.setAnimationPlayer(mockAnimationPlayer);
        pag.setupTransitionUnits();        
        pag.startUnit(0);
    }

    @Test
    public void testPositionMid() throws MUPlayException
    {
        pag.play(0.5);
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0.15f, -0.5f, 0.1f, t, POSITION_PRECISION);
    }

    @Test
    public void testPositionStart() throws MUPlayException
    {
        pag.play(0);
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0f, -0.8f, 0f, t, POSITION_PRECISION);
    }
    
    @Test
    public void testPositionEnd() throws MUPlayException
    {
        pag.play(0.799);
        pag.setupRelaxUnit();
        pag.play(1);
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0f, -0.8f, 0f, t, POSITION_PRECISION);
    }
    
    @Test
    public void testPositionBeforeStrokeStart() throws MUPlayException
    {
        pag.play(0.199);
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0.12f, -0.5f, 0.1f, t, POSITION_PRECISION);
    }
    
    @Test
    public void testPositionBeforeStrokeEnd() throws MUPlayException
    {
        pag.play(0.799);        
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0.18f, -0.5f, 0.1f, t, POSITION_PRECISION);
    }
    
    @Test
    public void testPositionAfterStrokeEnd() throws MUPlayException
    {
        pag.play(0.799);
        pag.setupRelaxUnit();        
        pag.play(0.801);
        
        float t[] = Vec3f.getVec3f();
        vNext.getPart("l_wrist").getPathTranslation(vNext, t);
        assertVec3fEquals(0.18f, -0.5f, 0.1f, t, POSITION_PRECISION);
    }
    
    @Test
    public void getPreparationDuration() throws MUPlayException
    {
        float res[] = new float[3];
        Vec3f.sub(res, Vec3f.getVec3f(0, -0.8f, 0), Vec3f.getVec3f(0.12f, -0.5f, 0.1f));
        assertEquals(FittsLaw.getHandTrajectoryDuration(Vec3f.length(res)), pag.getPreparationDuration(), TIMING_PRECISION);
    }
    
    @Test
    public void getRetractionDuration() throws MUPlayException
    {
        pag.play(0.799);
        pag.setupRelaxUnit();
        float res[] = new float[3];        
        Vec3f.sub(res, Vec3f.getVec3f(0, -0.8f, 0), Vec3f.getVec3f(0.18f, -0.5f, 0.1f));
        assertEquals(FittsLaw.getHandTrajectoryDuration(Vec3f.length(res)), pag.getRetractionDuration(), TIMING_PRECISION);
    }
}
