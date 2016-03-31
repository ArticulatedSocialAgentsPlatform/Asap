/*******************************************************************************
 *******************************************************************************/
package asap.incrementalspeechengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitPlayer;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerport.BMLFeedbackListener;

/**
 * Generic PlanUnit player. Plays the correct PlanUnit at time t.
 * 
 * PlanUnits used in the PlanPlayer should have non-blocking play and start functions.
 * 
 * @author Herwin
 * @param <T>
 */
@ThreadSafe
@Slf4j
public final class SingleThreadedPlanPlayeriSS<T extends TimedPlanUnit> implements PlanPlayer
{
    private final PlanManager<T> planManager;

    private final FeedbackManager fbManager;

    private List<T> tmuRemove = new ArrayList<T>();

    private TimedPlanUnitPlayer tpuPlayer;

    public SingleThreadedPlanPlayeriSS(FeedbackManager fbm, PlanManager<T> planManager, TimedPlanUnitPlayer tpuCallback)
    {
        fbManager = fbm;
        this.planManager = planManager;
        this.tpuPlayer = tpuCallback;
    }

    public SingleThreadedPlanPlayeriSS(FeedbackManager fbm, PlanManager<T> planManager)
    {
        this(fbm, planManager, new DefaultTimedPlanUnitPlayer());
    }

    public SingleThreadedPlanPlayeriSS(PlanManager<T> planManager)
    {
        this(NullFeedbackManager.getInstance(), planManager);
    }

    public void warn(BMLWarningFeedback e, double time)
    {
        fbManager.warn(e, time);
    }

    private List<T> playingPlanUnits = new ArrayList<T>();

    @Override
    public void updateTiming(String bmlId)
    {
        List<T> failedBehaviors = new ArrayList<>();
        for (T pu : planManager.getPlanUnits())
        {
            if (pu.getBMLId().equals(bmlId))
            {
                try
                {
                    pu.updateTiming(0);
                }
                catch (TimedPlanUnitPlayException e)
                {
                    log.warn("Failure in updating the timing of {}, removed", pu);
                    failedBehaviors.add(pu);
                }
            }
            planManager.removePlanUnits(failedBehaviors, 0);
        }
    }

    public synchronized void play(double t)
    {
        playingPlanUnits.clear();
        tmuRemove.clear();

        for (T pu : planManager.getPlanUnits())
        {
            if (t < pu.getStartTime() || pu.isLurking())
            {
                try
                {
                    pu.updateTiming(t);
                }
                catch (TimedPlanUnitPlayException e)
                {
                    log.warn("Exception when updating timing: ", e);
                }
            }

            if (t >= pu.getStartTime() && (pu.isPlaying() || pu.isLurking()))
            {
                playingPlanUnits.add(pu);
            }
        }

        for (T pu : playingPlanUnits)
        {
            tpuPlayer.playUnit(pu, t);
        }
        tpuPlayer.handlePlayExceptions(t, fbManager);
        tpuPlayer.handleStopExceptions(t);
        for (T tmuR : tmuRemove)
        {
            tpuPlayer.stopUnit(tmuR, t);
        }
        planManager.removeFinishedPlanUnits();
    }

    public Set<String> getInvalidBehaviors()
    {
        return planManager.getInvalidBehaviours();
    }

    /**
     * Stops and removes a selected collection of planunits.
     * Blocking.
     */
    public synchronized void removePlanUnits(Collection<T> puRemove, double time)
    {
        planManager.removePlanUnits(puRemove, time);
    }

    /**
     * Stops and removes a planunit.
     * Blocking.
     */
    public synchronized void stopPlanUnit(String bmlId, String id, double globalTime)
    {
        planManager.stopPlanUnit(bmlId, id, globalTime);
    }

    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        planManager.setBMLBlockState(bmlId, state);
    }

    public int getNumberOfPlanUnits()
    {
        return planManager.getNumberOfPlanUnits();
    }

    /**
     * Removes all planunits, if appropriate sends feedback at time
     * 
     * @param time
     */
    public synchronized void reset(double time)
    {
        planManager.removeAllPlanUnits(time);
    }

    public Set<String> getBehaviours(String bmlId)
    {
        return planManager.getBehaviours(bmlId);
    }

    public synchronized void stopBehaviourBlock(String bmlId, double time)
    {
        planManager.stopBehaviourBlock(bmlId, time);
    }

    @Override
    public void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        planManager.interruptPlanUnit(bmlId, id, globalTime);
    }

    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        planManager.interruptBehaviourBlock(bmlId, time);
    }

    public void shutdown()
    {
    }

    public void addFeedbackListener(BMLFeedbackListener fl)
    {
        fbManager.addFeedbackListener(fl);
    }

}
