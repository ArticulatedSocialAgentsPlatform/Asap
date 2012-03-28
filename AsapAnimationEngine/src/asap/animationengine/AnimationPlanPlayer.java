package asap.animationengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Sets;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.motionunit.TMUPlayException;
import asap.planunit.PlanUnitPriorityComparator;

import hmi.bml.feedback.BMLExceptionListener;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.PlanPlayer;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitState;

/**
 * Specialized PlanPlayer that handles conflict resolution for TimedMotionUnits
 * @author hvanwelbergen
 */
@Slf4j
public class AnimationPlanPlayer implements PlanPlayer
{
    private final SingleThreadedPlanPlayer<TimedAnimationUnit> defPlayer;
    private final PlanManager<TimedAnimationUnit> planManager;
    private final FeedbackManager fbManager;
    private final TimedPlanUnitPlayer tpuPlayer;
    private final RestPose defaultRestPose;
    private RestPose currentRestPose;

    public RestPose getRestPose()
    {
        return currentRestPose;
    }

    public AnimationPlanPlayer(RestPose defaultRestPose, FeedbackManager fbm, PlanManager<TimedAnimationUnit> planManager,
            TimedPlanUnitPlayer tpuCallback)
    {
        defPlayer = new SingleThreadedPlanPlayer<TimedAnimationUnit>(fbm, planManager, tpuCallback);
        fbManager = fbm;
        tpuPlayer = tpuCallback;
        this.planManager = planManager;
        this.defaultRestPose = defaultRestPose;
        currentRestPose = defaultRestPose;
    }

    private void updateTiming(double t)
    {
        List<TimedAnimationUnit> tuUpdateFailed = new ArrayList<TimedAnimationUnit>();        
        // check which units should be playing
        for (TimedAnimationUnit tmu : planManager.getPlanUnits())
        {
            if(tmu.isPlaying()||tmu.isLurking())
            //if(tmu.isLurking())
            {
                try
                {
                    tmu.updateTiming(t);
                }
                catch (TMUPlayException e)
                {
                    tuUpdateFailed.add(tmu);
                    log.warn("updateTiming failure, TimedMotionUnit dropped",e);
                    continue;
                }
            }            
        }
        planManager.removePlanUnits(tuUpdateFailed, t);
    }
    
    @Override
    public synchronized void play(double t)
    {
        List<TimedAnimationUnit> tmuRemove = new ArrayList<TimedAnimationUnit>();
        List<TimedAnimationUnit> playingPlanUnits = new ArrayList<TimedAnimationUnit>();
        Set<String> physicalJoints = new HashSet<String>();
        Set<String> kinematicJoints = new HashSet<String>();

        playingPlanUnits.clear();
        tmuRemove.clear();
        log.debug("plan Units: {}",planManager.getPlanUnits());
        
        //long time = System.nanoTime();
        updateTiming(t);
        //log.debug("update time: {} ms", (System.nanoTime()-time)/1000000d );
        
        // check which units should be playing
        for (TimedAnimationUnit tmu : planManager.getPlanUnits())
        {
            if (t >= tmu.getStartTime() && (tmu.isPlaying() || tmu.isLurking()))
            {
                playingPlanUnits.add(tmu);
            }
        }
        
        
        log.debug("playing plan Units: {}",playingPlanUnits);
        
        // sort by priority
        Collections.sort(playingPlanUnits, new PlanUnitPriorityComparator());

        // playback
        List<TimedAnimationUnit> tmuAdd = playback(t, tmuRemove, playingPlanUnits, physicalJoints, kinematicJoints);
        planManager.addPlanUnits(tmuAdd);

        tpuPlayer.handlePlayExceptions(t, fbManager);
        tpuPlayer.handleStopExceptions(t);
        for (TimedPlanUnit tmuR : tmuRemove)
        {
            tpuPlayer.stopUnit(tmuR, t);
        }
        planManager.removeFinishedPlanUnits();
        currentRestPose.play(t, kinematicJoints, physicalJoints);
    }

    private List<TimedAnimationUnit> playback(double t, List<TimedAnimationUnit> tmuRemove, List<TimedAnimationUnit> playingPlanUnits,
            Set<String> physicalJoints, Set<String> kinematicJoints)
    {
        List<TimedAnimationUnit> tmuAdd = new ArrayList<TimedAnimationUnit>();
        for (TimedAnimationUnit tmu : playingPlanUnits)
        {
            if (Sets.intersection(tmu.getKinematicJoints(), kinematicJoints).isEmpty()
                    && Sets.intersection(tmu.getKinematicJoints(), physicalJoints).isEmpty()
                    && Sets.intersection(tmu.getPhysicalJoints(), kinematicJoints).isEmpty())
            {
                tpuPlayer.playUnit(tmu, t);
                kinematicJoints.addAll(tmu.getKinematicJoints());
                physicalJoints.addAll(tmu.getPhysicalJoints());
            }
            else
            {
                if (tmu.isLurking())
                {
                    fbManager.puException(tmu, "Dropping " + tmu.getBMLId() + ":" + tmu.getId()
                            + "for higher priority behaviors before it was even started", t);
                }
                log.debug("Dropping {}:{}",tmu.getBMLId(),tmu.getId());
                Set<String> cleanup = new HashSet<String>(tmu.getKinematicJoints());
                cleanup.removeAll(kinematicJoints);
                cleanup.addAll(tmu.getPhysicalJoints());
                cleanup.removeAll(physicalJoints);
                TimedAnimationUnit tmuCleanup = this.currentRestPose.createTransitionToRest(cleanup, t, tmu.getBMLId(), tmu.getId()
                        + "-cleanup", tmu.getBMLBlockPeg());
                tmuRemove.add(tmu);
                tmuAdd.add(tmuCleanup);
            }
        }
        if (!tmuAdd.isEmpty())
        {
            tmuAdd.addAll(playback(t, tmuRemove, tmuAdd, physicalJoints, kinematicJoints));
        }
        return tmuAdd;
    }

    @Override
    public void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        defPlayer.interruptPlanUnit(bmlId, id, globalTime);
    }

    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        defPlayer.interruptBehaviourBlock(bmlId, time);
    }

    @Override
    public void reset(double time)
    {
        currentRestPose = defaultRestPose;
        defaultRestPose.setRestPose();
        defPlayer.reset(time);
    }

    @Override
    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        defPlayer.setBMLBlockState(bmlId, state);
    }

    @Override
    public void shutdown()
    {
        defPlayer.shutdown();
    }

    public void addExceptionListener(BMLExceptionListener ws)
    {
        defPlayer.addExceptionListener(ws);
    }
}
