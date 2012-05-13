package hmi.elckerlyc.bridge;

import saiba.bml.bridge.RealizerPort;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingStartFeedback;
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
