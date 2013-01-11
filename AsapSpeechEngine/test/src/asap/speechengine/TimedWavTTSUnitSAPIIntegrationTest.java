package asap.speechengine;

import hmi.util.OS;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.scheduler.BMLBlockManager;
import asap.sapittsbinding.SAPITTSBinding;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
/**
 * Unit test cases for TimedWavTTSUnits that use a SAPITTSBinding
 * @author hvanwelbergen
 *
 */
public class TimedWavTTSUnitSAPIIntegrationTest extends AbstractTimedWavTTSUnitTest
{

    @Before
    public void setup()
    {
        Assume.assumeTrue(OS.equalsOS(OS.WINDOWS));
        super.setup();        
        ttsBinding = new SAPITTSBinding();
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
