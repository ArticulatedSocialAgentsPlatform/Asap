/*******************************************************************************
 *******************************************************************************/
package asap.realizer.bridge;

import hmi.util.Clock;

import org.slf4j.Logger;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * A LoggingRealizerBridge can be put between two bridges to log their communication.
 * It logs feedback from its output bridge and logs BML requests from its input bridge.
 * On construction, it is provided with a Elckerlyc.SchedulingClock; the timestamps from this clock
 * are logged together with the requests.
 * @author welberge
 * @author reidsma
 */
public class LoggingRealizerBridge implements RealizerPort, BMLFeedbackListener
{
    private final Logger logger;
    private final RealizerPort outputBridge;
    private final Clock schedulingClock;
    private final boolean logRequests;
    private final boolean logFeedback;

    public LoggingRealizerBridge(Logger logger, RealizerPort outBridge, Clock clock)
    {
        this(logger, outBridge, clock, true, true);
    }

    public LoggingRealizerBridge(Logger logger, RealizerPort outBridge, Clock clock, boolean logR, boolean logF)
    {
        this.logger = logger;
        this.outputBridge = outBridge;
        this.schedulingClock = clock;
        this.logRequests = logR;
        this.logFeedback = logF;
        outputBridge.addListeners(this);
    }

    @Override
    public void addListeners(BMLFeedbackListener... listeners)
    {
        outputBridge.addListeners(listeners);
    }

    @Override
    public void performBML(String bmlString)
    {
        if (logRequests)
        {
            logger.info("<entry name=\"{}\" time=\"{}\">", logger.getName(), schedulingClock.getMediaSeconds());
            logger.info(bmlString);
            logger.info("</entry>");
        }
        outputBridge.performBML(bmlString);
    }

    @Override
    public void removeAllListeners()
    {
        outputBridge.removeAllListeners();
    }
    
    @Override
    public void removeListener(BMLFeedbackListener l)
    {
        outputBridge.removeListener(l);        
    }  

    @Override
    public void feedback(String feedback)
    {
        if (logFeedback) logger.info(feedback);        
    }

     
}
