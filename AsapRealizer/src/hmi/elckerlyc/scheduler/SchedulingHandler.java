package hmi.elckerlyc.scheduler;

import hmi.bml.core.BehaviourBlock;

public interface SchedulingHandler
{
    void schedule(BehaviourBlock bb, BMLScheduler scheduler);    
}
