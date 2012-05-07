package hmi.elckerlyc.planunit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import hmi.bml.BMLGestureSync;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.bml.feedback.ListFeedbackListener;
import static hmi.testutil.bml.feedback.FeedbackAsserts.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.powermock.api.mockito.PowerMockito.*;


/**
 * Generic testcases for TimedPlanUnits.
 * The TimedPlanUnit is to be constructed in the setupPlanUnit function and must have non-zero duration.
 * @author welberge
 * 
 */
public abstract class AbstractTimedPlanUnitTest
{
    protected abstract TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime);

    protected List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
    protected BMLBlockManager mockBlockManager = mock(BMLBlockManager.class);
    protected FeedbackManager fbManager = new FeedbackManagerImpl(mockBlockManager, "character1");

    protected TimedPlanUnit setupPlanUnitWithListener(BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedPlanUnit tpu = setupPlanUnit(fbManager, bbPeg, id, bmlId, startTime);
        fbManager.addFeedbackListener(new ListFeedbackListener(fbList));
        return tpu;
    }

    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.SUBSIDING, tpu.getState());
    }
    
    @Test
    public void testFeedback()
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        BMLSyncPointProgressFeedback expected = new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", 0, 0);
        tpu.feedback(expected);
        assertOneFeedback(expected, fbList);
    }

    @Test
    public void testSetup()
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        assertEquals(0, tpu.getStartTime(), 0.0001f);
        // assertEquals(TimePeg.VALUE_UNKNOWN,tpu.getEndTime(),0.0001f);
        assertEquals(TimedPlanUnitState.IN_PREP, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testPlayInPrep() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.play(0);
        assertEquals(TimedPlanUnitState.IN_PREP, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStopInPrep() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.stop(0);
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStartInPrep() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.start(0);
        assertEquals(TimedPlanUnitState.IN_PREP, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testPlayInPending() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.PENDING);
        tpu.play(0);
        assertEquals(TimedPlanUnitState.PENDING, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStartInPending() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.PENDING);
        tpu.start(0);
        assertEquals(TimedPlanUnitState.PENDING, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStopInPending() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.PENDING);
        tpu.stop(0);
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testPlayInLurking() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.play(0);
        assertEquals(TimedPlanUnitState.LURKING, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStopInLurking() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.stop(0);
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
        assertThat(fbList, hasSize(0));
    }

    @Test
    public void testStartInLurking() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, tpu.getState());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartThenPlayInLurking() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(0);
        tpu.play(0);
        assertThat(tpu.getState(),
                anyOf(equalTo(TimedPlanUnitState.SUBSIDING), equalTo(TimedPlanUnitState.IN_EXEC), equalTo(TimedPlanUnitState.DONE)));
        BMLSyncPointProgressFeedback expected = new BMLSyncPointProgressFeedback("bml1", "id1", "start", 0, 0);
        assertEqualSyncPointProgress(expected, fbList.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayInExec() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(0);
        tpu.play(0);
        tpu.play(0);
        assertThat(tpu.getState(), anyOf(equalTo(TimedPlanUnitState.SUBSIDING), equalTo(TimedPlanUnitState.IN_EXEC), equalTo(TimedPlanUnitState.DONE)));
    }

    @Test
    public void testStartInExec() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.IN_EXEC);
        tpu.start(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, tpu.getState());
        assertThat(fbList, hasSize(0));// do not resend start feedback
    }

    @Test
    public void testSetStrokePeg()
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        TimePeg strokePeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        strokePeg.setGlobalValue(2);
        tpu.setTimePeg(BMLGestureSync.STROKE.getId(), strokePeg);
        assertEquals(2f, tpu.getTime(BMLGestureSync.STROKE.getId()), 0.0001f);
    }

    @Test
    public void testSubsiding() throws TimedPlanUnitPlayException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        TimePeg relaxPeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        relaxPeg.setGlobalValue(2);
        relaxPeg.setAbsoluteTime(true);
        tpu.setTimePeg(BMLGestureSync.RELAX.toString(), relaxPeg);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.start(0);
        tpu.play(2.1);
        assertSubsiding(tpu);        
    }
}
