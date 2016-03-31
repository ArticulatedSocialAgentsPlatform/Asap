/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.motionunit.MUPlayException;
import asap.realizer.planunit.KeyPosition;

/**
 * Unit test case for a ProcAnimationGestureMU that specifies keyframes
 * @author hvanwelbergen
 * 
 */
public class ProcAnimationGestureMUKeyframesTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private VJoint vCurr;
    private VJoint vNext;
    private VJoint vAdd;
    private ProcAnimationGestureMU pag;    
    private static final float ROTATION_PRECISION = 0.01f;

    @Before
    public void setup() throws MUSetupException
    {
        vCurr = HanimBody.getLOA1HanimBody();
        vNext = HanimBody.getLOA1HanimBody();
        vAdd = HanimBody.getLOA1HanimBody();

        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockAnimationPlayer.constructAdditiveBody()).thenReturn(vAdd);
        RestPose pose = new SkeletonPoseRestPose();
        pose = pose.copy(mockAnimationPlayer);
        when(mockAnimationPlayer.getRestPose()).thenReturn(pose);

        ProcAnimationMU procAnimation = new ProcAnimationMU();
        String xmlString = "<ProcAnimation prefDuration=\"1\" minDuration=\"1\" maxDuration=\"1\">" +

        "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">"
                + "<Keyframe time=\"0.2\" local=\"false\" value=\"0.0;0.7071068;0.7071068;0.0\"/>"
                + "<Keyframe time=\"0.8\" local=\"false\" value=\"0.0;0.7071068;0.7071068;0.0\"/>" + "</Keyframes>"
                + "<KeyPosition id=\"strokeStart\" weight=\"1\" time=\"0.2\"/>"
                + "<KeyPosition id=\"strokeEnd\" weight=\"1\" time=\"0.8\"/>" + "</ProcAnimation>";
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
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(1, 0, 0, 0, q, ROTATION_PRECISION);
    }

    @Test
    public void testPlayPrepEnd() throws MUPlayException
    {
        pag.play(0.199);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f, 0.7071068f, 0.7071068f, 0.0f, q, ROTATION_PRECISION);
    }

    @Test
    public void testPlayStrokeStart() throws MUPlayException
    {
        pag.play(0.201);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f, 0.7071068f, 0.7071068f, 0.0f, q, ROTATION_PRECISION);
    }

    @Test
    public void testPlayPrepHalf() throws MUPlayException
    {
        pag.play(0.1);
        float q[] = Quat4f.getQuat4f();
        float qRef[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        Quat4f.interpolate(qRef, Quat4f.getIdentity(), Quat4f.getQuat4f(0.0f, 0.7071068f, 0.7071068f, 0.0f), 0.5f);
        assertQuat4fRotationEquivalent(qRef, q, ROTATION_PRECISION);
    }

    @Test
    public void testPlayStrokeEnd() throws MUPlayException
    {
        pag.play(0.799);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f, 0.7071068f, 0.7071068f, 0.0f, q, ROTATION_PRECISION);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayEnd() throws MUPlayException
    {
        RestPose mockRestPose = mock(RestPose.class);
        AnimationUnit mockRelaxMU = mock(AnimationUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);
        when(mockRestPose.createTransitionToRest((Set<String>) any())).thenReturn(mockRelaxMU);
        when(mockRestPose.createTransitionToRestFromVJoints((Collection<VJoint>) any())).thenReturn(mockRelaxMU);
        pag.setupRelaxUnit();
        pag.play(1);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(1, 0, 0, 0, q, ROTATION_PRECISION);
    }

}
