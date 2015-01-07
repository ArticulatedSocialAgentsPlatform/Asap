/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLBlockManager;

/**
 * Unit test cases for SpeechBehaviour planning using a TextPlanner 
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TextPlannerTest extends AbstractTextPlannerTest<TimedSpeechTextUnit>
{
    private TextOutput mockTextOutput = mock(TextOutput.class);
    private PlanManager<TimedSpeechTextUnit> planManager = new PlanManager<TimedSpeechTextUnit>();
    
    @Before
    public void setup()
    {
        speechPlanner = new TextPlanner(mockFeedbackManager,mockTextOutput, planManager);
        super.setup();
    }    
}
