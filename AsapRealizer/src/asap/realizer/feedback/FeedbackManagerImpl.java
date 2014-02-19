package asap.realizer.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.BMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Default implementation for the FeedbackManager
 * @author Herwin
 *
 */
@Slf4j
public class FeedbackManagerImpl implements FeedbackManager
{
    private final BMLBlockManager bmlBlockManager;

    @GuardedBy("feedbackListeners")
    private final List<BMLFeedbackListener> feedbackListeners = Collections.synchronizedList(new ArrayList<BMLFeedbackListener>());

    private final String characterId;

    public FeedbackManagerImpl(BMLBlockManager bbm, String characterId)
    {
        bmlBlockManager = bbm;
        this.characterId = characterId;
    }

    @Override
    public void addFeedbackListener(BMLFeedbackListener fb)
    {
        feedbackListeners.add(fb);
    }

    @Override
    public void puException(TimedPlanUnit timedMU, String message, double time)
    {
        String bmlId = timedMU.getBMLId();        
        String exceptionText = message + "\nBehavior " + timedMU.getBMLId() + ":" + timedMU.getId() + " dropped.";
        warn(new BMLWarningFeedback(bmlId+":"+timedMU.getId(), "EXECUTION_FAILURE",exceptionText));
    }
    
    @Override
    public void feedback(BMLSyncPointProgressFeedback fb)
    {
        fb.setCharacterId(characterId);
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener fbl : feedbackListeners)
            {
                try
                {
                    fbl.feedback(fb.toXMLString());
                }
                catch (Exception ex)
                {
                    log.warn("Exception in FeedbackListener: {}, feedback: {}", ex, fb.toXMLString());
                }
            }
        }
        bmlBlockManager.syncProgress(fb);
    }

    /**
     * Send a list of feedback (in list-order) to the BMLFeedbackListeners. The listeners will
     * receive all feedbacks in the list before any subsequent feedback is sent using the feedback
     * functions.
     */
    @Override
    public void feedback(List<BMLSyncPointProgressFeedback> fbs)
    {
        synchronized (feedbackListeners)
        {
            for (BMLSyncPointProgressFeedback fb : fbs)
            {
                fb.setCharacterId(characterId);
                for (BMLFeedbackListener fbl : feedbackListeners)
                {
                    try
                    {
                        fbl.feedback(fb.toXMLString());
                    }
                    catch (Exception ex)
                    {
                        log.warn("Exception in FeedbackListener: {}, feedback: {}", ex, fb.toXMLString());
                    }
                }
            }
        }
        for (BMLSyncPointProgressFeedback fb : fbs)
        {
            bmlBlockManager.syncProgress(fb);
        }
    }

    @Override
    public void removeAllFeedbackListeners()
    {
        feedbackListeners.clear();
    }

    public ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId)
    {
        return bmlBlockManager.getSyncsPassed(bmlId, behaviorId);
    }

    @Override
    public void removeFeedbackListener(BMLFeedbackListener fb)
    {
        feedbackListeners.remove(fb);
    }

    @Override
    public void blockProgress(BMLBlockProgressFeedback psf)
    {
        psf.setCharacterId(characterId);
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener fbl : feedbackListeners)
            {
                try
                {
                    fbl.feedback(psf.toXMLString());
                }
                catch (Exception ex)
                {
                    log.warn("Exception in FeedbackListener: {}, feedback: {}", ex, psf);
                }
            }
        }
        bmlBlockManager.blockProgress(psf);
    }    

    
    @Override
    public void prediction(BMLPredictionFeedback bpf)
    {
        synchronized (feedbackListeners)
        {
            String feedbackString = bpf.toXMLString();       
            for (BMLFeedbackListener pl : feedbackListeners)
            {
                try
                {                    
                    pl.feedback(feedbackString);                    
                }
                catch (Exception ex)
                {
                    log.warn("Exception in FeedbackListener: {}, feedback: {}", ex, feedbackString);
                }
            }
        }        
    }    

    @Override
    public void warn(BMLWarningFeedback w)
    {
        w.setCharacterId(characterId);
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener wl : feedbackListeners)
            {
                try
                {
                    wl.feedback(w.toXMLString());
                }
                catch (Exception ex)
                {
                    log.warn("Exception in WarningListener: {}, feedback: {}", ex, w.toXMLString());
                }
            }
        }
        bmlBlockManager.warn(w);
    }    
}
