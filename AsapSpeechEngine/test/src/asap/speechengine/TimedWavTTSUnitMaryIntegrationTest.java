package asap.speechengine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.speechengine.ttsbinding.MaryTTSBinding;


import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.tts.util.NullPhonemeToVisemeMapping;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
/**
 * Unit test cases for TimedWavTTSUnits that use a MaryTTSBinding
 * @author hvanwelbergen
 */
public class TimedWavTTSUnitMaryIntegrationTest extends AbstractTimedWavTTSUnitTest
{
    @Before
    public void setup()
    {
        super.setup();
        ttsBinding = new MaryTTSBinding(System.getProperty("shared.project.root")+
                "/HmiResource/MARYTTS",
                new NullPhonemeToVisemeMapping());
        
    }    
    
    @After
    public void tearDown()
    {
        super.tearDown();
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    } 
}
