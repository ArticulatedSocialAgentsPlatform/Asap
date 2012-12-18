package asap.animationengine.keyframe;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;
import asap.timemanipulator.LinearManipulator;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for MURMLKeyframeTMU
 * @author Herwin
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class MURMLKeyframeTMUTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    private AnimationUnit mockAnimationUnit = mock(AnimationUnit.class);
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        when(mockAniPlayer.getTransitionToRestDuration(any(Set.class))).thenReturn(1d);
        when(mockAniPlayer.createTransitionToRest(any(Set.class))).thenReturn(mockAnimationUnit);
    }
    //(List<String> targets, Interpolator interp, TimeManipulator manip, List<KeyFrame> keyFrames, int nrOfDofs,
    //        boolean allowDynamicStart)
    private MURMLKeyframeMU  mu = new MURMLKeyframeMU(new ArrayList<String>(), new LinearQuatFloatInterpolator(), 
            new LinearManipulator(), ImmutableList.of(new KeyFrame(0, new float[0]), new KeyFrame(0, new float[0])), 0, true);
    
    @Override
    protected MURMLKeyframeTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        try
        {
            mu = mu.copy(mockAniPlayer);
        }
        catch (MUSetupException e)
        {
            throw new RuntimeException(e);
        }
        MURMLKeyframeTMU tmu = new MURMLKeyframeTMU(bfm,bbPeg, bmlId, id, mu,pegBoard);
        tmu.resolveGestureKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }
}
