/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import saiba.bml.core.BehaviourBlock;

/**
 * Schedules BehaviourBlock bb (e.g. by interfacing with several Engines through the scheduler)
 * @author welberge
 */
public interface SchedulingHandler
{
    void schedule(BehaviourBlock bb, BMLScheduler scheduler, double time);    
}
