package asap.speechengine;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLBlockManager;
import asap.speechengine.TextOutput;
import asap.speechengine.TextPlanner;
import asap.speechengine.TimedTextSpeechUnit;

/**
 * Unit test cases for SpeechBehaviour planning using a TextPlanner 
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TextPlannerTest extends AbstractSpeechPlannerTest<TimedTextSpeechUnit>
{
    private TextOutput mockTextOutput = mock(TextOutput.class);
    private PlanManager<TimedTextSpeechUnit> planManager = new PlanManager<TimedTextSpeechUnit>();
    
    @Before
    public void setup()
    {
        speechPlanner = new TextPlanner(mockFeedbackManager,mockTextOutput, planManager);
        super.setup();
    }    
}
