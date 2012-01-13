package asap.animationengine.procanimation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.procanimation.ProcAnimationGestureMU;
import asap.animationengine.procanimation.ProcAnimationMU;

import hmi.animation.VJoint;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.BMLGestureSync;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;
import static hmi.testutil.math.Quat4fTestUtil.*;
public class ProcAnimationGestureMUTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);    
    private VJoint vCurr;
    private VJoint vNext;
    private ProcAnimationGestureMU pag;
    
    @Before
    public void setup()
    {
        vCurr = HanimBody.getLOA1HanimBody();
        vNext = HanimBody.getLOA1HanimBody();
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        
        ProcAnimationMU procAnimation = new ProcAnimationMU();
        String xmlString = "<ProcAnimation prefDuration=\"1\" minDuration=\"1\" maxDuration=\"1\">"+
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
    public void testPlayStrokeEnd() throws MUPlayException
    {
        pag.play(0.799);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(0.0f,0.7071068f,0.7071068f,0.0f,q,0.01f);
    }
    
    @Test
    public void testPlayEnd() throws MUPlayException
    {
        pag.play(1);
        float q[]=Quat4f.getQuat4f();
        vNext.getPart("r_wrist").getRotation(q);
        assertQuat4fRotationEquivalent(1,0,0,0,q,0.001f);
    }
}
