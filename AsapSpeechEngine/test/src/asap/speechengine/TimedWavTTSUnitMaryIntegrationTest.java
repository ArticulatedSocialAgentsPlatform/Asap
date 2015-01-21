/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import hmi.tts.util.NullPhonemeToVisemeMapping;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.maryttsbinding.MaryTTSBinding;
import asap.realizer.scheduler.BMLBlockManager;

/**
 * Unit test cases for TimedWavTTSUnits that use a MaryTTSBinding
 * @author hvanwelbergen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedWavTTSUnitMaryIntegrationTest extends AbstractTimedWavTTSUnitTest
{
    @Before
    public void setup()
    {
        super.setup();
        ttsBinding = new MaryTTSBinding(System.getProperty("shared.project.root")+"/Asap/AsapSpeechEngine/test/lib/MARYTTS",
                new NullPhonemeToVisemeMapping());
        
    }    
    
    @After
    public void tearDown()
    {
        super.tearDown();
    }   
}
