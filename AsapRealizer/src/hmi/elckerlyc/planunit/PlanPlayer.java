package hmi.elckerlyc.planunit;

/**
 * Handles the playback of a single behaviour plan 
 * @author welberge
 */
public interface PlanPlayer
{
    void play(double time);

    void interruptPlanUnit(String bmlId, String id, double globalTime);

    void interruptBehaviourBlock(String bmlId, double time);

    void reset(double time);

    void setBMLBlockState(String bmlId, TimedPlanUnitState state);

    void shutdown();
}
