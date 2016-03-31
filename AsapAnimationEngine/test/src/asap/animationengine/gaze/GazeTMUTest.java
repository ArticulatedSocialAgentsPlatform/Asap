/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
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
    private VJoint vCurr = HanimBody.getLOA1HanimBodyWithEyes();
    private VJoint vNext = HanimBody.getLOA1HanimBodyWithEyes();
    private AnimationPlayer mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(vCurr, vNext);
    private PegBoard pegBoard = new PegBoard();
    private static final double TIME_PRECISION = 0.0001;
    private GazeBehaviour mockBeh = mock(GazeBehaviour.class);

    private GazeTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId) throws TMUSetupException, MUSetupException
    {
        //TweedGazeMU mu = new TweedGazeMU();
        DynamicGazeMU mu = new DynamicGazeMU();
        
        WorldObjectManager woManager = new WorldObjectManager();
        VJoint bluebox = new VJoint();
        bluebox.setTranslation(Vec3f.getVec3f(0, 0, 1));
        WorldObject blueBox = new VJointWorldObject(bluebox);
        woManager.addWorldObject("bluebox", blueBox);

        mu = mu.copy(mockAnimationPlayer);
        mu.player = mockAnimationPlayer;
        mu.target = "bluebox";
        mu.woManager = woManager;
        

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
    protected GazeTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime) throws TMUSetupException
    {
        GazeTMU tmu;
        try
        {
            tmu = setupPlanUnit(bfm, bbPeg, id, bmlId);
        }
        catch (MUSetupException e)
        {
            throw new RuntimeException(e);
        }
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }

    @Test
    @Override
    public void testSetStrokePeg()
    {

    }

    @Test
    public void testResolve() throws BehaviourPlanningException, TMUSetupException, MUSetupException
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
    
    @Test
    public void testStart() throws TMUSetupException, BehaviourPlanningException, TimedPlanUnitPlayException, MUSetupException
    {
        GazeTMU tmu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "gaze1", "bml1");
        List<TimePegAndConstraint> sacs = new ArrayList<>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(1), new Constraint(), 0));
        sacs.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(3), new Constraint(), 0));
        tmu.resolveSynchs(BMLBlockPeg.GLOBALPEG, mockBeh, sacs);                
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(1);
        assertEquals(1, tmu.getStartTime(), TIME_PRECISION);
        assertEquals(3, tmu.getTime("ready"), TIME_PRECISION);
        assertThat(tmu.getTime("relax"), greaterThan(3d));
        assertThat(tmu.getTime("end"),  greaterThan(tmu.getTime("ready")+1.95));
    }
}
