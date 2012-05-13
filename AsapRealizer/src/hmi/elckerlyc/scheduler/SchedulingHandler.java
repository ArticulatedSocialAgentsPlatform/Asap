package hmi.elckerlyc.scheduler;

import saiba.bml.core.BehaviourBlock;

public interface SchedulingHandler
{
    void schedule(BehaviourBlock bb, BMLScheduler scheduler);    
}
