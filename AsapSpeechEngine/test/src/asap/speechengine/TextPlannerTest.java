package asap.speechengine;

import static org.mockito.Mockito.mock;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.BMLBlockManager;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
