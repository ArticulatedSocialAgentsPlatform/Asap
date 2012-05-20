package asap.realizer.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLPredictionListener;
import asap.bml.feedback.BMLWarningListener;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;

import com.google.common.collect.ImmutableSet;

/**
 * Default implementation for the FeedbackManager
 * @author Herwin
 *
 */
public class FeedbackManagerImpl implements FeedbackManager
{
    private final BMLBlockManager bmlBlockManager;

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedbackManagerImpl.class.getName());

    @GuardedBy("feedbackListeners")
    private final List<BMLFeedbackListener> feedbackListeners = Collections.synchronizedList(new ArrayList<BMLFeedbackListener>());

    @GuardedBy("warningListeners")
    private final List<BMLWarningListener> warningListeners = Collections.synchronizedList(new ArrayList<BMLWarningListener>());

    @GuardedBy("planningListeners")
    private final List<BMLPredictionListener> predictionListeners = Collections.synchronizedList(new ArrayList<BMLPredictionListener>());
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
                    fbl.syncProgress(fb);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken FeedbackListener: {}", ex);
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
                        fbl.syncProgress(fb);
                    }
                    catch (Exception ex)
                    {
                        LOGGER.warn("Broken FeedbackListener: {}", ex);
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
                    fbl.blockProgress(psf);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken FeedbackListener: {}", ex);
                }
            }
        }
        bmlBlockManager.blockProgress(psf);
    }    

    @Override
    public void addPredictionListener(BMLPredictionListener p)
    {
        predictionListeners.add(p);
    }

    @Override
    public void removeAllPredictionListeners()
    {
        predictionListeners.clear();
    }

    @Override
    public void addWarningListener(BMLWarningListener ws)
    {
        warningListeners.add(ws);
    }

    @Override
    public void removeAllWarningListeners()
    {
        warningListeners.clear();
    }

    @Override
    public void removeWarningListener(BMLWarningListener ws)
    {
        warningListeners.remove(ws);
    }

    @Override
    public void prediction(BMLPredictionFeedback bpf)
    {
        synchronized (predictionListeners)
        {
            for (BMLPredictionListener pl : predictionListeners)
            {
                try
                {
                    pl.prediction(bpf);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken SchedulingListener: {}", ex);
                }
            }
        }
    }    

    @Override
    public void warn(BMLWarningFeedback w)
    {
        w.setCharacterId(characterId);
        synchronized (warningListeners)
        {
            for (BMLWarningListener wl : warningListeners)
            {
                try
                {
                    wl.warn(w);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken WarningListener: {}", ex);
                }
            }
        }
        bmlBlockManager.warn(w);
    }

    @Override
    public void blockStartPrediction(String bmlId, double time)
    {
        // TODO Auto-generated method stub

    }
}
