package hmi.elckerlyc;

import hmi.bml.core.Behaviour;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;

import java.util.List;
import java.util.Set;

/**
 * Default implementation of the Engine Interface
 * 
 * @author Herwin van Welbergen
 */
public class DefaultEngine<T extends TimedPlanUnit> implements Engine
{
    private final Planner<T> planner;
    private final Player player;
    private final PlanManager<T> planManager;
    private boolean verifyNoPlay = false;

    public void updateTiming(String bmlId)
    {
        player.updateTiming(bmlId);
    }
    
    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        // planManager.setBMLBlockState(bmlId, state);
        player.setBMLBlockState(bmlId, state);
    }

    @SuppressWarnings("unchecked")
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b,
            List<TimePegAndConstraint> sac, TimedPlanUnit planElement)
            throws BehaviourPlanningException
    {
        return planner.addBehaviour(bbPeg, b, sac, (T)planElement);
    }

    public T resolveSynchs(BMLBlockPeg bbPeg, Behaviour b,
            List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        return planner.resolveSynchs(bbPeg, b, sac);
    }

    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        return planner.getSupportedBehaviours();
    }

    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        return planner.getSupportedDescriptionExtensions();
    }

    public void interruptBehaviourBlock(String bmlId, double time)
    {
        player.interruptBehaviourBlock(bmlId, time);
    }

    public void interruptBehaviour(String bmlId, String behaviourId, double time)
    {
        player.interruptBehaviour(bmlId, behaviourId, time);
    }

    public double getEndTime(String bmlId, String behId)
    {
        return planManager.getEndTime(bmlId, behId);
    }

    public float getFloatParameterValue(String bmlId, String behId, String paramId)
            throws ParameterException, BehaviorNotFoundException
    {
        return planManager.getFloatParameterValue(bmlId, behId, paramId);
    }

    public DefaultEngine(Planner<T> planner, Player player, PlanManager<T> planManager)
    {
        this(planner, player, planManager, false);
    }
        
    /** 
     * @param verifyNoPlay    if set, the engine should not call player.play(time), 
     * but rather player.verifyTime(time), because the player is already 
     * being "played" from somewhere else. 
     */
    public DefaultEngine(Planner<T> planner, Player player, PlanManager<T> planManager, boolean verifyNoPlay)
    {
        this.planner = planner;
        this.player = player;
        this.planManager = planManager;
        this.verifyNoPlay = verifyNoPlay;
    }

    @Override
    public Set<String> getInvalidBehaviours()
    {
        return planManager.getInvalidBehaviours();
    }

    @Override
    public boolean containsBehaviour(String bmlId, String behId)
    {
        return planManager.containsBehaviour(bmlId, behId);
    }

    @Override
    public Set<String> getBehaviours(String bmlId)
    {
        return planManager.getBehaviours(bmlId);
    }

    @Override
    public void setFloatParameterValue(String bmlId, String behId, String paramId, float value)
            throws ParameterException, BehaviorNotFoundException
    {
        planManager.setFloatParameterValue(bmlId, behId, paramId, value);
    }

    @Override
    public OffsetPeg createOffsetPeg(String bmlId, String behId, String syncId)
            throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        return planManager.createOffsetPeg(bmlId, behId, syncId);
    }

    @Override
    public void setParameterValue(String bmlId, String behId, String paramId, String value)
            throws ParameterException, BehaviorNotFoundException
    {
        planManager.setParameterValue(bmlId, behId, paramId, value);
    }

    @Override
    public String getParameterValue(String bmlId, String behId, String paramId)
            throws ParameterException, BehaviorNotFoundException
    {
        return planManager.getParameterValue(bmlId, behId, paramId);
    }

    @Override
    public double getBlockEndTime(String bmlId)
    {
        return planManager.getEndTime(bmlId);
    }

    @Override
    public double getBlockSubsidingTime(String bmlId)
    {
        return planManager.getSubsidingTime(bmlId);
    }
    
    @Override
    public void shutdown()
    {
        player.shutdown();
        planner.shutdown();
    }

    @Override
    public void reset(double time)
    {
        player.reset(time);
    }

    @Override
    public void play(double time)
    {
        if (verifyNoPlay)
        {
            player.verifyTime(time);
        } 
        else
        {
            player.play(time);
        }
    }

    private String id = "";

    public void setId(String newId)
    {
        id = newId;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return planner.getRigidity(beh);
    }    
}
