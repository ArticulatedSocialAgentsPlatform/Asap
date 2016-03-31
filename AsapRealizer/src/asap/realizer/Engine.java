/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Interface for the combined functionality of a Player, PlanManager and Planner
 * @author welberge
 */
public interface Engine
{
    boolean containsBehaviour(String bmlId, String behId);

    boolean containsMainBehaviour(String bmlId, String behId);
    /**
     * Updates the timing for all behaviors in bml block bmlId to reflect the current execution context
     * (e.g. position of limbs).
     */
    void updateTiming(String bmlId);

    /**
     * Get a string containing bmlId:behaviorId of all invalid behaviours
     */
    Set<String> getInvalidBehaviours();

    /**
     * Get the behaviors with bmlId
     */
    Set<String> getBehaviours(String bmlId);

    /**
     * Creates an offsetpeg for bmlId:behId:syncId, this peg is linked to the TimePeg in this
     * behaviour
     * 
     * @throws BehaviorNotFoundException
     *             bmlId:behId does not exist
     * @throws SyncPointNotFoundException
     *             syncId is not valid
     * @throws TimePegAlreadySetException
     *             bmlId:behId:syncId is already linked to a TimePeg
     */
    OffsetPeg createOffsetPeg(String bmlId, String behId, String syncId) throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException;

    String getParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException;

    float getFloatParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException;

    void setParameterValue(String bmlId, String behId, String paramId, String value) throws ParameterException, BehaviorNotFoundException;

    void setFloatParameterValue(String bmlId, String behId, String paramId, float value) throws ParameterException,
            BehaviorNotFoundException;

    double getEndTime(String bmlId, String behId);

    /**
     * Get the (predicted) end time of a planned BML block
     */
    double getBlockEndTime(String bmlId);
    
    /**
     * Get the (predicted) start time of the subsiding phase of a planned BML block
     */
    double getBlockSubsidingTime(String bmlId);

    /**
     * Stops all behaviors in the block; that is: calls their stop and removes them from the plan
     */
    void stopBehaviourBlock(String bmlId, double time);

    /**
     * Stops a behavior. That is: calls their stop and removes them from the plan
     */
    void stopBehaviour(String bmlId, String behaviourId, double time);
    
    /**
     * Gracefully interrupts all behaviors in the block; that is: calls their interrupt
     */
    void interruptBehaviourBlock(String bmlId, double time);

    /**
     * Gracefully interrupts a behavior. That is: calls their interrupt
     */
    void interruptBehaviour(String bmlId, String behaviourId, double time);

    /**
     * Adds a behavior to the plan. All timepegs in sac must be resolved.
     * 
     * @param planElement planElement obtained from resolveSynchs, null to create a new planElement
     * @return a list of all syncs of the behavior and their linked TimePegs
     * @throws BehaviourPlanningException if no behavior satisfying sac can be constructed
     */
    List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac, TimedPlanUnit planElement)
            throws BehaviourPlanningException;

    /**
     * Resolves TimePegs for behavior b, given some known time pegs and constraints
     * 
     * @param b the behavior
     * @param sac the provided time pegs and constraints, missing constraints are filled out by this method
     * @return the object to be placed in the plan (a TimedMotionUnit, SpeechUnit, ...)
     * @throws BehaviourPlanningException if no behavior satisfying sac can be constructed
     */
    TimedPlanUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException;

    List<Class<? extends Behaviour>> getSupportedBehaviours();

    List<Class<? extends Behaviour>> getSupportedDescriptionExtensions();

    /**
     * Reset the engine, that is: stops all behaviors, then removes them from the plan and restores the startup state (default pose etc).
     */
    void reset(double time);

    /**
     * Clean up resources constructed with the planner (e.g. for native stuff, spawned threads, ...). A planner should not be (re)used after shutting
     * it down.
     */
    void shutdown();

    /**
     * Set the state of all behaviors with bmlId bmlId in the plan to state
     */
    void setBMLBlockState(String bmlId, TimedPlanUnitState state);

    void play(double time);

    /**
     * @return : 0 behavior should adjust completely to the timing of other behaviors,
     *         1:completely inflexible
     */
    double getRigidity(Behaviour beh);

    void setId(String newId);

    String getId();

    
}
