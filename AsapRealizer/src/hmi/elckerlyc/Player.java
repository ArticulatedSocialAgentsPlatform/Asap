/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package hmi.elckerlyc;

import hmi.elckerlyc.planunit.TimedPlanUnitState;

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
     * Interrupts all behaviors in the block; that is: calls their stop and removes them from the plan
     */
    void interruptBehaviourBlock(String bmlId, double time);

    /**
     * Interrupts a behavior. That is: calls their stop and removes them from the plan
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
