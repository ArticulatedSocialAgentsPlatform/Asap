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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;

import com.google.common.collect.ImmutableSet;

public class FeedbackManagerImpl implements FeedbackManager
{
    private final BMLBlockManager bmlBlockManager;

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedbackManagerImpl.class.getName());

    @GuardedBy("feedbackListeners")
    private final List<BMLFeedbackListener> feedbackListeners = Collections.synchronizedList(new ArrayList<BMLFeedbackListener>());

    @GuardedBy("exceptionListeners")
    private final List<BMLExceptionListener> exceptionListeners = Collections.synchronizedList(new ArrayList<BMLExceptionListener>());

    @GuardedBy("warningListeners")
    private final List<BMLWarningListener> warningListeners = Collections.synchronizedList(new ArrayList<BMLWarningListener>());

    @GuardedBy("planningListeners")
    private final List<BMLTSchedulingListener> planningListeners = Collections.synchronizedList(new ArrayList<BMLTSchedulingListener>());
    private final String characterId;

    public FeedbackManagerImpl(BMLBlockManager bbm, String characterId)
    {
        bmlBlockManager = bbm;
        this.characterId = characterId;
    }

    @Override
    public void exception(BMLExceptionFeedback e)
    {
        e.setCharacterId(characterId);
        synchronized (exceptionListeners)
        {
            for (BMLExceptionListener el : exceptionListeners)
            {
                try
                {
                    el.exception(e);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken ExceptionListener: {}", ex);
                }
            }
        }
        bmlBlockManager.exception(e);
    }

    @Override
    public void addExceptionListener(BMLExceptionListener es)
    {
        exceptionListeners.add(es);
    }

    @Override
    public void removeAllExceptionListeners()
    {
        exceptionListeners.clear();
    }

    @Override
    public void puException(TimedPlanUnit timedMU, String message, double time)
    {
        String bmlId = timedMU.getBMLId();
        Set<String> failedBehaviours = new HashSet<String>();
        failedBehaviours.add(timedMU.getId());
        Set<String> failedConstraints = new HashSet<String>();
        String exceptionText = message + "\nBehavior " + timedMU.getBMLId() + ":" + timedMU.getId() + " dropped.";
        // TODO: handle performance failed
        exception(new BMLExceptionFeedback(bmlId, time, failedBehaviours, failedConstraints, exceptionText, false));
    }

    @Override
    public void addFeedbackListener(BMLFeedbackListener fb)
    {
        feedbackListeners.add(fb);
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
    public void blockStopFeedback(BMLPerformanceStopFeedback psf)
    {
        psf.setCharacterId(characterId);
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener fbl : feedbackListeners)
            {
                try
                {
                    fbl.performanceStop(psf);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken FeedbackListener: {}", ex);
                }
            }
        }
        bmlBlockManager.performanceStop(psf);
    }

    @Override
    public void blockStartFeedback(BMLPerformanceStartFeedback psf)
    {
        psf.setCharacterId(characterId);
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener fbl : feedbackListeners)
            {
                try
                {
                    fbl.performanceStart(psf);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken FeedbackListener: {}", ex);
                }
            }
        }
        bmlBlockManager.performanceStart(psf);
    }

    @Override
    public void removeExceptionListener(BMLExceptionListener e)
    {
        exceptionListeners.remove(e);
    }

    @Override
    public void addPlanningListener(BMLTSchedulingListener p)
    {
        planningListeners.add(p);
    }

    @Override
    public void removeAllPlanningListeners()
    {
        planningListeners.clear();
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
    public void planningStart(BMLTSchedulingStartFeedback bpsf)
    {
        synchronized (planningListeners)
        {
            for (BMLTSchedulingListener pl : planningListeners)
            {
                try
                {
                    pl.schedulingStart(bpsf);
                }
                catch (Exception ex)
                {
                    LOGGER.warn("Broken SchedulingListener: {}", ex);
                }
            }
        }
    }

    @Override
    public void planningFinished(BMLTSchedulingFinishedFeedback bpff)
    {
        synchronized (planningListeners)
        {
            for (BMLTSchedulingListener pl : planningListeners)
            {
                try
                {
                    pl.schedulingFinished(bpff);
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
