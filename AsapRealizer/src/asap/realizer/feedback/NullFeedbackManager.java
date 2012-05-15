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

/**
 * Null implementation of the FeedbackManager, ignores all feedback. 
 * Useful for building TimedPlanUnits that are executed outside a full blown Elckerlyc environment.
 * @author Herwin
 */
public final class NullFeedbackManager implements FeedbackManager
{
    private static final NullFeedbackManager NULL_MANAGER = new NullFeedbackManager();
    private NullFeedbackManager(){}
    
    public static NullFeedbackManager getInstance()
    {
        return NULL_MANAGER;
    }
    
    @Override
    public void addFeedbackListener(BMLFeedbackListener fb){}

    @Override
    public void feedback(BMLSyncPointProgressFeedback fb){}

    @Override
    public void feedback(List<BMLSyncPointProgressFeedback> fbs){}

    @Override
    public void removeAllFeedbackListeners(){}

    @Override
    public void removeFeedbackListener(BMLFeedbackListener fb){}
    
    @Override
    public ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId)
    {
        return new ImmutableSet.Builder<String>().build();        
    }

    @Override
    public void blockStopFeedback(BMLPerformanceStopFeedback psf){}
    
    @Override
    public void blockStartFeedback(BMLPerformanceStartFeedback psf){}
    
    @Override
    public void exception(BMLExceptionFeedback e){}
    
    @Override
    public void addExceptionListener(BMLExceptionListener es){}
    
    @Override
    public void removeAllExceptionListeners(){}
    
    @Override
    public void puException(TimedPlanUnit timedMU, String message, double time){}

    @Override
    public void removeExceptionListener(BMLExceptionListener e)
    {
    }

    @Override
    public void addPlanningListener(BMLTSchedulingListener p)
    {
    }

    @Override
    public void removeAllPlanningListeners()
    {
    }

    @Override
    public void addWarningListener(BMLWarningListener ws)
    {
    }

    @Override
    public void removeAllWarningListeners()
    {
    }

    @Override
    public void removeWarningListener(BMLWarningListener ws)
    {
    }

    @Override
    public void planningStart(BMLTSchedulingStartFeedback bpsf)
    {
    }

    @Override
    public void planningFinished(BMLTSchedulingFinishedFeedback bpff)
    {
    }

    @Override
    public void warn(BMLWarningFeedback w)
    {
    }

    @Override
    public void blockStartPrediction(String bmlId, double time)
    {
    }   
}
