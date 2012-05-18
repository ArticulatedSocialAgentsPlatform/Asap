package asap.realizer.planunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import saiba.bml.feedback.BMLWarningFeedback;

import net.jcip.annotations.ThreadSafe;
import asap.bml.feedback.BMLWarningListener;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.TimePeg;

/**
 * Generic PlanUnit player. Plays the correct PlanUnit at time t, taking into account replacement groups etc.
 * 
 * PlanUnits used in the PlanPlayer should have non-blocking play and start functions.
 * 
 * @author Herwin 
 */
@ThreadSafe
public final class SingleThreadedPlanPlayer<T extends TimedPlanUnit> implements PlanPlayer
{
    private final PlanManager<T> planManager;

    private final FeedbackManager fbManager;

    private List<T> tmuRemove = new ArrayList<T>();

    private TimedPlanUnitPlayer tpuPlayer;

    public SingleThreadedPlanPlayer(FeedbackManager fbm, PlanManager<T> planManager, TimedPlanUnitPlayer tpuCallback)
    {
        fbManager = fbm;
        this.planManager = planManager;
        this.tpuPlayer = tpuCallback;
    }

    public SingleThreadedPlanPlayer(FeedbackManager fbm, PlanManager<T> planManager)
    {
        this(fbm, planManager, new DefaultTimedPlanUnitPlayer());
    }

    public SingleThreadedPlanPlayer(PlanManager<T> planManager)
    {
        this(NullFeedbackManager.getInstance(), planManager);
    }

    public void warn(BMLWarningFeedback e)
    {
        fbManager.warn(e);
    }

    public void addWarningListener(BMLWarningListener ws)
    {
        fbManager.addWarningListener(ws);
    }

    public void removeAllWarningListeners()
    {
        fbManager.removeAllWarningListeners();
    }

    private List<T> playingPlanUnits = new ArrayList<T>();

    private boolean updatePlayingPU(T pu, double t)
    {
        ArrayList<T> remove = new ArrayList<T>();
        boolean play = true;
        T replacementOverlap = null;
        for (T planUnitPlay : playingPlanUnits)
        {
            if (planUnitPlay.getReplacementGroup() != null && planUnitPlay.getReplacementGroup().equals(pu.getReplacementGroup()))
            {
                boolean runPuPlay = false;
                double puPlayEnd = planUnitPlay.getEndTime();
                double puEnd = pu.getEndTime();
                if (t >= planUnitPlay.getEndTime() && planUnitPlay.getEndTime() != TimePeg.VALUE_UNKNOWN)
                {
                    tpuPlayer.stopUnit(planUnitPlay, t);
                    runPuPlay = false;
                }
                else if (t >= pu.getEndTime() && pu.getEndTime() != TimePeg.VALUE_UNKNOWN)
                {
                    tpuPlayer.stopUnit(pu, t);
                    runPuPlay = true;
                }
                else if (planUnitPlay.getStartTime() > pu.getStartTime())
                {
                    runPuPlay = true;
                }
                else if (planUnitPlay.getStartTime() < pu.getStartTime())
                {
                    runPuPlay = false;
                }
                else if (puEnd == TimePeg.VALUE_UNKNOWN && puPlayEnd != TimePeg.VALUE_UNKNOWN)
                {
                    runPuPlay = true;
                }
                else if (puEnd != TimePeg.VALUE_UNKNOWN && puPlayEnd == TimePeg.VALUE_UNKNOWN)
                {
                    runPuPlay = false;
                }
                else if (puEnd == TimePeg.VALUE_UNKNOWN && puPlayEnd == TimePeg.VALUE_UNKNOWN)
                {
                    replacementOverlap = planUnitPlay;
                    runPuPlay = false;
                }
                else if (puPlayEnd < puEnd)
                {
                    runPuPlay = true;
                }
                else if (puPlayEnd == puEnd)
                {
                    replacementOverlap = planUnitPlay;
                    runPuPlay = false;
                }
                if (runPuPlay)
                {
                    play = false;
                    break;
                }
                else
                {
                    remove.add(planUnitPlay);
                }
                if (replacementOverlap != null)
                {
                    break;
                }
            }
        }
        playingPlanUnits.removeAll(remove);

        if (replacementOverlap != null)
        {
            fbManager.puException(replacementOverlap, "Replacement group overlap between " + pu.getBMLId() + ":" + pu.getId() + " and "
                    + replacementOverlap.getBMLId() + ":" + replacementOverlap.getId(), t);
            tmuRemove.add(replacementOverlap);
        }
        return play;
    }

    public synchronized void play(double t)
    {
        playingPlanUnits.clear();
        tmuRemove.clear();

        for (T pu : planManager.getPlanUnits())
        {
            if (t >= pu.getStartTime() && (pu.isPlaying() || pu.isLurking()))
            {
                if (updatePlayingPU(pu, t))
                {
                    playingPlanUnits.add(pu);
                }
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
    public synchronized void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        planManager.interruptPlanUnit(bmlId, id, globalTime);        
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

    public synchronized void interruptBehaviourBlock(String bmlId, double time)
    {
        planManager.interruptBehaviourBlock(bmlId, time);
    }

    public void shutdown()
    {
    }

}
