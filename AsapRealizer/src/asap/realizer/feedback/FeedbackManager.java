/*******************************************************************************
 *******************************************************************************/
package asap.realizer.feedback;

import java.util.List;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizerport.BMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Interface for classes that manage and keep track of BML feedback
 * @author Herwin
 *
 */
public interface FeedbackManager
{
    void addFeedbackListener(BMLFeedbackListener fb);
    
    void feedback(BMLSyncPointProgressFeedback fb);
    
    /**
     * Send a list of feedback (in list-order) to the BMLFeedbackListeners. The listeners will
     * receive all feedbacks in the list before any subsequent feedback is sent using the feedback
     * functions.
     */
    void feedback(List<BMLSyncPointProgressFeedback> fbs);
    
    void removeAllFeedbackListeners();
    
    void removeFeedbackListener(BMLFeedbackListener fb);
    
    ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId);
    
    void blockProgress(BMLBlockProgressFeedback psf);
    
    void puException(TimedPlanUnit timedMU, String message, double time);
    
    void prediction(BMLPredictionFeedback bpsf);    
    
    void warn(BMLWarningFeedback w);
}
