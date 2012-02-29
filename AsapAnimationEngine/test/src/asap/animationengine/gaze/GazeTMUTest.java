package asap.animationengine.gaze;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.PegBoard;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.elckerlyc.world.WorldObject;
import hmi.elckerlyc.world.WorldObjectManager;
import hmi.testutil.animation.HanimBody;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.restpose.RestPose;

/**
 * Unit test cases for the GazeTMU
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class GazeTMUTest extends AbstractTimedPlanUnitTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    
    @SuppressWarnings("unchecked")
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        GazeMU mu = new GazeMU();        
        
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        WorldObjectManager woManager = new WorldObjectManager();
        WorldObject blueBox = new WorldObject(new VJoint());
        woManager.addWorldObject("bluebox", blueBox);
        
        mu.player = mockAnimationPlayer;
        mu.target = "bluebox";
        mu.woManager = woManager;
        mu.neck = vNext.getPartBySid(Hanim.skullbase);
        
        RestPose mockRestPose = mock(RestPose.class);
        MotionUnit mockRelaxMU = mock(MotionUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);        
        when(mockRestPose.createTransitionToRest((Set<String>)any())).thenReturn(mockRelaxMU);
        
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        mu.setupRelaxUnit();
        
        GazeTMU tmu = new GazeTMU(bfm,bbPeg,bmlId,id,mu, pegBoard);
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }

}
