package asap.realizer.bridge;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import org.slf4j.Logger;

import saiba.bml.feedback.BMLWarningFeedback;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLListener;
import asap.bml.feedback.BMLPredictionListener;
import asap.bml.feedback.BMLWarningListener;
import hmi.util.Clock;

/**
 * A LoggingRealizerBridge can be put between two bridges to log their communication.
 * It logs feedback from its output bridge and logs BML requests from its input bridge.
 * On construction, it is provided with a Elckerlyc.SchedulingClock; the timestamps from this clock
 * are logged together with the requests.
 * @author welberge
 * @author reidsma
 */
public class LoggingRealizerBridge implements RealizerPort, BMLFeedbackListener, BMLWarningListener, BMLPredictionListener
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
    public void blockProgress(BMLBlockProgressFeedback psf)
    {
        if (logFeedback) logger.info(psf.toXMLString());
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        if (logFeedback) logger.info(spp.toXMLString());
    }

    @Override
    public void warn(BMLWarningFeedback bw)
    {
        if (logFeedback) logger.info(bw.toXMLString());
    }

    @Override
    public void prediction(BMLPredictionFeedback bpf)
    {
        if (logFeedback) logger.info(bpf.toXMLString());
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
}
