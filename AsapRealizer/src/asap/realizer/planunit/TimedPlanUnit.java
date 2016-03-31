/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.List;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Units of the Unimodal Plan normally used in an Engine.
 * The timing of PlanUnits is linked to TimePegs on the PegBoard.
 * PlanUnits have a state (e.g. pending, lurking, executing, subsiding)
 * and can be executed at a global time moment getStartTime() &lt; t &lt; getEndime()
 * They send BMLSyncPointProgressFeedback feedback on their progress to listeners registered to such feedback.
 * @author hvanwelbergen
 */
public interface TimedPlanUnit
{
    /**
     * Priority of the TimedPlanUnit. It is up to the Player to make execution decisions based upon this value.
     * For example, a Player may decide to drop the TimedPlanUnit with the lowest priority if two TimedPlanUnit conflict.
     * The priority should be interpreted as the current priority of the TimedPlanUnit and
     * might change over time (e.g. the priority of TimedPlanUnits may drop in their subsiding phase).
     */
    int getPriority();

    void setPriority(int priority);

    /**
     * Get the BML block to which this PlanUnit belongs
     */
    BMLBlockPeg getBMLBlockPeg();

    /**
     * Get the behaviour id of the PlanUnit
     */
    String getId();

    /**
     * Get the BML block id of the PlanUnit
     */
    String getBMLId();

    /**
     * Send feedback to all feedback listeners
     * 
     * @param fb
     *            feedback to send
     */
    void feedback(BMLSyncPointProgressFeedback fb);

    /**
     * Set the plan unit state
     */
    void setState(TimedPlanUnitState newState);

    /**
     * Get the plan unit state
     */
    TimedPlanUnitState getState();

    /**
     * Starts the Plan unit, is called only once before the first play is
     * called.
     * 
     * @param time
     *            global time, used for feedback
     * @throws TimedPlanUnitPlayException
     */
    void start(double time) throws TimedPlanUnitPlayException;

    /**
     * Stop the Plan unit.
     * 
     * @param time
     *            global time, used for feedback
     * @throws TimedPlanUnitPlayException
     */
    void stop(double time) throws TimedPlanUnitPlayException;

    /**
     * Gracefully stops the PlanUnit
     * @param time
     */
    void interrupt(double time) throws TimedPlanUnitPlayException;

    /**
     * Plays the unit at global time time. Is allowed to be a blocking call.
     */
    void play(double time) throws TimedPlanUnitPlayException;

    /**
     * Triggers bottom-up updates to the timing of the planunit
     */
    void updateTiming(double time) throws TimedPlanUnitPlayException;

    /**
     * Get the global start time of the PlanUnit, TimePeg.VALUEUNKNOWN if not
     * known (yet)
     */
    double getStartTime();

    /**
     * Get the global end time of the PlanUnit, TimePeg.VALUEUNKNOWN if not
     * known (yet)
     */
    double getEndTime();

    /**
     * Get the timing of the relax phase TimePeg.VALUEUNKNOWN if not
     * known (yet)
     */
    double getRelaxTime();

    /**
     * Get the global time of sync syncId of the PlanUnit, TimePeg.VALUEUNKNOWN
     * if not known (yet)
     */
    double getTime(String syncId);

    /**
     * Get the list of sync ids that can be used in this behavior. The list is
     * ordered in relative time and maintains the BML order for BML syncIds. All
     * BML syncs should be present in this list.
     */
    List<String> getAvailableSyncs();

    /**
     * Get the relative timing of this sync id Implementing classes should at
     * least provide valid relative times for the BML sync points
     * 
     * @return relative time in range [0..1]
     */
    double getRelativeTime(String syncId) throws SyncPointNotFoundException;

    
    /**
     * Link timepegs to those in the TimedPlanUnit
     */
    void linkSynchs(List<TimePegAndConstraint> sacs);
    
    /**
     * null is not set TODO: throw exception on not set instead?
     */
    TimePeg getTimePeg(String syncId);

    /**
     * Assigns a TimePeg to a sync in the PlanUnit
     */
    void setTimePeg(String syncId, TimePeg peg);

    /**
     * State is lurking?
     */
    boolean isLurking();

    /**
     * State is done?
     */
    boolean isDone();

    /**
     * State is pending?
     */
    boolean isPending();

    /**
     * Return true if this unit is a subunit of another planunit (e.g. a visime
     * or speech-related timed motion unit)
     */
    boolean isSubUnit();

    /**
     * 0 is unknown/persistent
     */
    double getPreferedDuration();

    /**
     * In PlanUnitState.IN_EXEC or PlanUnitState.SUBSIDING
     * 
     * @return
     */
    boolean isPlaying();

    /**
     * In PlanUnitState.SUBSIDING
     */
    boolean isSubsiding();

    /**
     * In PlanUnitState.IN_PREP
     */
    boolean isInPrep();

    /**
     * Checks if the timing of this plan unit is 'valid' (e.g. stuff like start
     * is earlier than end, but also planunit specific stuff like
     * biomechanically possible timing).
     * 
     * @return true if valid
     */
    boolean hasValidTiming();

    /**
     * Sets a parameter value
     */
    void setParameterValue(String paramId, String value) throws ParameterException;

    /**
     * Sets a parameter value
     */
    void setFloatParameterValue(String paramId, float value) throws ParameterException;

    /**
     * Gets a float parameter value
     */
    float getFloatParameterValue(String paramId) throws ParameterException;

    /**
     * Gets a float parameter value
     */
    String getParameterValue(String paramId) throws ParameterException;

}
