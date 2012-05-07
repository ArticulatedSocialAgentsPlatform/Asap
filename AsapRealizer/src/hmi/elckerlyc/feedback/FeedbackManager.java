package hmi.elckerlyc.feedback;

import hmi.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import hmi.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.bml.feedback.BMLExceptionListener;
import hmi.bml.feedback.BMLFeedbackListener;
import hmi.bml.feedback.BMLPerformanceStartFeedback;
import hmi.bml.feedback.BMLPerformanceStopFeedback;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.bml.feedback.BMLWarningFeedback;
import hmi.bml.feedback.BMLWarningListener;
import hmi.elckerlyc.planunit.TimedPlanUnit;

import java.util.List;

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
