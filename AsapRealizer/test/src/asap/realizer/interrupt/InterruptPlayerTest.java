/*******************************************************************************
 *******************************************************************************/
package asap.realizer.interrupt;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.DefaultPlayer;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Unit testcases for the playback of interrupt units, using a DefaultPlayer
 * @author Herwin
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TimedInterruptUnit.class)
public class InterruptPlayerTest
{
    private TimedInterruptUnit mockInterruptUnit = mock(TimedInterruptUnit.class);    
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private PlanManager<TimedInterruptUnit> planManager = new PlanManager<TimedInterruptUnit>();
    
    @Test
    public void testPlay() throws TimedPlanUnitPlayException
    {
        DefaultPlayer player = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedInterruptUnit>(mockFeedbackManager,planManager));
        planManager.addPlanUnit(mockInterruptUnit);
        when(mockInterruptUnit.getState()).thenReturn(TimedPlanUnitState.IN_EXEC);
        when(mockInterruptUnit.isPlaying()).thenReturn(true);
        player.play(0);
        verify(mockInterruptUnit,times(1)).play(0);
    }
}
