package asap.animationengine.gaze;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.GazeBehaviour;
import saiba.bml.parser.Constraint;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit test cases for the GazeTMU
 * @author hvanwelbergen
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class GazeTMUTest extends AbstractTimedPlanUnitTest
{
    private VJoint vCurr = HanimBody.getLOA1HanimBody();
    private VJoint vNext = HanimBody.getLOA1HanimBody();
    private AnimationPlayer mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(vCurr, vNext);
    private PegBoard pegBoard = new PegBoard();
    private static final double TIME_PRECISION = 0.0001;
    private GazeBehaviour mockBeh = mock(GazeBehaviour.class);

    private GazeTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId)
    {
        TweedGazeMU mu = new TweedGazeMU();

        WorldObjectManager woManager = new WorldObjectManager();
        VJoint bluebox = new VJoint();
        bluebox.setTranslation(Vec3f.getVec3f(1, 1, 1));
        WorldObject blueBox = new VJointWorldObject(bluebox);
        woManager.addWorldObject("bluebox", blueBox);

        mu.player = mockAnimationPlayer;
        mu.target = "bluebox";
        mu.woManager = woManager;
        mu.neck = vNext.getPartBySid(Hanim.skullbase);

        RestGaze mockRestGaze = mock(RestGaze.class);
        TimedAnimationMotionUnit mockTMU = mock(TimedAnimationMotionUnit.class);
        when(mockAnimationPlayer.getGazeTransitionToRestDuration()).thenReturn(2d);
        when(mockAnimationPlayer.getRestGaze()).thenReturn(mockRestGaze);
        when(
                mockRestGaze.createTransitionToRest(any(FeedbackManager.class), any(TimePeg.class), any(TimePeg.class), anyString(),
                        anyString(), any(BMLBlockPeg.class), eq(pegBoard))).thenReturn(mockTMU);

        return new GazeTMU(bfm, bbPeg, bmlId, id, mu, pegBoard, mockAnimationPlayer);
    }

    @Override
    protected GazeTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        GazeTMU tmu = setupPlanUnit(bfm, bbPeg, id, bmlId);
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }

    @Test
    @Override
    public void testSetStrokePeg()
    {

    }

    @Test
    public void testResolve() throws BehaviourPlanningException
    {
        GazeTMU tmu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "gaze1", "bml1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(1), new Constraint(), 0));
        sacs.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN), new Constraint(), 0));
        sacs.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN), new Constraint(), 0));
        tmu.resolveSynchs(BMLBlockPeg.GLOBALPEG, mockBeh, sacs);
        assertEquals(1, tmu.getStartTime(), TIME_PRECISION);
        assertThat(tmu.getTime("ready"), greaterThan(1d));
        assertThat(tmu.getTime("end"), greaterThan(1d));
    }
}
