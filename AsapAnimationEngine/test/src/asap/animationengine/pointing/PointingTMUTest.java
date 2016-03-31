/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.pointing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.restpose.RestPose;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit test cases for the PointingTMU
 * @author hvanwelbergen
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class PointingTMUTest extends AbstractTimedPlanUnitTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    private TimedAnimationMotionUnit mockRelaxTMU = mock(TimedAnimationMotionUnit.class);
    private static final double TIME_PRECISION = 0.001;

    @SuppressWarnings("unchecked")
    @Override
    protected PointingTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        PointingMU mu = new PointingMU(vNext.getPartBySid(Hanim.l_shoulder), vNext.getPartBySid(Hanim.l_elbow),
                vNext.getPartBySid(Hanim.l_wrist), vNext.getPartBySid(Hanim.l_wrist));
        WorldObjectManager woManager = new WorldObjectManager();
        WorldObject blueBox = new VJointWorldObject(new VJoint());
        woManager.addWorldObject("bluebox", blueBox);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockAnimationPlayer.getVCurrPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        when(mockAnimationPlayer.getVNextPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        when(mockAnimationPlayer.getWoManager()).thenReturn(woManager);
        
        
        
        mu.target = "bluebox";
        mu = mu.copy(mockAnimationPlayer);
        
        RestPose mockRestPose = mock(RestPose.class);
        AnimationUnit mockRelaxMU = mock(AnimationUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);
        when(mockRestPose.getTransitionToRestDuration(any(VJoint.class), any(Set.class))).thenReturn(2d);
        when(mockRestPose.createTransitionToRest((Set<String>) any())).thenReturn(mockRelaxMU);
        when(
                mockAnimationPlayer.createTransitionToRest(any(FeedbackManager.class), (Set<String>) any(), any(TimePeg.class),
                        any(TimePeg.class), any(String.class), any(String.class), any(BMLBlockPeg.class), any(PegBoard.class))).thenReturn(
                mockRelaxTMU);
        when(mockAnimationPlayer.getTransitionToRestDuration(any(Set.class))).thenReturn(2d);
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));

        PointingTMU tmu = new PointingTMU(bfm, bbPeg, bmlId, id, mu, pegBoard, mockAnimationPlayer);
        tmu.resolveGestureKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        tmu.setTimePeg("relax", new TimePeg(bbPeg));
        tmu.setTimePeg("end", new TimePeg(bbPeg));
        return tmu;
    }

    @Test
    public void testGracefullInterrupt() throws TimedPlanUnitPlayException
    {
        PointingTMU tmu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0);
        tmu.setTimePeg("ready", TimePegUtil.createTimePeg(1));
        tmu.setTimePeg("relax", TimePegUtil.createTimePeg(2));
        tmu.setTimePeg("end", TimePegUtil.createTimePeg(3));
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(0);
        tmu.play(0);        
        assertEquals(TimedPlanUnitState.IN_EXEC, tmu.getState());
        tmu.interrupt(0.5f);
        tmu.play(0.49);
        tmu.play(0.495);
        assertEquals(TimedPlanUnitState.SUBSIDING, tmu.getState());
        assertEquals(0.49,tmu.getRelaxTime(), TIME_PRECISION);
        assertEquals(2.49,tmu.getEndTime(), TIME_PRECISION);
    }
}
