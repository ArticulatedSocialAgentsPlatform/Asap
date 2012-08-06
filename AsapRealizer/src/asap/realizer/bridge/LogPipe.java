package asap.realizer.bridge;

import org.slf4j.Logger;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLListener;
import asap.bml.feedback.BMLPredictionListener;
import asap.bml.feedback.BMLWarningListener;
import hmi.util.Clock;


/**
 * A LogPipe can be put between two bridges to log their communication. 
 * It logs feedback from its output bridge and logs BML requests from its input bridge.
 * On construction, it is provided with a Elckerlyc.SchedulingClock; the timestamps from this clock
 * are logged together with the requests.
 * THIS IS THE NEWER VERSION OF LOGGINGREALIZERBRIDGE
 * @author welberge
 * @author reidsma
 */
public class LogPipe implements RealizerPort, BMLFeedbackListener, BMLWarningListener, BMLPredictionListener
{
    private final Logger requestLogger;
    private final Logger feedbackLogger;
    private final RealizerPort outputBridge;
    private final Clock schedulingClock;
    private final boolean logRequests;
    private final boolean logFeedback;
    
    public LogPipe(Logger rl, Logger fl, RealizerPort outBridge, Clock clock)
    {
        this.requestLogger = rl;
        this.feedbackLogger = fl;
        this.outputBridge = outBridge;
        this.schedulingClock = clock;
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
    public void prediction(BMLPredictionFeedback bpf)
    {
        if (logFeedback)feedbackLogger.info(bpf.toXMLString());
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
        requestLogger.info("<entry name=\"{}\" time=\"{}\">", requestLogger.getName(), schedulingClock.getMediaSeconds());
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
