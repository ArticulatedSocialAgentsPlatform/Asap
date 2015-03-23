/*******************************************************************************
 *******************************************************************************/
package asap.rsbadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * Integration tests for the BMLToIpaaca and IpaacaToBML adapters.
 * Requires a running spread daemon.
 * @author Herwin
 */
public class AsaptersIntegrationTest
{
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    private BMLFeedbackListener mockFeedbackListener = mock(BMLFeedbackListener.class);
    private BMLRealizerToRsbAdapter bmlToRsb = new BMLRealizerToRsbAdapter();
    private RsbToBMLRealizerAdapter rsbToBML = new RsbToBMLRealizerAdapter(mockRealizerPort);
    
    @After
    public void tearDown()
    {
        bmlToRsb.close();
        rsbToBML.close();
    }
    
    @Test
    public void testPerformBML() throws InterruptedException
    {
        bmlToRsb.performBML("bmltest");
        Thread.sleep(500);
        verify(mockRealizerPort).performBML("bmltest");
    }
    
    @Test
    public void testFeedback() throws InterruptedException
    {
        bmlToRsb.addListeners(mockFeedbackListener);
        rsbToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback("bmlfeedback");
    }
}
