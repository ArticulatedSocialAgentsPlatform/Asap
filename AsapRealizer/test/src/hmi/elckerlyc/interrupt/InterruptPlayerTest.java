package hmi.elckerlyc.interrupt;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import hmi.elckerlyc.DefaultPlayer;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
