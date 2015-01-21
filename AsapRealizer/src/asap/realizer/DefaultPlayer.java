/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Default implementation of the Player. Use this player if each TimedPlanUnit in the plan managed
 * by the player can directly apply its result on the virtual human embodiment. That is: there is no
 * conflict resolution or combination needed between the results of the TimedPlanUnits in the plan.
 * 
 * @author Herwin
 * 
 */
public class DefaultPlayer implements Player
{
    private final PlanPlayer planPlayer;

    public void updateTiming(String bmlId)
    {
        planPlayer.updateTiming(bmlId);
    }

    public DefaultPlayer(PlanPlayer pPlayer)
    {
        planPlayer = pPlayer;
    }

    public synchronized void play(double time)
    {
        planPlayer.play(time);
    }

    /** Implementation of this verification is completely dependent on the particular player class. Default do nothing. */
    public synchronized void verifyTime(double time)
    {
    }

    public void interruptBehaviour(String bmlId, String behaviourId, double globalTime)
    {
        planPlayer.interruptPlanUnit(bmlId, behaviourId, globalTime);
    }

    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        planPlayer.interruptBehaviourBlock(bmlId, time);
    }

    
    public void stopBehaviour(String bmlId, String behaviourId, double globalTime)
    {
        planPlayer.stopPlanUnit(bmlId, behaviourId, globalTime);
    }

    @Override
    public void stopBehaviourBlock(String bmlId, double time)
    {
        planPlayer.stopBehaviourBlock(bmlId, time);
    }

    @Override
    public void reset(double time)
    {
        planPlayer.reset(time);
    }

    @Override
    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        planPlayer.setBMLBlockState(bmlId, state);
    }

    @Override
    public void shutdown()
    {
        planPlayer.shutdown();
    }
}
