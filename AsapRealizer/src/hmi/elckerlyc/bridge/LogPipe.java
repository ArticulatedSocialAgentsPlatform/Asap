package hmi.elckerlyc.bridge;

import hmi.bml.bridge.RealizerPort;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingStartFeedback;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.bml.feedback.BMLExceptionListener;
import hmi.bml.feedback.BMLFeedbackListener;
import hmi.bml.feedback.BMLListener;
import hmi.bml.feedback.BMLPerformanceStartFeedback;
import hmi.bml.feedback.BMLPerformanceStopFeedback;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.bml.feedback.BMLWarningFeedback;
import hmi.bml.feedback.BMLWarningListener;
import hmi.bml.feedback.XMLBMLExceptionFeedback;
import hmi.bml.feedback.XMLBMLPerformanceStartFeedback;
import hmi.bml.feedback.XMLBMLPerformanceStopFeedback;
import hmi.bml.feedback.XMLBMLSyncPointProgressFeedback;
import hmi.bml.feedback.XMLBMLWarningFeedback;

import org.slf4j.Logger;

import asap.utils.SchedulingClock;


/**
 * A LogPipe can be put between two bridges to log their communication. 
 * It logs feedback from its output bridge and logs BML requests from its input bridge.
 * On construction, it is provided with a Elckerlyc.SchedulingClock; the timestamps from this clock
 * are logged together with the requests.
 * THIS IS THE NEWER VERSION OF LOGGINGREALIZERBRIDGE
 * @author welberge
 * @author reidsma
 */
public class LogPipe implements RealizerPort, BMLFeedbackListener, BMLExceptionListener,
        BMLWarningListener, BMLTSchedulingListener
{
    private final Logger requestLogger;
    private final Logger feedbackLogger;
    private final RealizerPort outputBridge;
    private final SchedulingClock clock;
    private final boolean logRequests;
    private final boolean logFeedback;
    
    public LogPipe(Logger rl, Logger fl, RealizerPort outBridge, SchedulingClock clock)
    {
        this.requestLogger = rl;
        this.feedbackLogger = fl;
        this.outputBridge = outBridge;
        this.clock = clock;
        this.logRequests = rl!=null;
        this.logFeedback = fl!=null;
        outputBridge.addListeners(this);        
    }

    @Override
    public void performanceStop(BMLPerformanceStopFeedback psf)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLPerformanceStopFeedback(psf).toXMLString());
    }

    @Override
    public void performanceStart(BMLPerformanceStartFeedback psf)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLPerformanceStartFeedback(psf).toXMLString());
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLSyncPointProgressFeedback(spp).toXMLString());
    }

    @Override
    public void exception(BMLExceptionFeedback be)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLExceptionFeedback(be).toXMLString());
    }

    @Override
    public void warn(BMLWarningFeedback bw)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLWarningFeedback(bw).toXMLString());
    }

    @Override
    public void schedulingFinished(BMLTSchedulingFinishedFeedback pff)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLTSchedulingFinishedFeedback(pff).toXMLString());
    }

    @Override
    public void schedulingStart(BMLTSchedulingStartFeedback psf)
    {
        if (logFeedback)feedbackLogger.info(new XMLBMLTSchedulingStartFeedback(psf).toXMLString());
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
        requestLogger.info("<entry name=\"{}\" time=\"{}\">", requestLogger.getName(), clock.getTime());
        requestLogger.info(bmlString);
        requestLogger.info("</entry>");
      }
      outputBridge.performBML(bmlString);
    }

    @Override
    public void removeAllListeners()
    {
        outputBridge.removeAllListeners();        
        outputBridge.addListeners(this); //logger itself needs to remain connected :)
    }
}
