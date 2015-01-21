/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import static asap.realizertestutil.util.TimePegUtil.createTimePeg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.BehaviorNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitSetupException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.FeedbackListUtils;

import com.google.common.collect.ImmutableSet;

/**
 * Unit testcases for TimedParameterValueChangeUnit
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class, BMLBlockManager.class})
public class TimedParameterValueChangeUnitTest extends AbstractTimedPlanUnitTest
{
    ParameterValueTrajectory mockTrajectory = mock(ParameterValueTrajectory.class);
    BMLScheduler mockScheduler = mock(BMLScheduler.class);

    private final static String TARGETBMLID = "bml1";
    private final static String TARGETBEHID = "speech1";
    private final static String TARGETPARAMID = "volume";
    private final static float INITIALVALUE = 0;
    private final static float TARGETVALUE = 100;
    private ParameterValueInfo paramValueInfo = new ParameterValueInfo(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE, TARGETVALUE);


    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
    
    @Override //behavior does not subside
    public void testSubsiding()
    {
        
    }
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedParameterValueChangeUnit tpvc = new TimedParameterValueChangeUnit(bfm, bbPeg, bmlId, id, mockScheduler, paramValueInfo,
                mockTrajectory);
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tpvc.setStartPeg(start);
        
        when(mockBlockManager.getSyncsPassed(TARGETBMLID, TARGETBEHID)).thenReturn(ImmutableSet.of("start"));
        return tpvc;
    }

    @Test
    public void testPlayAtTimeZero() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.getTimePeg("end").setGlobalValue(2);
        when(mockTrajectory.getValue(INITIALVALUE, TARGETVALUE, 0)).thenReturn(0f);

        tpu.start(0);
        tpu.play(0);
        verify(mockScheduler, times(2)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testPlayAtTimeOne() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.getTimePeg("end").setGlobalValue(2);
        when(mockTrajectory.getValue(INITIALVALUE, TARGETVALUE, 0.5f)).thenReturn(50f);

        tpu.start(0);
        tpu.play(1);
        assertThat(FeedbackListUtils.getSyncs(fbList), IsIterableContainingInOrder.contains("start"));
        verify(mockScheduler, times(1)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, 50);
    }

    @Test
    public void testPlayAtTimeZeroNoEndSet() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        when(mockTrajectory.getValue(INITIALVALUE, TARGETVALUE, 1)).thenReturn(0f);

        tpu.start(0);
        tpu.play(0);
        verify(mockScheduler, times(2)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testPlayAtTimeZeroNoInitialValue() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        paramValueInfo = new ParameterValueInfo(TARGETBMLID, TARGETBEHID, TARGETPARAMID, TARGETVALUE);
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.getTimePeg("end").setGlobalValue(2);

        when(mockScheduler.getFloatParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID)).thenReturn(1f);
        when(mockTrajectory.getValue(1, TARGETVALUE, 0)).thenReturn(0f);

        tpu.start(0);
        tpu.play(0);
        verify(mockScheduler, times(2)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testPlayAfterEnd() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);

        TimePeg start = new TimePeg(BMLBlockPeg.GLOBALPEG);
        start.setGlobalValue(0);
        tpu.setTimePeg("start", start);
        tpu.getTimePeg("end").setGlobalValue(2);

        tpu.start(3);
        tpu.play(3);
        assertThat(FeedbackListUtils.getSyncs(fbList),
                IsIterableContainingInOrder.contains("start", "end"));
        verify(mockScheduler, times(1)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testPlayBeforeEnd() throws TimedPlanUnitPlayException, ParameterException, BehaviorNotFoundException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);

        TimePeg start = new TimePeg(BMLBlockPeg.GLOBALPEG);
        start.setGlobalValue(0);
        tpu.setTimePeg("start", start);
        tpu.getTimePeg("end").setGlobalValue(2);

        tpu.start(0);
        tpu.play(1.999);
        assertThat(FeedbackListUtils.getSyncs(fbList), IsIterableContainingInOrder.contains("start"));
        verify(mockScheduler, times(2)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testStopAfterTargetFinished() throws TimedPlanUnitPlayException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);

        TimePeg start = new TimePeg(BMLBlockPeg.GLOBALPEG);
        start.setGlobalValue(0);
        tpu.setTimePeg("start", start);
        tpu.getTimePeg("end").setGlobalValue(2);

        when(mockBlockManager.getSyncsPassed(TARGETBMLID, TARGETBEHID)).thenReturn(
                ImmutableSet.of("start", "end"));
        tpu.start(0);
        tpu.play(3);
        assertThat(FeedbackListUtils.getSyncs(fbList),
                IsIterableContainingInOrder.contains("start", "end"));
    }

    @Test(expected = TimedPlanUnitPlayException.class)
    public void testPlayUnknownParameter() throws ParameterException, BehaviorNotFoundException, TimedPlanUnitPlayException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.getTimePeg("start").setGlobalValue(0);
        tpu.getTimePeg("end").setGlobalValue(2);

        doThrow(new PlanUnitFloatParameterNotFoundException("bml1", "id1", "param1")).when(mockScheduler).setParameterValue(TARGETBMLID,
                TARGETBEHID, TARGETPARAMID, INITIALVALUE);

        tpu.start(0);
        tpu.play(0);
        verify(mockScheduler, times(1)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test(expected = TimedPlanUnitPlayException.class)
    public void testPlayUnknownBehavior() throws ParameterException, BehaviorNotFoundException, TimedPlanUnitPlayException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setState(TimedPlanUnitState.LURKING);
        tpu.getTimePeg("start").setGlobalValue(0);
        tpu.getTimePeg("end").setGlobalValue(2);

        doThrow(new BehaviorNotFoundException("bml1", "id1")).when(mockScheduler).setParameterValue(TARGETBMLID, TARGETBEHID,
                TARGETPARAMID, INITIALVALUE);

        tpu.start(0);
        tpu.play(0);
        verify(mockScheduler, times(1)).setParameterValue(TARGETBMLID, TARGETBEHID, TARGETPARAMID, INITIALVALUE);
    }

    @Test
    public void testInvalid() throws TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setTimePeg("end", createTimePeg(0));
        tpu.setTimePeg("start", createTimePeg(2));
        assertFalse(tpu.hasValidTiming());
    }

    @Test
    public void testValid() throws TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.setTimePeg("end", createTimePeg(2));
        tpu.setTimePeg("start", createTimePeg(0));
        assertTrue(tpu.hasValidTiming());
    }

    @Test
    @Override //no stroke sync anymore
    public void testSetStrokePeg()
    {
        
    }
    
    @Test
    public void testSetValueOnFinishedBehavior() throws ParameterException, BehaviorNotFoundException, TimedPlanUnitPlayException, TimedPlanUnitSetupException
    {
        TimedPlanUnit tpu = setupPlanUnitWithListener(BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        when(mockBlockManager.getSyncsPassed(TARGETBMLID, TARGETBEHID)).thenReturn(
                ImmutableSet.of("start", "end"));
        doThrow(new BehaviorNotFoundException("bml1", "id1")).when(mockScheduler).setParameterValue(TARGETBMLID, TARGETBEHID,
                TARGETPARAMID, INITIALVALUE);
        tpu.setTimePeg("end", createTimePeg(2));
        tpu.setTimePeg("start", createTimePeg(0));
        tpu.setState(TimedPlanUnitState.IN_EXEC);
        tpu.play(0);
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
}
