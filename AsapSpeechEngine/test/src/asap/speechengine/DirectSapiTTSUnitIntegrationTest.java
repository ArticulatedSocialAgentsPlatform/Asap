/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import hmi.util.OS;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.util.TimePegUtil;
import asap.sapittsbinding.SAPITTSBinding;

/**
 * Integration tests for the sapi direct TTS.
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class DirectSapiTTSUnitIntegrationTest extends AbstractTTSUnitTest
{
    private SAPITTSBinding sapiBinding;
    @Before
    public void setup() throws SpeechUnitPlanningException
    {
        Assume.assumeTrue(OS.equalsOS(OS.WINDOWS));
        sapiBinding = new SAPITTSBinding();
    }
    
    @After
    public void cleanup()
    {
        if(sapiBinding!=null)
        {
            sapiBinding.cleanup();
        }
    }
    
    protected TimedTTSUnit getTTSUnit(BMLBlockPeg bbPeg, String text, String id, String bmlId)
    {
        return new TimedDirectTTSUnit(fbManager, bbPeg, text, bmlId, id, sapiBinding, SpeechBehaviour.class);
    }

    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedDirectTTSUnit ttsU = new TimedDirectTTSUnit(bfm, bbPeg, "hello world", bmlId, id, sapiBinding, SpeechBehaviour.class);
        ttsU.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return ttsU;
    }    
}
