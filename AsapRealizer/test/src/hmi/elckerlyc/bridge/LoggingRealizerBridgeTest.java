package hmi.elckerlyc.bridge;

import static hmi.testutil.bml.feedback.FeedbackAsserts.assertOneFeedback;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import hmi.bml.bridge.RealizerPort;
import hmi.bml.feedback.BMLFeedback;
import hmi.bml.feedback.BMLListener;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.bml.feedback.XMLBMLSyncPointProgressFeedback;
import hmi.bml.util.BMLFeedbackManager;
import asap.utils.SchedulingClock;
import hmi.bml.feedback.ListFeedbackListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.*;
/**
 * Unit tests for the LoggingRealizerBridge
 * @author Herwin
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggerFactory.class)
public class LoggingRealizerBridgeTest
{
    private LoggingRealizerBridge logBridge;
    private StubInputBridge inputBridge;
    private StubOutputBridge outputBridge;
    private List<BMLSyncPointProgressFeedback> fbList;
    private SchedulingClock mockSchedulingClock = mock(SchedulingClock.class);
    Logger mockLogger = mock(Logger.class);
    
    
    private static class StubOutputBridge implements RealizerPort
    {
        private BMLFeedbackManager feedbackManager = new BMLFeedbackManager();
        private String performedBML;
        
        public void sendFeedback(BMLFeedback fb)
        {
            feedbackManager.sendFeedback(fb);
        }
        
        public String getPerformedBML()
        {
            return performedBML;
        }

        @Override
        public void addListeners(BMLListener... bmlListeners)
        {
            feedbackManager.addListeners(bmlListeners);
        }

        @Override
        public void performBML(String bmlString)
        {
            performedBML = bmlString;
        }

        @Override
        public void removeAllListeners()
        {
            feedbackManager.removeAllListeners();
        }        
    }
    
    private static class StubInputBridge implements RealizerPort
    {
        private RealizerPort outBridge;
        public StubInputBridge(RealizerPort outBridge)
        {
            this.outBridge = outBridge;
        }
        
        @Override
        public void addListeners(BMLListener... listeners)
        {
            outBridge.addListeners(listeners);            
        }

        @Override
        public void performBML(String bmlString)
        {
            outBridge.performBML(bmlString);            
        }

        @Override
        public void removeAllListeners()
        {
            outBridge.removeAllListeners();            
        }        
    }
    
    
    @Before
    public void setup()
    {
        PowerMockito.mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger(anyString())).thenReturn(mockLogger);
        
        outputBridge = new StubOutputBridge();
        logBridge = new LoggingRealizerBridge(mockLogger, outputBridge, mockSchedulingClock);
        inputBridge = new StubInputBridge(logBridge);
        fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        inputBridge.addListeners(new ListFeedbackListener(fbList));        
    }
    
    @Test
    public void testLogBMLRequest()
    {
        final String bml = "<bml id=\"bml1\"/>";
        inputBridge.performBML(bml);
        assertEquals(bml,outputBridge.getPerformedBML());
        
        // XXX: this is a bit too specific, any .info function could be called 
        //(also one with different arguments)
        verify(mockLogger,times(1)).info(contains(bml));        
    }
    
    @Test
    public void testLogFeedback()
    {
        BMLSyncPointProgressFeedback spp = new BMLSyncPointProgressFeedback("bml1", "beh1", "sync1", 0, 0);
        outputBridge.sendFeedback(spp);
        assertOneFeedback(spp,fbList);
        
        final String logString = new XMLBMLSyncPointProgressFeedback(spp).toXMLString();
        verify(mockLogger,times(1)).info(logString);        
    }
}
