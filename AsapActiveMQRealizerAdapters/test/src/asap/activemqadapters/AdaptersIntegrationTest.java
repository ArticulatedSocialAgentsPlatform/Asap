/*******************************************************************************
 *******************************************************************************/
package asap.activemqadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * Integration tests for the BMLToIpaaca and IpaacaToBML adapters.
 * Requires a running activemq daemon.
 * @author Herwin
 *
 */
public class AdaptersIntegrationTest
{
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    private BMLFeedbackListener mockFeedbackListener = mock(BMLFeedbackListener.class);
    private BMLRealizerToActiveMQAdapter bmlToIpaaca = new BMLRealizerToActiveMQAdapter();
    private ActiveMQToBMLRealizerAdapter ipaacaToBML;
    
    @Before
    public void setup() throws JMSException
    {
        ipaacaToBML = new ActiveMQToBMLRealizerAdapter(mockRealizerPort);
    }
    
    @Test(timeout=1000)
    public void testPerformBML() throws InterruptedException
    {
        bmlToIpaaca.performBML("bmltest");
        Thread.sleep(500);
        verify(mockRealizerPort).performBML("bmltest");
    }
    
    @Test(timeout=1000)
    public void testFeedback() throws InterruptedException
    {
        bmlToIpaaca.addListeners(mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback("bmlfeedback");
    }
}
