package asap.realizer.feedback;

import java.util.List;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLPredictionListener;
import asap.bml.feedback.BMLWarningListener;
import asap.realizer.planunit.TimedPlanUnit;

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
    
    void feedback(List<BMLSyncPointProgressFeedback> fbs);
    
    void removeAllFeedbackListeners();
    
    void removeFeedbackListener(BMLFeedbackListener fb);
    
    ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId);
    
    void blockProgress(BMLBlockProgressFeedback psf);
    
    void puException(TimedPlanUnit timedMU, String message, double time);
    
    void addPredictionListener(BMLPredictionListener p);
    
    void removeAllPredictionListeners();
    
    void prediction(BMLPredictionFeedback bpsf);    
    
    /**
     * Generates a feedback message on only the start time of a BML block
     */
    void blockStartPrediction(String bmlId, double time);
    
    void addWarningListener(BMLWarningListener ws);
    
    void removeAllWarningListeners();
    
    void removeWarningListener(BMLWarningListener ws);
    
    void warn(BMLWarningFeedback w);
}
