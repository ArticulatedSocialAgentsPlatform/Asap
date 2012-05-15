package asap.realizer.feedback;

import hmi.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.BMLExceptionListener;
import saiba.bml.feedback.BMLFeedbackListener;
import saiba.bml.feedback.BMLPerformanceStartFeedback;
import saiba.bml.feedback.BMLPerformanceStopFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.feedback.BMLWarningListener;

import java.util.List;

import asap.realizer.planunit.TimedPlanUnit;

import com.google.common.collect.ImmutableSet;

public interface FeedbackManager
{
    void addFeedbackListener(BMLFeedbackListener fb);
    
    void feedback(BMLSyncPointProgressFeedback fb);
    
    void feedback(List<BMLSyncPointProgressFeedback> fbs);
    
    void removeAllFeedbackListeners();
    
    void removeFeedbackListener(BMLFeedbackListener fb);
    
    ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId);
    
    void blockStopFeedback(BMLPerformanceStopFeedback psf);
    
    void blockStartFeedback(BMLPerformanceStartFeedback psf);
    
    
    void exception(BMLExceptionFeedback e);
    
    void addExceptionListener(BMLExceptionListener es);
    
    void removeAllExceptionListeners();
    
    void puException(TimedPlanUnit timedMU, String message, double time);
    
    void removeExceptionListener(BMLExceptionListener e);
    
    void addPlanningListener(BMLTSchedulingListener p);
    
    void removeAllPlanningListeners();
    
    void planningStart(BMLTSchedulingStartFeedback bpsf);
    
    void planningFinished(BMLTSchedulingFinishedFeedback bpff);
    
    /**
     * Generates a feedback message on only the start time of a BML block
     */
    void blockStartPrediction(String bmlId, double time);
    
    void addWarningListener(BMLWarningListener ws);
    
    void removeAllWarningListeners();
    
    void removeWarningListener(BMLWarningListener ws);
    
    void warn(BMLWarningFeedback w);
}
