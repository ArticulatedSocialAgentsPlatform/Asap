package asap.ipaacaadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;

/**
 * Integration tests for the BMLToIpaaca and IpaacaToBML adapters.
 * Requires a running spread daemon.
 * @author Herwin
 *
 */
public class AdaptersIntegrationTest
{
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    private BMLFeedbackListener mockFeedbackListener = mock(BMLFeedbackListener.class);
    private BMLRealizerToIpaacaAdapter bmlToIpaaca = new BMLRealizerToIpaacaAdapter();
    private IpaacaToBMLRealizerAdapter ipaacaToBML = new IpaacaToBMLRealizerAdapter(mockRealizerPort);
    
    @After
    public void tearDown()
    {
        bmlToIpaaca.close();
        ipaacaToBML.close();
    }
    
    @Test
    public void testPerformBML() throws InterruptedException
    {
        bmlToIpaaca.performBML("bmltest");
        Thread.sleep(500);
        verify(mockRealizerPort).performBML("bmltest");
    }
    
    @Test
    public void testFeedback() throws InterruptedException
    {
        bmlToIpaaca.addListeners(mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback("bmlfeedback");
    }
}
