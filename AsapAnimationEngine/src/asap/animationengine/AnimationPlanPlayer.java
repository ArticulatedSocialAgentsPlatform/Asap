package asap.animationengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import asap.animationengine.motionunit.TimedMotionUnit;
import asap.planunit.PlanUnitPriorityComparator;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.PlanPlayer;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitState;

public class AnimationPlanPlayer implements PlanPlayer
{
    private final SingleThreadedPlanPlayer<TimedMotionUnit> defPlayer;
    private final PlanManager<TimedMotionUnit> planManager;
    private final FeedbackManager fbManager;
    private final TimedPlanUnitPlayer tpuPlayer;
    
    
    public AnimationPlanPlayer(FeedbackManager fbm, PlanManager<TimedMotionUnit> planManager, TimedPlanUnitPlayer tpuCallback)
    {
        defPlayer = new SingleThreadedPlanPlayer<TimedMotionUnit>(fbm, planManager, tpuCallback);
        fbManager = fbm;
        tpuPlayer = tpuCallback;
        this.planManager = planManager;
    }
    
    @Override
    public synchronized void play(double t)
    {
        List<TimedMotionUnit> tmuRemove = new ArrayList<TimedMotionUnit>();
        List<TimedMotionUnit> playingPlanUnits = new ArrayList<TimedMotionUnit>();
        Set<String> physicalJoints = new HashSet<String>();
        Set<String> kinematicJoints = new HashSet<String>();
        
        playingPlanUnits.clear();
        tmuRemove.clear();
        
        //check which units should be playing
        for (TimedMotionUnit pu : planManager.getPlanUnits())
        {
            if (t >= pu.getStartTime() && (pu.isPlaying() || pu.isLurking()))
            {
                playingPlanUnits.add(pu);                
            }
        }

        //sort by priority
        Collections.sort(playingPlanUnits, new PlanUnitPriorityComparator());
        
        for (TimedMotionUnit tmu : playingPlanUnits)
        {
            
            tpuPlayer.playUnit(tmu, t);            
        }
        tpuPlayer.handlePlayExceptions(t,fbManager);
        tpuPlayer.handleStopExceptions(t);
        for (TimedPlanUnit tmuR : tmuRemove)
        {
            tpuPlayer.stopUnit(tmuR, t);
        }
        planManager.removeFinishedPlanUnits();        
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
    
}
