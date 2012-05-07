package hmi.elckerlyc.scheduler;

import hmi.bml.core.BMLBlockComposition;
import hmi.bml.core.BehaviourBlock;
import hmi.elckerlyc.pegboard.BMLBlockPeg;

/**
 * Interface for strategies to schedule BML blocks (that is: resolve the time constraints and add the behaviors to the appropiate plans). 
 * @author welberge
 */
public interface SchedulingStrategy
{
    /**
     * Schedules the behaviour block. That is: resolve the time constraints and add timedplanunit implementations of all behaviours to 
     * the plans (using the engines through the scheduler).
     */
    void schedule(BMLBlockComposition mechanism, BehaviourBlock bb, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler, double schedulingTime);
}
