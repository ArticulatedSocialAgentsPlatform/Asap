/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Handles the playback of a single behaviour plan 
 * @author welberge
 */
public interface PlanPlayer
{
    void play(double time);

    void updateTiming(String bmlId);
    
    void stopPlanUnit(String bmlId, String id, double globalTime);

    void stopBehaviourBlock(String bmlId, double time);
    
    /**
     * Gracefully interrupts the planunit     
     */
    void interruptPlanUnit(String bmlId, String id, double globalTime);

    /**
     * Gracefully interrupts the behaviour block
     */
    void interruptBehaviourBlock(String bmlId, double time);

    void reset(double time);

    void setBMLBlockState(String bmlId, TimedPlanUnitState state);

    void shutdown();
}
