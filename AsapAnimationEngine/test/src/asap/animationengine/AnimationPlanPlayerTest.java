package asap.animationengine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.planunit.DefaultTimedPlanUnitPlayer;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.bml.feedback.ListBMLExceptionListener;
import hmi.bml.feedback.ListFeedbackListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.transitions.TransitionMU;
import asap.animationengine.transitions.TransitionTMU;

import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;
import static hmi.elckerlyc.util.KeyPositionMocker.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;


/**
 * Unit test cases for a PlanPlayer using TimedMotionUnits
 * 
 * @author Herwin
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class AnimationPlanPlayerTest
{
    private MotionUnit muMock1 = mock(MotionUnit.class);
    private MotionUnit muMock2 = mock(MotionUnit.class);
    private TransitionMU muMockTransition = mock(TransitionMU.class);

    private List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
    private List<BMLExceptionFeedback> exList = new ArrayList<BMLExceptionFeedback>();
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private RestPose mockRestPose = mock(RestPose.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");

    PlanManager<TimedMotionUnit> planManager = new PlanManager<TimedMotionUnit>();

    private ListFeedbackListener fbl;
    private AnimationPlanPlayer app;   
    

    @Before
    public void setup()
    {
        fbl = new ListFeedbackListener(fbList);
        app = new AnimationPlanPlayer(mockRestPose,fbManager, planManager, new DefaultTimedPlanUnitPlayer());
        app.addExceptionListener(new ListBMLExceptionListener(exList));
        fbManager.addFeedbackListener(fbl);
    }

    private TimedMotionUnit createMotionUnit(String behId, String bmlId, MotionUnit mu)
    {
        return new TimedMotionUnit(fbManager, BMLBlockPeg.GLOBALPEG, bmlId, behId, mu);
    }

    private TransitionTMU createTransitionTMU(String behId, String bmlId, TransitionMU mu)
    {
        return new TransitionTMU(fbManager, BMLBlockPeg.GLOBALPEG, bmlId, behId, mu);
    }

    @Test
    public void testPlayTmu() throws MUPlayException
    {
        // Playing one motion unit at t=0, checking for state transition,
        // feedback calls, no warnings
        TimedMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        planManager.addPlanUnit(tmu1);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tmu1.setTimePeg("start", tpStart);
        tmu1.setState(TimedPlanUnitState.LURKING);
        app.play(0);

        assertEquals(TimedPlanUnitState.IN_EXEC, tmu1.getState());
        assertEquals(1, fbList.size());
        assertEquals("behaviour1", fbList.get(0).behaviorId);
        assertEquals("bml1", fbList.get(0).bmlId);
        assertEquals("start", fbList.get(0).syncId);
        assertEquals(0, fbList.get(0).timeStamp, 0.00001f);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(0);
    }

    @Test
    public void testPlayTmuPastEnd() throws MUPlayException
    {
        // Playing one motion unit, 2 calls to play: during valid play time and
        // after end time. Checking for state transitions, feedback calls, no
        // warnings
        TimedMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
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
        assertTrue(fbList.get(0).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(0).bmlId.equals("bml1"));
        assertTrue(fbList.get(0).syncId.equals("start"));
        assertTrue(fbList.get(0).timeStamp == 0.99);
        app.play(2);
        assertTrue(tmu1.getState() == TimedPlanUnitState.DONE);
        assertTrue(fbList.size() == 2);
        assertTrue(exList.size() == 0);
        assertTrue(fbList.get(0).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(0).bmlId.equals("bml1"));
        assertTrue(fbList.get(0).syncId.equals("start"));
        assertTrue(fbList.get(0).timeStamp == 0.99);
        assertTrue(fbList.get(1).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(1).bmlId.equals("bml1"));
        assertTrue(fbList.get(1).syncId.equals("end"));
        assertTrue(fbList.get(1).timeStamp == 2);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(anyDouble());
    }

    @Test
    public void testPlay2Tmu() throws MUPlayException
    {
        // Plays a succesion of two motion units. Checking for state
        // transitions, feedback calls, no warnings.
        TimedMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        planManager.addPlanUnit(tmu1);

        TimedMotionUnit tmu2 = createMotionUnit("behaviour2", "bml1", muMock2);
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
        assertEquals(0,exList.size());
        assertTrue(tmu1.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(tmu2.getState() == TimedPlanUnitState.LURKING);
        assertTrue(fbList.size() == 1);
        assertTrue(fbList.get(0).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(0).bmlId.equals("bml1"));
        assertTrue(fbList.get(0).syncId.equals("start"));
        assertTrue(fbList.get(0).timeStamp == 0);

        app.play(2.1);
        assertTrue(exList.size() == 0);
        assertTrue(tmu1.getState() == TimedPlanUnitState.DONE);
        assertTrue(tmu2.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 3);
        assertTrue(fbList.get(1).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(1).bmlId.equals("bml1"));
        assertTrue(fbList.get(1).syncId.equals("end"));
        assertTrue(fbList.get(1).timeStamp == 2.1);
        assertTrue(fbList.get(2).behaviorId.equals("behaviour2"));
        assertTrue(fbList.get(2).bmlId.equals("bml1"));
        assertTrue(fbList.get(2).syncId.equals("start"));
        assertTrue(fbList.get(2).timeStamp == 2.1);

        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(0);
        verify(muMock2, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, times(1)).play(anyDouble());
    }

    @Test
    public void testTransitionTMU()
    {
        // play a single transition motion unit, somewhere halfway execution,
        // check feedback, state transition, call (once) to setStartPose.
        TransitionTMU tmu = createTransitionTMU("behaviour1", "bml1", muMockTransition);
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
        assertTrue(fbList.get(0).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(0).bmlId.equals("bml1"));
        assertTrue(fbList.get(0).syncId.equals("start"));
        assertTrue(fbList.get(0).timeStamp == 0.5);

        app.play(0.6);
        assertTrue(exList.size() == 0);
        assertTrue(fbList.size() == 1);
        assertTrue(tmu.getState() == TimedPlanUnitState.IN_EXEC);
        verify(muMockTransition, atLeastOnce()).getKeyPosition("start");
        verify(muMockTransition, atLeastOnce()).getKeyPosition("end");
        verify(muMockTransition, times(1)).play(eq(0.5, 0.01));
        verify(muMockTransition, times(1)).setStartPose();
    }

    @Test
    public void testTMU() throws MUPlayException
    {
        // play a single transition motion unit, somewhere halfway execution,
        // check feedback, state transition
        TimedMotionUnit tmu = createMotionUnit("behaviour1", "bml1", muMock1);
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
        assertTrue(fbList.get(0).behaviorId.equals("behaviour1"));
        assertTrue(fbList.get(0).bmlId.equals("bml1"));
        assertTrue(fbList.get(0).syncId.equals("start"));
        assertTrue(fbList.get(0).timeStamp == 0.5);
        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, atLeastOnce()).getKeyPosition("end");
        verify(muMock1, times(1)).play(eq(0.5, 0.01));
    }

    @Test
    public void testFailingTMU() throws MUPlayException
    {
        // play a single (failing) transition motion unit, somewhere halfway
        // execution, check feedback, state transition
        TimedMotionUnit tmu = createMotionUnit("behaviour1", "bml1", muMock1);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);

        planManager.addPlanUnit(tmu);

        stubKeyPositions(muMock1, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        doThrow(new MUPlayException("failure!", muMock1)).when(muMock1).play(eq(0.5, 0.01));

        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.setState(TimedPlanUnitState.LURKING);
        app.play(0.5);
        assertTrue(exList.size() == 1);
        assertTrue(exList.get(0).failedBehaviours.contains(tmu.getId()));
        assertEquals(TimedPlanUnitState.DONE, tmu.getState());
        verify(muMock1, atLeastOnce()).getKeyPosition("start");
        verify(muMock1, atLeastOnce()).getKeyPosition("end");
        verify(muMock1, times(1)).play(eq(0.5, 0.01));
    }
    
    @Test
    public void testPriority() throws MUPlayException
    {
        TimedMotionUnit tmu1 = createMotionUnit("behaviour1", "bml1", muMock1);
        when(muMock1.getKinematicJoints()).thenReturn(ImmutableSet.of("r_shoulder","r_wrist"));
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
        
        TimedMotionUnit tmu2 = createMotionUnit("behaviour2", "bml1", muMock2);
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
        
        TimedMotionUnit mockTmu = mock(TimedMotionUnit.class);
        when(mockTmu.getKinematicJoints()).thenReturn(ImmutableSet.of("r_wrist"));
        when(mockTmu.getPhysicalJoints()).thenReturn(new HashSet<String>());
        when(mockTmu.getState()).thenReturn(TimedPlanUnitState.LURKING);
        when(mockRestPose.createTransitionToRest(eq(ImmutableSet.of("r_wrist")), anyDouble(), 
                eq("bml1"), eq("behaviour1-cleanup"), eq(BMLBlockPeg.GLOBALPEG))).thenReturn(mockTmu);
        app.play(0);
        assertThat(planManager.getPlanUnits(),containsInAnyOrder(tmu2,mockTmu));
    }
}
