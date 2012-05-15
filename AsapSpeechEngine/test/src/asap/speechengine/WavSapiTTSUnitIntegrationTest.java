package asap.speechengine;

import hmi.audioenvironment.LWJGLJoalSoundManager;
import hmi.audioenvironment.SoundManager;
import saiba.bml.core.SpeechBehaviour;
import hmi.util.OS;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.scheduler.BMLBlockManager;
import asap.speechengine.SpeechUnitPlanningException;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.TimedWavTTSUnit;
import asap.speechengine.ttsbinding.SAPITTSBinding;

/**
 * Integration tests for TimedWavTTSUnit connect to a sapibinding 
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class WavSapiTTSUnitIntegrationTest extends AbstractTTSUnitTest
{
    private SAPITTSBinding sapiBinding;    
    private static SoundManager soundManager = new LWJGLJoalSoundManager();
    @BeforeClass
    public static void setupClass()
    {
        soundManager.init();        
    }
    
    @AfterClass
    public static void tearDownClass()
    {
        soundManager.shutdown();        
    }
    
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
        return new TimedWavTTSUnit(fbManager,soundManager, bbPeg, text, "voice1", bmlId, id, sapiBinding, SpeechBehaviour.class);
    }
}
