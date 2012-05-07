package hmi.bml.util;

import hmi.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.XMLBMLTSchedulingStartFeedback;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.bml.feedback.BMLExceptionListener;
import hmi.bml.feedback.BMLFeedback;
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
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to send out feedback to registered listeners
 * @author welberge
 *
 */
public class BMLFeedbackManager
{
    private static Logger logger = LoggerFactory.getLogger(BMLFeedbackManager.class.getName());

    /** This listener should receive feedback that comes in over the network connection */
    private List<BMLExceptionListener> bmlExceptionListeners = new ArrayList<BMLExceptionListener>();

    /** This listener should receive feedback that comes in over the network connection */
    private List<BMLWarningListener> bmlWarningListeners = new ArrayList<BMLWarningListener>();

    /** This listener should receive feedback that comes in over the network connection */
    private List<BMLFeedbackListener> bmlFeedbackListeners = new ArrayList<BMLFeedbackListener>();

    private List<BMLTSchedulingListener> bmlPlanningListeners = new ArrayList<BMLTSchedulingListener>();

    
    public void removeAllListeners()
    {
        bmlExceptionListeners.clear();
        bmlWarningListeners.clear();
        bmlFeedbackListeners.clear();
        bmlPlanningListeners.clear();
        
    }
    
    public void addListeners(BMLListener... bmlListeners)
    {
        for (BMLListener listener : bmlListeners)
        {
            if (listener instanceof BMLExceptionListener)
            {
                bmlExceptionListeners.add((BMLExceptionListener) listener);
            }
            if (listener instanceof BMLWarningListener)
            {
                bmlWarningListeners.add((BMLWarningListener) listener);
            }
            if (listener instanceof BMLFeedbackListener)
            {
                bmlFeedbackListeners.add((BMLFeedbackListener) listener);
            }
            if (listener instanceof BMLTSchedulingListener)
            {
                bmlPlanningListeners.add((BMLTSchedulingListener) listener);
            }
        }
    }

    /**
     * Sends feedback packed in an XML string
     * @param feedbackString
     */
    public void sendFeedback(String feedbackString)
    {
        XMLTokenizer tok = new XMLTokenizer(feedbackString);
        try
        {
            if (tok.atSTag(XMLBMLWarningFeedback.xmlTag()))
            {
                XMLBMLWarningFeedback feedback = new XMLBMLWarningFeedback();
                feedback.readXML(tok);
                sendWarning(feedback.getBMLWarningFeedback());
            }
            else if (tok.atSTag(XMLBMLExceptionFeedback.xmlTag()))
            {
                XMLBMLExceptionFeedback feedback = new XMLBMLExceptionFeedback();
                feedback.readXML(tok);
                sendException(feedback.getBMLExceptionFeedback());
            }
            else if (tok.atSTag(XMLBMLPerformanceStartFeedback.xmlTag()))
            {
                XMLBMLPerformanceStartFeedback feedback = new XMLBMLPerformanceStartFeedback();
                feedback.readXML(tok);
                sendPerformanceStartFeedback(feedback.getBMLPerformanceStartFeedback());
            }
            else if (tok.atSTag(XMLBMLPerformanceStopFeedback.xmlTag()))
            {
                XMLBMLPerformanceStopFeedback feedback = new XMLBMLPerformanceStopFeedback();
                feedback.readXML(tok);
                sendPerformanceStopFeedback(feedback.getBMLPerformanceStopFeedback());
            }
            else if (tok.atSTag(XMLBMLSyncPointProgressFeedback.xmlTag()))
            {
                XMLBMLSyncPointProgressFeedback feedback = new XMLBMLSyncPointProgressFeedback();
                feedback.readXML(tok);
                sendSyncProgress(feedback.getBMLSyncPointProgressFeedback());
            }
            else if (tok.atSTag(XMLBMLTSchedulingStartFeedback.xmlTag()))
            {
                XMLBMLTSchedulingStartFeedback feedback = new XMLBMLTSchedulingStartFeedback();
                feedback.readXML(tok);
                sendPlanningStart(feedback.getBMLTPlanningStartFeedback());
            }
            else if (tok.atSTag(XMLBMLTSchedulingFinishedFeedback.xmlTag()))
            {
                XMLBMLTSchedulingFinishedFeedback feedback = new XMLBMLTSchedulingFinishedFeedback();
                feedback.readXML(tok);
                sendPlanningFinished(feedback.getBMLTPlanningFinishedFeedback());
            }
            else
            { // give up when not a feedback tag...
                logger.warn("Failed to read feedback from server, unexpected feedback format. Ignoring message.");
            }
        }
        catch (IOException e)
        {
            logger.warn(
                    "Error reading feedback from message {}, error: {}. Disconnecting from server.",
                    feedbackString, e.getMessage());
        }
        catch (Exception e)
        {
            logger.warn("Error sending BMLErrorFeedback {} to listeners", feedbackString);
        }
    }

    public void sendFeedback(BMLFeedback feedback)
    {
        // send feedback to appropriate listeners...
        if (feedback instanceof BMLExceptionFeedback)
        {
            sendException((BMLExceptionFeedback) feedback);
        }
        else if (feedback instanceof BMLWarningFeedback)
        {
            sendWarning((BMLWarningFeedback) feedback);
        }
        else if (feedback instanceof BMLSyncPointProgressFeedback)
        {
            sendSyncProgress((BMLSyncPointProgressFeedback) feedback);
        }
        else if (feedback instanceof BMLPerformanceStopFeedback)
        {
            sendPerformanceStopFeedback((BMLPerformanceStopFeedback) feedback);
        }
        else if (feedback instanceof BMLPerformanceStartFeedback)
        {
            sendPerformanceStartFeedback((BMLPerformanceStartFeedback) feedback);
        }
        else if (feedback instanceof BMLTSchedulingStartFeedback)
        {
            sendPlanningStart((BMLTSchedulingStartFeedback)feedback);
        }
        else if (feedback instanceof BMLTSchedulingFinishedFeedback)
        {
            sendPlanningFinished((BMLTSchedulingFinishedFeedback)feedback);
        }
    }

    public void sendException(BMLExceptionFeedback bef)
    {
        for (BMLExceptionListener b : bmlExceptionListeners)
        {
            b.exception(bef);
        }
        if (bmlExceptionListeners.isEmpty())
        {
            logger.warn(
                    "BMLExceptionFeedback {} occured and was not captured by a BMLExceptionListener",
                    bef.toString());
        }
    }

    private void sendPlanningStart(BMLTSchedulingStartFeedback psf)
    {
        for (BMLTSchedulingListener b : bmlPlanningListeners)
        {
            b.schedulingStart(psf);
        }
    }

    private void sendPlanningFinished(BMLTSchedulingFinishedFeedback psf)
    {
        for (BMLTSchedulingListener b : bmlPlanningListeners)
        {
            b.schedulingFinished(psf);
        }
    }

    private void sendWarning(BMLWarningFeedback wfb)
    {
        for (BMLWarningListener b : bmlWarningListeners)
        {
            b.warn(wfb);
        }
    }

    private void sendSyncProgress(BMLSyncPointProgressFeedback spf)
    {
        for (BMLFeedbackListener f : bmlFeedbackListeners)
        {
            f.syncProgress(spf);
        }
    }

    private void sendPerformanceStartFeedback(BMLPerformanceStartFeedback psf)
    {
        for (BMLFeedbackListener f : bmlFeedbackListeners)
        {
            f.performanceStart(psf);
        }
    }

    private void sendPerformanceStopFeedback(BMLPerformanceStopFeedback psf)
    {
        for (BMLFeedbackListener f : bmlFeedbackListeners)
        {
            f.performanceStop(psf);
        }
    }
}
