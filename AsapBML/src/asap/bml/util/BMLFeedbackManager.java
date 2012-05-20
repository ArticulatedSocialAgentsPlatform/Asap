package asap.bml.util;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.feedback.BMLWarningFeedback;

import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLListener;
import asap.bml.feedback.BMLPredictionListener;
import asap.bml.feedback.BMLWarningListener;

/**
 * Utility class to send out feedback to registered listeners
 * @author welberge
 * 
 */
public class BMLFeedbackManager
{
    private static Logger logger = LoggerFactory.getLogger(BMLFeedbackManager.class.getName());

    /** This listener should receive feedback that comes in over the network connection */
    private List<BMLWarningListener> bmlWarningListeners = new ArrayList<BMLWarningListener>();

    /** This listener should receive feedback that comes in over the network connection */
    private List<BMLFeedbackListener> bmlFeedbackListeners = new ArrayList<BMLFeedbackListener>();

    private List<BMLPredictionListener> bmlPredictionListeners = new ArrayList<BMLPredictionListener>();

    public void removeAllListeners()
    {
        bmlWarningListeners.clear();
        bmlFeedbackListeners.clear();
        bmlPredictionListeners.clear();

    }

    public void addListeners(BMLListener... bmlListeners)
    {
        for (BMLListener listener : bmlListeners)
        {
            if (listener instanceof BMLWarningListener)
            {
                bmlWarningListeners.add((BMLWarningListener) listener);
            }
            if (listener instanceof BMLFeedbackListener)
            {
                bmlFeedbackListeners.add((BMLFeedbackListener) listener);
            }
            if (listener instanceof BMLPredictionListener)
            {
                bmlPredictionListeners.add((BMLPredictionListener) listener);
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
            if (tok.atSTag(BMLWarningFeedback.xmlTag()))
            {
                BMLWarningFeedback feedback = new BMLWarningFeedback();
                feedback.readXML(tok);
                sendWarning(feedback);
            }         
            else if (tok.atSTag(BMLBlockProgressFeedback.xmlTag()))
            {
                BMLBlockProgressFeedback feedback = new BMLBlockProgressFeedback();
                feedback.readXML(tok);
                sendBlockProgress(feedback);
            }
            else if (tok.atSTag(BMLSyncPointProgressFeedback.xmlTag()))
            {
                BMLSyncPointProgressFeedback feedback = new BMLSyncPointProgressFeedback();
                feedback.readXML(tok);
                sendSyncProgress(feedback);
            }
            else if (tok.atSTag(BMLPredictionFeedback.xmlTag()))
            {
                BMLPredictionFeedback feedback = new BMLPredictionFeedback();
                feedback.readXML(tok);
                sendPrediction(feedback);
            }
            else
            { // give up when not a feedback tag...
                logger.warn("Failed to read feedback from server, unexpected feedback format. Ignoring message.");
            }
        }
        catch (IOException e)
        {
            logger.warn("Error reading feedback from message {}, error: {}. Disconnecting from server.", feedbackString, e.getMessage());
        }
        catch (Exception e)
        {
            logger.warn("Error sending BMLErrorFeedback {} to listeners", feedbackString);
        }
    }

    public void sendFeedback(BMLWarningFeedback feedback)
    {
        sendWarning(feedback);
    }

    public void sendFeedback(BMLFeedback feedback)
    {
        // send feedback to appropriate listeners...
        if (feedback instanceof BMLSyncPointProgressFeedback)
        {
            sendSyncProgress((BMLSyncPointProgressFeedback) feedback);
        }
        else if (feedback instanceof BMLBlockProgressFeedback)
        {
            sendBlockProgress((BMLBlockProgressFeedback)feedback);
        }
        else if (feedback instanceof BMLPredictionFeedback)
        {
            sendPrediction((BMLPredictionFeedback) feedback);
        }        
    }

    private void sendPrediction(BMLPredictionFeedback bpf)
    {
        for (BMLPredictionListener b : bmlPredictionListeners)
        {
            b.prediction(bpf);
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

    private void sendBlockProgress(BMLBlockProgressFeedback psf)
    {
        for (BMLFeedbackListener f : bmlFeedbackListeners)
        {
            f.blockProgress(psf);
        }
    }    
}
