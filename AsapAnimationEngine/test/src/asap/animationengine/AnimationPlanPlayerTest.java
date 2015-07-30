/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import static asap.realizertestutil.util.KeyPositionMocker.stubKeyPositions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.AdditiveRotationBlend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.transitions.TransitionMU;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for a PlanPlayer using TimedMotionUnits
 * 
 * @author Herwin
 * 
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class AnimationPlanPlayerTest
{
    private AnimationUnit muMock1 = mock(AnimationUnit.class);
    private AnimationUnit muMock2 = mock(AnimationUnit.class);
    private TransitionMU muMockTransition = mock(TransitionMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private List<BMLSyncPointProgressFeedback> fbList = new ArrayList<>();
    private List<BMLWarningFeedback> exList = new ArrayList<>();
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private RestPose mockRestPose = mock(RestPose.class);
    private RestGaze mockRestGaze = mock(RestGaze.class);
    private AdditiveRotationBlend mockAdditiveRotationBlend = mock(AdditiveRotationBlend.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private PegBoard pegBoard = new PegBoard();
    PlanManager<TimedAnimationUnit> planManager = new PlanManager<>();

    private ListBMLFeedbackListener fbl;
    private AnimationPlanPlayer app;
    private static final double TIMING_PRECISION = 0.0001;

    @Before
    public void setup()
    {
        fbl = new ListBMLFeedbackListener.Builder().feedBackList(fbList).build();
        app = new AnimationPlanPlayer(mockRestPose, mockRestGaze, fbManager, planManager, new DefaultTimedPlanUnitPlayer(), pegBoard);
        app.setAdditiveBlender(mockAdditiveRotationBlend);
        app.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(exList).build());
        fbManager.addFeedbackListener(fbl);
    }

    private TimedAnimationMotionUnit createMotionUnit(String behId, String bmlId, AnimationUnit mu)
    {
        return new TimedAnimationMotionUnit(fbManager, BMLBlockPeg.GLOBALPEG, bmlId, behId, mu, pegBoard, mockAnimationPlayer);
    }

    private TimedAnimationMotionUnit createTransitionTMU(String behId, String bmlId, TransitionMU mu)
    {
        return new TimedAnimationMotionUnit(fbManager, BMLBlockPeg.GLOBALPEG, bmlId, behId, mu, pegBoard, mockAnimationPlayer);
    }

    @Test
    public void testPlayTmu() throws MUPlayException
    {
        // Playing one motion unit at t=0, checking for state transition,
        // feedback calls, no warnings
        TimedAnimationMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        planManager.addPlanUnit(tmu1);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tmu1.setTimePeg("start", tpStart);
        tmu1.setState(TimedPlanUnitState.LURKING);
        app.play(0);

        assertEquals(TimedPlanUnitState.IN_EXEC, tmu1.getState());
        assertEquals(1, fbList.size());
        assertEquals("behaviour1", fbList.get(0).getBehaviourId());
        assertEquals("bml1", fbList.get(0).getBMLId());
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals(0, fbList.get(0).getTime(), TIMING_PRECISION);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(0);
    }

    @Test
    public void testPlayTmuPastEnd() throws MUPlayException
    {
        // Playing one motion unit, 2 calls to play: during valid play time and
        // after end time. Checking for state transitions, feedback calls, no
        // warnings
        TimedAnimationMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        planManager.addPlanUnit(tmu1);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tmu1.setTimePeg("start", tpStart);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);
        tmu1.setTimePeg("end", tpEnd);

        tmu1.setState(TimedPlanUnitState.LURKING);
        app.play(0.99);
        assertTrue(exList.size() == 0);
        assertTrue(tmu1.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 1);
        assertTrue(fbList.get(0).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(0).getBMLId().equals("bml1"));
        assertTrue(fbList.get(0).getSyncId().equals("start"));
        assertEquals(0.99, fbList.get(0).getTime(), TIMING_PRECISION);
        app.play(2);
        assertTrue(tmu1.getState() == TimedPlanUnitState.DONE);
        assertEquals(3, fbList.size());
        assertTrue(exList.size() == 0);
        assertTrue(fbList.get(0).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(0).getBMLId().equals("bml1"));
        assertTrue(fbList.get(0).getSyncId().equals("start"));
        assertEquals(0.99, fbList.get(0).getTime(), TIMING_PRECISION);
        assertTrue(fbList.get(1).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(1).getBMLId().equals("bml1"));
        assertTrue(fbList.get(1).getSyncId().equals("relax"));       
        assertTrue(fbList.get(1).getTime() == 2);
        assertTrue(fbList.get(2).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(2).getBMLId().equals("bml1"));
        assertTrue(fbList.get(2).getSyncId().equals("end"));        
        assertTrue(fbList.get(2).getTime() == 2);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(anyDouble());
    }

    @Test
    public void testPlay2Tmu() throws MUPlayException
    {
        // Plays a succesion of two motion units. Checking for state
        // transitions, feedback calls, no warnings.
        TimedAnimationMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        planManager.addPlanUnit(tmu1);

        TimedAnimationMotionUnit tmu2 = createMotionUnit("behaviour2", "bml1", muMock2);
        planManager.addPlanUnit(tmu2);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        stubKeyPositions(muMock2, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        TimePeg tpStart1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart1.setGlobalValue(0);
        TimePeg tpStart2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart2.setGlobalValue(2);
        tmu1.setTimePeg("start", tpStart1);
        tmu1.setTimePeg("end", tpStart2);
        tmu2.setTimePeg("start", tpStart2);
        tmu1.setState(TimedPlanUnitState.LURKING);
        tmu2.setState(TimedPlanUnitState.LURKING);
        tmu1.setPriority(2);
        tmu2.setPriority(1);

        app.play(0);
        assertEquals(0, exList.size());
        assertTrue(tmu1.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(tmu2.getState() == TimedPlanUnitState.LURKING);
        assertTrue(fbList.size() == 1);
        assertTrue(fbList.get(0).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(0).getBMLId().equals("bml1"));
        assertTrue(fbList.get(0).getSyncId().equals("start"));
        assertTrue(fbList.get(0).getTime() == 0);

        app.play(2.1);
        assertTrue(exList.size() == 0);
        assertTrue(tmu1.getState() == TimedPlanUnitState.DONE);
        assertTrue(tmu2.getState() == TimedPlanUnitState.IN_EXEC);
        assertEquals(4, fbList.size());
        assertTrue(fbList.get(1).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(1).getBMLId().equals("bml1"));
        assertTrue(fbList.get(1).getSyncId().equals("relax"));
        assertEquals(2.1, fbList.get(1).getTime(), TIMING_PRECISION);
        
        assertTrue(fbList.get(2).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(2).getBMLId().equals("bml1"));
        assertTrue(fbList.get(2).getSyncId().equals("end"));
        assertEquals(2.1, fbList.get(2).getTime(), TIMING_PRECISION);
        
        assertTrue(fbList.get(3).getBehaviourId().equals("behaviour2"));
        assertTrue(fbList.get(3).getBMLId().equals("bml1"));
        assertTrue(fbList.get(3).getSyncId().equals("start"));
        assertEquals(2.1, fbList.get(3).getTime(), TIMING_PRECISION);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(0);
        verify(muMock2, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(anyDouble());
    }

    @Test
    public void testTransitionTMU() throws MUPlayException
    {
        // play a single transition motion unit, somewhere halfway execution,
        // check feedback, state transition, call (once) to setStartPose.
        TimedAnimationMotionUnit tmu = createTransitionTMU("behaviour1", "bml1", muMockTransition);
        planManager.addPlanUnit(tmu);

        stubKeyPositions(muMockTransition, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);

        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.setState(TimedPlanUnitState.LURKING);
        app.play(0.5);
        assertTrue(exList.size() == 0);
        assertTrue(tmu.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 1);
        assertTrue(fbList.get(0).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(0).getBMLId().equals("bml1"));
        assertTrue(fbList.get(0).getSyncId().equals("start"));
        assertEquals(0.5, fbList.get(0).getTime(), TIMING_PRECISION);

        app.play(0.6);
        assertTrue(exList.size() == 0);
        assertTrue(fbList.size() == 1);
        assertTrue(tmu.getState() == TimedPlanUnitState.IN_EXEC);
        verify(muMockTransition, atLeastOnce()).getKeyPosition("start");
        verify(muMockTransition, atLeastOnce()).getKeyPosition("end");
        verify(muMockTransition, times(1)).play(eq(0.5, TIMING_PRECISION));
        verify(muMockTransition, times(1)).startUnit(eq(0.5, TIMING_PRECISION));
    }

    @Test
    public void testTMU() throws MUPlayException
    {
        // play a single transition motion unit, somewhere halfway execution,
        // check feedback, state transition
        TimedAnimationMotionUnit tmu = createMotionUnit("behaviour1", "bml1", muMock1);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);

        planManager.addPlanUnit(tmu);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.setState(TimedPlanUnitState.LURKING);
        app.play(0.5);

        assertTrue(exList.size() == 0);
        assertTrue(tmu.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 1);
        assertTrue(fbList.get(0).getBehaviourId().equals("behaviour1"));
        assertTrue(fbList.get(0).getBMLId().equals("bml1"));
        assertTrue(fbList.get(0).getSyncId().equals("start"));
        assertEquals(0.5, fbList.get(0).getTime(), TIMING_PRECISION);
        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, atLeastOnce()).getKeyPosition("end");
        verify(muMock1, times(1)).play(eq(0.5, TIMING_PRECISION));
    }

    @Test
    public void testFailingTMU() throws MUPlayException
    {
        // play a single (failing) transition motion unit, somewhere halfway
        // execution, check feedback, state transition
        TimedAnimationMotionUnit tmu = createMotionUnit("behaviour1", "bml1", muMock1);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);

        planManager.addPlanUnit(tmu);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        doThrow(new MUPlayException("failure!", muMock1)).when(muMock1).play(eq(0.5, TIMING_PRECISION));

        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.setState(TimedPlanUnitState.LURKING);
        app.play(0.5);
        assertTrue(exList.size() == 1);
        assertEquals("bml1:behaviour1", exList.get(0).getId());
        assertEquals(TimedPlanUnitState.DONE, tmu.getState());
        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, atLeastOnce()).getKeyPosition("end");
        verify(muMock1, times(1)).play(eq(0.5, TIMING_PRECISION));
    }

    @Test
    public void testPriority() throws MUPlayException
    {
        TimedAnimationMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        when(muMock1.getKinematicJoints()).thenReturn(ImmutableSet.of("r_shoulder", "r_wrist"));
        when(muMock1.getPhysicalJoints()).thenReturn(new HashSet<String>());
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);
        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        tmu1.setTimePeg("start", tpStart);
        tmu1.setTimePeg("end", tpEnd);
        tmu1.setState(TimedPlanUnitState.LURKING);
        tmu1.setPriority(50);
        planManager.addPlanUnit(tmu1);

        TimedAnimationMotionUnit tmu2 = createMotionUnit("behaviour2", "bml1", muMock2);
        when(muMock2.getKinematicJoints()).thenReturn(ImmutableSet.of("r_shoulder"));
        when(muMock2.getPhysicalJoints()).thenReturn(new HashSet<String>());
        TimePeg tpStart2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart2.setGlobalValue(0);
        TimePeg tpEnd2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd2.setGlobalValue(2);
        stubKeyPositions(muMock2, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        tmu2.setTimePeg("start", tpStart2);
        tmu2.setTimePeg("end", tpEnd2);
        tmu2.setPriority(100);
        tmu2.setState(TimedPlanUnitState.LURKING);
        planManager.addPlanUnit(tmu2);

        TimedAnimationUnit mockTmu = mock(TimedAnimationUnit.class);
        when(mockTmu.getKinematicJoints()).thenReturn(ImmutableSet.of("r_wrist"));
        when(mockTmu.getPhysicalJoints()).thenReturn(new HashSet<String>());
        when(mockTmu.getState()).thenReturn(TimedPlanUnitState.LURKING);
        when(
                mockRestPose.createTransitionToRest(eq(NullFeedbackManager.getInstance()), eq(ImmutableSet.of("r_wrist")), anyDouble(),
                        eq("bml1"), eq("behaviour1"), eq(BMLBlockPeg.GLOBALPEG), eq(pegBoard))).thenReturn(mockTmu);
        app.play(0);
        assertThat(planManager.getPlanUnits(), containsInAnyOrder(tmu2, mockTmu));
    }
}
