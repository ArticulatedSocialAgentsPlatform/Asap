package asap.tcpipadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * Integration tests for the BMLToTCPIP and TCPIPToBML adapters.
 * @author Herwin
 *
 */
public class AdaptersIntegrationTest
{
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    private BMLFeedbackListener mockFeedbackListener = mock(BMLFeedbackListener.class);
    private BMLRealizerToTCPIPAdapter bmlToTCPIP = new BMLRealizerToTCPIPAdapter();
    private TCPIPToBMLRealizerAdapter tcpIpToBML = new TCPIPToBMLRealizerAdapter(mockRealizerPort,6500,6501);
    
    @After
    public void tearDown()
    {
        bmlToTCPIP.shutdown();
        tcpIpToBML.shutdown();   
        while(bmlToTCPIP.isConnected()){}
        while(tcpIpToBML.isConnectedToClient()){}
    }
    
    @Test
    public void testPerformBML() throws InterruptedException
    {
        String bmlString = "<bml id=\"bml1\"></bml>";
        bmlToTCPIP.connect(new ServerInfo("localhost",6500,6501));
        bmlToTCPIP.performBML(bmlString);        
        Thread.sleep(500);
        verify(mockRealizerPort).performBML(bmlString);        
    }
    
    @Test
    public void testFeedback() throws InterruptedException
    {
        bmlToTCPIP.connect(new ServerInfo("localhost",6500,6501));
        bmlToTCPIP.addListeners(mockFeedbackListener);
        String fbString = "<blockProgress id=\"bml1:end\" globalTime=\"15\" characterId=\"doctor\"></blockProgress>";
        Thread.sleep(500);        
        tcpIpToBML.feedback(fbString);
        Thread.sleep(500);
        verify(mockFeedbackListener).feedback(fbString);
    }
}
