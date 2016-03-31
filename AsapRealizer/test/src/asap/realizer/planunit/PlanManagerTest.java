/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import asap.realizer.BehaviorNotFoundException;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.TimePegAlreadySetException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit testcases for the PlanManager
 * @author welberge
 */
public class PlanManagerTest
{
    private PlanManager<TimedPlanUnit> planManager = new PlanManager<TimedPlanUnit>();
    private TimedPlanUnit mockTimedPlanUnit1 = mock(TimedPlanUnit.class);
    private TimedPlanUnit mockTimedPlanUnit2 = mock(TimedPlanUnit.class);
    private TimedPlanUnit mockTimedPlanUnit3 = mock(TimedPlanUnit.class);
    
    private static final double TIME_PRECISION = 0.0001;
    @Before
    public void setup()
    {
        when(mockTimedPlanUnit1.getId()).thenReturn("planunit1");
        when(mockTimedPlanUnit2.getId()).thenReturn("planunit2");
        when(mockTimedPlanUnit3.getId()).thenReturn("planunit3");
        when(mockTimedPlanUnit1.getBMLId()).thenReturn("bml1");
        when(mockTimedPlanUnit2.getBMLId()).thenReturn("bml1");
        when(mockTimedPlanUnit3.getBMLId()).thenReturn("bml2");

        when(mockTimedPlanUnit1.getEndTime()).thenReturn(3d);

        planManager.addPlanUnit(mockTimedPlanUnit1);
        planManager.addPlanUnit(mockTimedPlanUnit2);
        planManager.addPlanUnit(mockTimedPlanUnit3);
    }

    @Test
    public void testGetPlanUnits()
    {
        assertThat(planManager.getPlanUnits(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(mockTimedPlanUnit1, mockTimedPlanUnit2, mockTimedPlanUnit3));
    }
    
    @Test
    public void testGetPlanUnitsByBMLId()
    {
        assertThat(planManager.getPlanUnits("bml1"),
                IsIterableContainingInAnyOrder.containsInAnyOrder(mockTimedPlanUnit1, mockTimedPlanUnit2));
    }

    @Test
    public void testSetup()
    {
        assertEquals(3, planManager.getNumberOfPlanUnits());
        assertThat(planManager.getBehaviours("bml1"), IsIterableContainingInAnyOrder.containsInAnyOrder("planunit1", "planunit2"));
        assertThat(planManager.getBehaviours("bml2"), IsIterableContainingInAnyOrder.containsInAnyOrder("planunit3"));
    }

    @Test
    public void testInterruptPlanUnit() throws TimedPlanUnitPlayException
    {
        when(mockTimedPlanUnit1.isPlaying()).thenReturn(true);
        planManager.stopPlanUnit("bml1", "planunit1", 0.2);
        assertThat(planManager.getBehaviours("bml1"), IsIterableContainingInAnyOrder.containsInAnyOrder("planunit2"));
        verify(mockTimedPlanUnit1, times(1)).stop(0.2);
    }

    @Test
    public void testGetEndTime()
    {
        assertEquals(3.0, planManager.getEndTime("bml1", "planunit1"), TIME_PRECISION);
    }

    @Test
    public void testGetEndTimeUnknownBehaviour()
    {
        assertEquals(TimePeg.VALUE_UNKNOWN, planManager.getEndTime("bml1", "unknown"), TIME_PRECISION);
    }

    @Test
    public void testSetBMLBlockState()
    {
        planManager.setBMLBlockState("bml1", TimedPlanUnitState.DONE);
        verify(mockTimedPlanUnit1, times(1)).setState(TimedPlanUnitState.DONE);
        verify(mockTimedPlanUnit2, times(1)).setState(TimedPlanUnitState.DONE);
        verify(mockTimedPlanUnit3, times(0)).setState(TimedPlanUnitState.DONE);
    }

    @Test
    public void testRemoveAllPlanUnits() throws TimedPlanUnitPlayException
    {
        planManager.removeAllPlanUnits(1d);
        assertEquals(0, planManager.getNumberOfPlanUnits());
    }

    @Test
    public void testRemoveSomePlayingPlanUnits() throws TimedPlanUnitPlayException
    {
        when(mockTimedPlanUnit1.isPlaying()).thenReturn(true);
        when(mockTimedPlanUnit2.isPlaying()).thenReturn(true);
        planManager.removeAllPlanUnits(1d);
        verify(mockTimedPlanUnit1, times(1)).stop(1d);
        verify(mockTimedPlanUnit2, times(1)).stop(1d);
        verify(mockTimedPlanUnit3, times(0)).stop(1d);
        assertEquals(0, planManager.getNumberOfPlanUnits());
    }

    @Test
    public void testContainsBehaviour()
    {
        assertTrue(planManager.containsBehaviour("bml1", "planunit1"));
        assertFalse(planManager.containsBehaviour("bml2", "planunit1"));
    }

    @Test
    public void testInterruptBehaviourBlock() throws TimedPlanUnitPlayException
    {
        when(mockTimedPlanUnit1.isPlaying()).thenReturn(true);
        planManager.interruptBehaviourBlock("bml1", 1d);

        verify(mockTimedPlanUnit1, times(1)).interrupt(1d);        
    }
    
    @Test
    public void testStopBehaviourBlock() throws TimedPlanUnitPlayException
    {
        when(mockTimedPlanUnit1.isPlaying()).thenReturn(true);
        planManager.stopBehaviourBlock("bml1", 1d);

        verify(mockTimedPlanUnit1, times(1)).stop(1d);
        assertEquals(1, planManager.getNumberOfPlanUnits());
    }

    @Test(expected = BehaviorNotFoundException.class)
    public void testGetParamaterValueOnNonExistingBehaviour() throws ParameterException, BehaviorNotFoundException
    {
        planManager.getParameterValue("bml1", "unknown", "param");
    }

    @Test(expected = BehaviorNotFoundException.class)
    public void testGetFloatParamaterValueOnNonExistingBehaviour() throws BehaviorNotFoundException, ParameterException
    {
        planManager.getFloatParameterValue("bml1", "unknown", "param");
    }

    @Test
    public void testGetParameterValue() throws ParameterException, BehaviorNotFoundException
    {
        when(mockTimedPlanUnit1.getParameterValue("param1")).thenReturn("dummy");
        assertEquals("dummy", planManager.getParameterValue("bml1", "planunit1", "param1"));
    }

    @Test
    public void testGetFloatParameterValue() throws BehaviorNotFoundException, ParameterException
    {
        when(mockTimedPlanUnit1.getFloatParameterValue("param1")).thenReturn(123f);
        assertEquals(123f, planManager.getFloatParameterValue("bml1", "planunit1", "param1"), 0.001f);
    }

    @Test
    public void testCreateOffsetPegLinkedToBefore() throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        TimePeg startPeg = TimePegUtil.createTimePeg(1);
        TimePeg endPeg = TimePegUtil.createTimePeg(2);
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getRelativeTime("start")).thenReturn(0d);
        when(mockTimedPlanUnit1.getRelativeTime("stroke")).thenReturn(0.2d);
        when(mockTimedPlanUnit1.getRelativeTime("end")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("start")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("stroke")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTime("end")).thenReturn(2d);
        when(mockTimedPlanUnit1.getTimePeg("start")).thenReturn(startPeg);
        when(mockTimedPlanUnit1.getTimePeg("end")).thenReturn(endPeg);
        when(mockTimedPlanUnit1.getBMLBlockPeg()).thenReturn(BMLBlockPeg.GLOBALPEG);

        OffsetPeg peg = planManager.createOffsetPeg("bml1", "planunit1", "stroke");
        assertEquals(startPeg, peg.getLink());
        assertEquals(1.2, peg.getGlobalValue(), 0.001f);
    }

    @Test
    public void testCreateOffsetPegLinkedToAfter() throws BehaviorNotFoundException, SyncPointNotFoundException, TimePegAlreadySetException
    {
        TimePeg startPeg = TimePegUtil.createTimePeg(1);
        TimePeg endPeg = TimePegUtil.createTimePeg(2);
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getRelativeTime("start")).thenReturn(0d);
        when(mockTimedPlanUnit1.getRelativeTime("stroke")).thenReturn(0.8d);
        when(mockTimedPlanUnit1.getRelativeTime("end")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("start")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("stroke")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTime("end")).thenReturn(2d);
        when(mockTimedPlanUnit1.getTimePeg("start")).thenReturn(startPeg);
        when(mockTimedPlanUnit1.getTimePeg("end")).thenReturn(endPeg);
        when(mockTimedPlanUnit1.getBMLBlockPeg()).thenReturn(BMLBlockPeg.GLOBALPEG);

        OffsetPeg peg = planManager.createOffsetPeg("bml1", "planunit1", "stroke");
        assertEquals(endPeg, peg.getLink());
        assertEquals(1.8, peg.getGlobalValue(), 0.001f);
    }

    @Test
    public void testCreateOffsetPegNoBefore() throws BehaviorNotFoundException, SyncPointNotFoundException, TimePegAlreadySetException
    {
        TimePeg endPeg = TimePegUtil.createTimePeg(2);
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getRelativeTime("start")).thenReturn(0d);
        when(mockTimedPlanUnit1.getRelativeTime("stroke")).thenReturn(0.2d);
        when(mockTimedPlanUnit1.getRelativeTime("end")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("start")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTime("stroke")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTime("end")).thenReturn(2d);
        when(mockTimedPlanUnit1.getTimePeg("start")).thenReturn(null);
        when(mockTimedPlanUnit1.getTimePeg("end")).thenReturn(endPeg);
        when(mockTimedPlanUnit1.getBMLBlockPeg()).thenReturn(BMLBlockPeg.GLOBALPEG);

        OffsetPeg peg = planManager.createOffsetPeg("bml1", "planunit1", "stroke");
        assertEquals(endPeg, peg.getLink());
        assertEquals(2d, peg.getGlobalValue(), 0.001f);
    }

    @Test
    public void testCreateOffsetPegNoAfter() throws BehaviorNotFoundException, SyncPointNotFoundException, TimePegAlreadySetException
    {
        TimePeg startPeg = TimePegUtil.createTimePeg(1);
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getRelativeTime("start")).thenReturn(0d);
        when(mockTimedPlanUnit1.getRelativeTime("stroke")).thenReturn(0.8d);
        when(mockTimedPlanUnit1.getRelativeTime("end")).thenReturn(1d);
        when(mockTimedPlanUnit1.getTime("start")).thenReturn(0d);
        when(mockTimedPlanUnit1.getTime("stroke")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTime("end")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getTimePeg("start")).thenReturn(startPeg);
        when(mockTimedPlanUnit1.getTimePeg("end")).thenReturn(null);
        when(mockTimedPlanUnit1.getBMLBlockPeg()).thenReturn(BMLBlockPeg.GLOBALPEG);
       

        OffsetPeg peg = planManager.createOffsetPeg("bml1", "planunit1", "stroke");
        assertEquals(startPeg, peg.getLink());
        assertEquals(1d, peg.getGlobalValue(), 0.001f);
    }

    @Test(expected = BehaviorNotFoundException.class)
    public void testCreateOffsetPegForNonExistingBehaviour() throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        planManager.createOffsetPeg("bml1", "unknown", "sync1");
    }

    @Test(expected = SyncPointNotFoundException.class)
    public void testCreateOffsetPegForNonExistingSync() throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        when(mockTimedPlanUnit1.getTime("unknown")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit1.getRelativeTime("unknown")).thenThrow(new SyncPointNotFoundException("bml1", "planunit1", "unkown"));
        planManager.createOffsetPeg("bml1", "planunit1", "unknown");
    }

    @Test(expected = TimePegAlreadySetException.class)
    public void testCreateOffsetPegForAlreadySetPeg() throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        when(mockTimedPlanUnit1.getTime("start")).thenReturn(1d);
        planManager.createOffsetPeg("bml1", "planunit1", "start");
    }

    @Test
    public void testGetBlockEndTime()
    {
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit2.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getEndTime()).thenReturn(1d);
        when(mockTimedPlanUnit2.getEndTime()).thenReturn(2d);
        assertEquals(2d, planManager.getEndTime("bml1"), TIME_PRECISION);
    }

    @Test
    public void testGetBlockEndTimeWithPersistentBehaviour()
    {
        when(mockTimedPlanUnit1.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit2.getAvailableSyncs()).thenReturn(Arrays.asList("start", "stroke", "end"));
        when(mockTimedPlanUnit1.getEndTime()).thenReturn(1d);
        when(mockTimedPlanUnit2.getEndTime()).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockTimedPlanUnit2.getTime("start")).thenReturn(1d);
        when(mockTimedPlanUnit2.getTime("stroke")).thenReturn(2d);
        when(mockTimedPlanUnit2.getTime("end")).thenReturn(TimePeg.VALUE_UNKNOWN);
        assertEquals(2d, planManager.getEndTime("bml1"),TIME_PRECISION);
    }

    @Test
    public void removeFinishedPlanUnits()
    {
        when(mockTimedPlanUnit1.isDone()).thenReturn(true);
        when(mockTimedPlanUnit2.isDone()).thenReturn(false);
        when(mockTimedPlanUnit3.isDone()).thenReturn(true);
        planManager.removeFinishedPlanUnits();
        assertEquals(1, planManager.getNumberOfPlanUnits());
    }
}
