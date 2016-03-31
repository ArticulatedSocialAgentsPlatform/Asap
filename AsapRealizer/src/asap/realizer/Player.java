/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Elckerlyc player. A player belongs to a planner. A player can be reset (see e.g. BMLRealizer.reset()). In the future, more generic methods may be
 * added to this interface...
 * 
 * @author Herwin van Welbergen
 * @author Dennis Reidsma
 */
public interface Player
{
    /**
     * Reset the player, that is: stops all behaviors, then removes them from the plan and restores the startup state (default pose etc).
     */
    void reset(double time);

    void setBMLBlockState(String bmlId, TimedPlanUnitState state);

    /**
     * Updates the timing for all behaviors in bml block bmlId to reflect the current execution context
     * (e.g. position of limbs).
     */
    void updateTiming(String bmlId);
    
    /**
     * Stop all behaviors in the block; that is: calls their stop and removes them from the plan
     */
    void stopBehaviourBlock(String bmlId, double time);

    /**
     * Stop a behavior. That is: calls their stop and removes them from the plan
     */
    void stopBehaviour(String bmlId, String behaviourId, double time);
    
    /**
     * Gracefully stops all behaviors in the block; that is: calls their interrupt and removes them from the plan
     */
    void interruptBehaviourBlock(String bmlId, double time);

    /**
     * Gracefully interrupts a behavior. That is: calls their interrupt and removes them from the plan
     */
    void interruptBehaviour(String bmlId, String behaviourId, double time);
    
    /**
     * Clean up resources constructed with the planner (e.g. for native stuff, spawned threads, ...). A planner should not be (re)used after shutting
     * it down.
     */
    void shutdown();

    void play(double time);
    
    /** Generally, the Engine will call player.play(time). Sometimes, however, play(time) is 
     * already called from somewhere else (e.g., the AnimationPlayerManager, 
     * or a separate playing thread). In that case, the engine will not call player.play(time) 
     * but rather player.verifyTime(time). The player can use this call to 
     * check whether the timestamps it gets from the play() 
     * calls still sync well with the time stamps on which the engine is running
     */
    void verifyTime(double time);
}
