/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;


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
    public void testPerformBMLCharacter() throws InterruptedException
    {
        bmlToIpaaca = new BMLRealizerToIpaacaAdapter("Fred");
        ipaacaToBML = new IpaacaToBMLRealizerAdapter(mockRealizerPort,"Fred");
        bmlToIpaaca.performBML("bmltest");
        Thread.sleep(500);
        verify(mockRealizerPort).performBML("bmltest");
    }
    
    @Test
    public void testPerformBMLDifferentCharacter() throws InterruptedException
    {
        bmlToIpaaca = new BMLRealizerToIpaacaAdapter("Fred");
        ipaacaToBML = new IpaacaToBMLRealizerAdapter(mockRealizerPort,"Wilma");
        bmlToIpaaca.performBML("bmltest");
        Thread.sleep(500);
        verify(mockRealizerPort,times(0)).performBML("bmltest");
    }
    
    @Test
    public void testFeedback() throws InterruptedException
    {
        bmlToIpaaca.addListeners(mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback("bmlfeedback");
    }
    
    @Test
    public void testFeedbackCharacter() throws InterruptedException
    {
        bmlToIpaaca = new BMLRealizerToIpaacaAdapter("Fred");
        ipaacaToBML = new IpaacaToBMLRealizerAdapter(mockRealizerPort,"Fred");
        bmlToIpaaca.addListeners(mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback("bmlfeedback");
    }
    
    @Test
    public void testNoFeedbackDifferentCharacter() throws InterruptedException
    {
        bmlToIpaaca = new BMLRealizerToIpaacaAdapter("Fred");
        ipaacaToBML = new IpaacaToBMLRealizerAdapter(mockRealizerPort,"Wilma");
        bmlToIpaaca.addListeners(mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener, times(0)).feedback("bmlfeedback");
    }
}
