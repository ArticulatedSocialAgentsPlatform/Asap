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
    public void blockProgress(BMLBlockProgressFeedback psf){}
    
    
    @Override
    public void puException(TimedPlanUnit timedMU, String message, double time){}

    
    @Override
    public void prediction(BMLPredictionFeedback bpsf)
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
