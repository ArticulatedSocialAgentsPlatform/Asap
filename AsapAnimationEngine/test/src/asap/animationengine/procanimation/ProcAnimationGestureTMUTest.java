package asap.animationengine.procanimation;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.procanimation.ProcAnimationGestureMU;
import asap.animationengine.procanimation.ProcAnimationGestureTMU;
import asap.animationengine.procanimation.ProcAnimationMU;

import hmi.animation.VJoint;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.testutil.animation.HanimBody;
import static org.mockito.Mockito.*;
/**
 * Unit test cases for ProcAnimationGestureTMUs
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class ProcAnimationGestureTMUTest extends AbstractTimedPlanUnitTest
{
    private ProcAnimationMU mockProcAnimation = mock(ProcAnimationMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        ProcAnimationGestureMU mu = new ProcAnimationGestureMU();
        mu.setGestureUnit(mockProcAnimation);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockProcAnimation.copy((VJoint)any())).thenReturn(mockProcAnimation);
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        mu.setAnimationPlayer(mockAnimationPlayer);
        
        ProcAnimationGestureTMU tmu = new ProcAnimationGestureTMU(bfm, bbPeg, bmlId, id,mu );
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }

}
