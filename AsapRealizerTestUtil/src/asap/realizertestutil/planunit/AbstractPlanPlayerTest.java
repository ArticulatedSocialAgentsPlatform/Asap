/*******************************************************************************
 *******************************************************************************/
package asap.realizertestutil.planunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Generic testcases for PlanPlayer implementations
 * @author Herwin
 */
public abstract class AbstractPlanPlayerTest
{
    TimedPlanUnit mockPlanUnit1 = mock(TimedPlanUnit.class);
    TimedPlanUnit mockPlanUnit2 = mock(TimedPlanUnit.class);
    TimedPlanUnit mockPlanUnit3 = mock(TimedPlanUnit.class);

    protected FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    protected PlanManager<TimedPlanUnit> planManager = new PlanManager<TimedPlanUnit>();

    protected abstract PlanPlayer createPlanPlayer();

    @Test
    public void testInterruptUnit()
    {
        PlanPlayer pp = createPlanPlayer();

        when(mockPlanUnit1.getEndTime()).thenReturn(3d);
        when(mockPlanUnit1.getBMLId()).thenReturn("bml1");
        when(mockPlanUnit1.getId()).thenReturn("beh1");
        when(mockPlanUnit2.getStartTime()).thenReturn(1d);
        when(mockPlanUnit2.getEndTime()).thenReturn(4d);
        when(mockPlanUnit2.getBMLId()).thenReturn("bml1");
        when(mockPlanUnit2.getId()).thenReturn("beh2");
        when(mockPlanUnit3.getStartTime()).thenReturn(0d);
        when(mockPlanUnit3.getEndTime()).thenReturn(5d);
        when(mockPlanUnit3.getBMLId()).thenReturn("bml2");
        when(mockPlanUnit3.getId()).thenReturn("beh3");

        planManager.addPlanUnit(mockPlanUnit1);
        planManager.addPlanUnit(mockPlanUnit2);
        planManager.addPlanUnit(mockPlanUnit3);
        assertEquals(3, planManager.getNumberOfPlanUnits());

        pp.stopBehaviourBlock("bml1", 1.0);
        assertEquals(1, planManager.getNumberOfPlanUnits());
    }

    @Test
    public void testRemoveWhenDone() throws InterruptedException
    {
        PlanPlayer pp = createPlanPlayer();
        StubPlanUnit spu1 = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", 0, 2);
        StubPlanUnit spu2 = new StubPlanUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh2", 1, 6);
        spu1.setState(TimedPlanUnitState.LURKING);
        spu2.setState(TimedPlanUnitState.LURKING);

        planManager.addPlanUnit(spu1);
        planManager.addPlanUnit(spu2);
        pp.play(1);
        assertEquals(2, planManager.getNumberOfPlanUnits());

        pp.play(3);
        Thread.sleep(400);
        assertEquals(1, planManager.getNumberOfPlanUnits());
    }
}
