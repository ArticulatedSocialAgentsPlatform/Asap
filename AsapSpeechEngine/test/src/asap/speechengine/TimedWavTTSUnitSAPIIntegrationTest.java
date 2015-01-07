/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import hmi.util.OS;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.scheduler.BMLBlockManager;
import asap.sapittsbinding.SAPITTSBinding;

/**
 * Unit test cases for TimedWavTTSUnits that use a SAPITTSBinding
 * @author hvanwelbergen
 *
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedWavTTSUnitSAPIIntegrationTest extends AbstractTimedWavTTSUnitTest
{
    private SAPITTSBinding sapiBinding; 
    @Before
    public void setup()
    {
        Assume.assumeTrue(OS.equalsOS(OS.WINDOWS));
        super.setup();        
        sapiBinding = new SAPITTSBinding();
        ttsBinding = sapiBinding;
        
    }
    
    @After
    public void tearDown()
    {
        if(sapiBinding!=null)
        {
            sapiBinding.cleanup();
        }
        super.tearDown();        
    }    
}
