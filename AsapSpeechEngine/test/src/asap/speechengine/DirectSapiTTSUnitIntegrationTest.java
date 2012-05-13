package asap.speechengine;

import saiba.bml.core.SpeechBehaviour;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.util.OS;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.speechengine.SpeechUnitPlanningException;
import asap.speechengine.TimedDirectTTSUnit;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.ttsbinding.SAPITTSBinding;

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
        super.setup();
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
}
