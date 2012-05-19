package asap.realizer.bridge;

import org.slf4j.Logger;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.bridge.RealizerPort;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import asap.bml.ext.bmlt.feedback.XMLBMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.XMLBMLTSchedulingStartFeedback;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLListener;
import asap.bml.feedback.BMLWarningListener;
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
public class LogPipe implements RealizerPort, BMLFeedbackListener, BMLWarningListener, BMLTSchedulingListener
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
    public void blockProgress(BMLBlockProgressFeedback psf)
    {
        if (logFeedback)feedbackLogger.info(psf.toXMLString());
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        if (logFeedback)feedbackLogger.info(spp.toXMLString());
    }

    @Override
    public void warn(BMLWarningFeedback bw)
    {
        if (logFeedback)feedbackLogger.info(bw.toXMLString());
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
