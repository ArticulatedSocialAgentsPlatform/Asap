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
package asap.realizer;

import saiba.bml.core.Behaviour;

import java.util.List;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Elckerlyc planner. Each planner can add BML behaviors to a plan and is able to resolve their unknown time constraints
 * 
 * @author Herwin van Welbergen
 * @param <T>
 */
public interface Planner<T extends TimedPlanUnit>
{
    /**
     * Adds a behavior to the plan. All timepegs in sac must be resolved (have time other than unknown); all TimePegs for the behavior are to be added to 
     * SyncAndTimePeg.
     * 
     * @param planElement planElement obtained from resolveSynchs, null to create a new planElement
     * @return a list of all syncs of the behavior and their linked TimePegs
     * @throws BehaviourPlanningException if no behavior satisfying sac can be constructed
     */
    List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac, T planElement)
            throws BehaviourPlanningException;

    /**
     * Resolves TimePegs for behavior b, given some known time pegs and constraints
     * 
     * @param b the behavior
     * @param sac the provided time pegs and constraints, missing constraints are filled out by this method
     * @return the object to be placed in the plan (a TimedMotionUnit, SpeechUnit, ...)
     * @throws BehaviourPlanningException if no behavior satisfying sac can be constructed
     */
    T resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException;

    List<Class<? extends Behaviour>> getSupportedBehaviours();

    List<Class<? extends Behaviour>> getSupportedDescriptionExtensions();

    /**
     * @return 0: behavior should adjust completely to the timing of other behaviors, 
     * 1:completely inflexible
     */
    double getRigidity(Behaviour beh);
    /**
     * Clean up resources constructed with the planner (e.g. for native stuff, spawned threads, ...). A planner should not be (re)used after shutting
     * it down.
     */
    void shutdown();
}
