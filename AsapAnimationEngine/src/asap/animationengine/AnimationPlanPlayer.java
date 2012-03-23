package asap.animationengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Sets;

import asap.animationengine.motionunit.TimeAnimationUnit;
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
    private final SingleThreadedPlanPlayer<TimeAnimationUnit> defPlayer;
    private final PlanManager<TimeAnimationUnit> planManager;
    private final FeedbackManager fbManager;
    private final TimedPlanUnitPlayer tpuPlayer;
    private final RestPose defaultRestPose;
    private RestPose currentRestPose;

    public RestPose getRestPose()
    {
        return currentRestPose;
    }

    public AnimationPlanPlayer(RestPose defaultRestPose, FeedbackManager fbm, PlanManager<TimeAnimationUnit> planManager,
            TimedPlanUnitPlayer tpuCallback)
    {
        defPlayer = new SingleThreadedPlanPlayer<TimeAnimationUnit>(fbm, planManager, tpuCallback);
        fbManager = fbm;
        tpuPlayer = tpuCallback;
        this.planManager = planManager;
        this.defaultRestPose = defaultRestPose;
        currentRestPose = defaultRestPose;
    }

    private void updateTiming(double t)
    {
        List<TimeAnimationUnit> tuUpdateFailed = new ArrayList<TimeAnimationUnit>();        
        // check which units should be playing
        for (TimeAnimationUnit tmu : planManager.getPlanUnits())
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
        List<TimeAnimationUnit> tmuRemove = new ArrayList<TimeAnimationUnit>();
        List<TimeAnimationUnit> playingPlanUnits = new ArrayList<TimeAnimationUnit>();
        Set<String> physicalJoints = new HashSet<String>();
        Set<String> kinematicJoints = new HashSet<String>();

        playingPlanUnits.clear();
        tmuRemove.clear();
        log.debug("plan Units: {}",planManager.getPlanUnits());
        
        //long time = System.nanoTime();
        updateTiming(t);
        //log.debug("update time: {} ms", (System.nanoTime()-time)/1000000d );
        
        // check which units should be playing
        for (TimeAnimationUnit tmu : planManager.getPlanUnits())
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
        List<TimeAnimationUnit> tmuAdd = playback(t, tmuRemove, playingPlanUnits, physicalJoints, kinematicJoints);
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

    private List<TimeAnimationUnit> playback(double t, List<TimeAnimationUnit> tmuRemove, List<TimeAnimationUnit> playingPlanUnits,
            Set<String> physicalJoints, Set<String> kinematicJoints)
    {
        List<TimeAnimationUnit> tmuAdd = new ArrayList<TimeAnimationUnit>();
        for (TimeAnimationUnit tmu : playingPlanUnits)
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
                TimeAnimationUnit tmuCleanup = this.currentRestPose.createTransitionToRest(cleanup, t, tmu.getBMLId(), tmu.getId()
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
