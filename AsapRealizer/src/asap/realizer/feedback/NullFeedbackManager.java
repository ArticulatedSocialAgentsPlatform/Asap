package asap.realizer.feedback;

import saiba.bml.feedback.BMLBlockProgress;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import java.util.List;

import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLWarningListener;
import asap.realizer.planunit.TimedPlanUnit;
import saiba.bml.feedback.BMLWarningFeedback;

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
    public void blockProgress(BMLBlockProgress psf){}
    
    
    @Override
    public void puException(TimedPlanUnit timedMU, String message, double time){}

    
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
