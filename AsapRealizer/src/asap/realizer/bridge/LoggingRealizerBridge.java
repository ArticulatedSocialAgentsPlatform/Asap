package asap.realizer.bridge;

import saiba.bml.bridge.RealizerPort;
import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.BMLExceptionListener;
import saiba.bml.feedback.BMLFeedbackListener;
import saiba.bml.feedback.BMLListener;
import saiba.bml.feedback.BMLPerformanceStartFeedback;
import saiba.bml.feedback.BMLPerformanceStopFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.feedback.BMLWarningListener;
import saiba.bml.feedback.XMLBMLExceptionFeedback;
import saiba.bml.feedback.XMLBMLPerformanceStartFeedback;
import saiba.bml.feedback.XMLBMLPerformanceStopFeedback;
import saiba.bml.feedback.XMLBMLSyncPointProgressFeedback;
import saiba.bml.feedback.XMLBMLWarningFeedback;

import org.slf4j.Logger;

import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import asap.bml.ext.bmlt.feedback.XMLBMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.XMLBMLTSchedulingStartFeedback;
import asap.utils.SchedulingClock;


/**
 * A LoggingRealizerBridge can be put between two bridges to log their communication. 
 * It logs feedback from its output bridge and logs BML requests from its input bridge.
 * On construction, it is provided with a Elckerlyc.SchedulingClock; the timestamps from this clock
 * are logged together with the requests.
 * @author welberge
 * @author reidsma
 */
public class LoggingRealizerBridge implements RealizerPort, BMLFeedbackListener, BMLExceptionListener,
        BMLWarningListener, BMLTSchedulingListener
{
    private final Logger logger;
    private final RealizerPort outputBridge;
    private final SchedulingClock clock;
    private final boolean logRequests;
    private final boolean logFeedback;
    
    public LoggingRealizerBridge(Logger logger, RealizerPort outBridge, SchedulingClock clock)
    {
      this(logger, outBridge, clock, true, true);
    }
    public LoggingRealizerBridge(Logger logger, RealizerPort outBridge, SchedulingClock clock, boolean logR, boolean logF)
    {
        this.logger = logger;
        this.outputBridge = outBridge;
        this.clock = clock;
        this.logRequests = logR;
        this.logFeedback = logF;
        outputBridge.addListeners(this);        
    }

    @Override
    public void performanceStop(BMLPerformanceStopFeedback psf)
    {
        if (logFeedback)logger.info(new XMLBMLPerformanceStopFeedback(psf).toXMLString());
    }

    @Override
    public void performanceStart(BMLPerformanceStartFeedback psf)
    {
        if (logFeedback)logger.info(new XMLBMLPerformanceStartFeedback(psf).toXMLString());
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        if (logFeedback)logger.info(new XMLBMLSyncPointProgressFeedback(spp).toXMLString());
    }

    @Override
    public void exception(BMLExceptionFeedback be)
    {
        if (logFeedback)logger.info(new XMLBMLExceptionFeedback(be).toXMLString());
    }

    @Override
    public void warn(BMLWarningFeedback bw)
    {
        if (logFeedback)logger.info(new XMLBMLWarningFeedback(bw).toXMLString());
    }

    @Override
    public void schedulingFinished(BMLTSchedulingFinishedFeedback pff)
    {
        if (logFeedback)logger.info(new XMLBMLTSchedulingFinishedFeedback(pff).toXMLString());
    }

    @Override
    public void schedulingStart(BMLTSchedulingStartFeedback psf)
    {
        if (logFeedback)logger.info(new XMLBMLTSchedulingStartFeedback(psf).toXMLString());
    }

    @Override
    public void addListeners(BMLListener... listeners)
    {
        outputBridge.addListeners(listeners);
    }

    @Override
    public void performBML(String bmlString)
    {
      if (logRequests)
      {
        logger.info("<entry name=\"{}\" time=\"{}\">", logger.getName(), clock.getTime());
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
}
