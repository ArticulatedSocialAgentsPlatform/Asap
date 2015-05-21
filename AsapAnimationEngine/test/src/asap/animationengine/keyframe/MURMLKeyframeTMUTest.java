/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.keyframe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.parser.Constraint;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.HnsHandshape;
import asap.animationengine.gesturebinding.MURMLMUBuilder;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.hns.Hns;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.Priority;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;
import asap.timemanipulator.LinearManipulator;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for MURMLKeyframeTMU
 * @author Herwin
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class MURMLKeyframeTMUTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    private AnimationUnit mockAnimationUnit = mock(AnimationUnit.class);

    private Hns mockHns = mock(Hns.class);
    private HnsHandshape mockHnsHandshapes = mock(HnsHandshape.class);
    private MURMLMUBuilder murmlMuBuilder = new MURMLMUBuilder(mockHns, mockHnsHandshapes);
    private MURMLGestureBehaviour mockBeh = mock(MURMLGestureBehaviour.class);

    private static final double RETRACTION_DURATION = 1;
    private static final float TIME_PRECISION = 0.001f;

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        VJoint vCurr = HanimBody.getLOA2HanimBody();
        VJoint vNext = HanimBody.getLOA2HanimBody();
        when(mockAniPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAniPlayer.getVNext()).thenReturn(vNext);
        when(mockAniPlayer.getTransitionToRestDuration(any(Set.class))).thenReturn(RETRACTION_DURATION);
        when(mockAniPlayer.createTransitionToRest(any(Set.class))).thenReturn(mockAnimationUnit);
    }

    private MURMLKeyframeMU mu = new MURMLKeyframeMU(new ArrayList<String>(), new LinearQuatFloatInterpolator(), new LinearManipulator(),
            ImmutableList.of(new KeyFrame(0, new float[0]), new KeyFrame(0, new float[0])), 0, true);

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
        MURMLKeyframeTMU tmu = new MURMLKeyframeTMU(bfm, bbPeg, bmlId, id, mu, pegBoard, mockAniPlayer);
        tmu.resolveGestureKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }

    @Test
    public void testResolveStart() throws BehaviourPlanningException, MUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)(r_shoulder 3 0 0 100)</posture></frame><frame ftime=\"1\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)(r_shoulder 3 80 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        au = au.copy(mockAniPlayer);
        TimedAnimationMotionUnit tmu = au.createTMU(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard);
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(1), new Constraint(), 0));
        tmu.resolveSynchs(BMLBlockPeg.GLOBALPEG, mockBeh, sacs);
        assertEquals(Priority.GESTURE, tmu.getPriority());
        assertEquals(1d, tmu.getStartTime(), TIME_PRECISION);
        assertEquals(1d, tmu.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(1d, tmu.getTime("stroke"), TIME_PRECISION);
        assertEquals(2d, tmu.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(2d, tmu.getRelaxTime(), TIME_PRECISION);
        assertEquals(3d, tmu.getEndTime(), TIME_PRECISION);
    }
}
