package asap.realizer.scheduler;


import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableMap;

/**
 * Implementations should captures the feedback of behaviors of a BML Block, and update the BML block state accordingly.
 */
public interface BMLBlock
{
    /**
     * Set Lurking state
     */
    void activate();
    
    /**
     * Set IN_EXEC state and generate appropriate feedback 
     */
    void start();
    
    /**
     * Set DONE state and generate appropriate feedback
     */
    void finish();
    
    /**
     * Called to inform the BMLBlock that sync point behaviorId:syncId has occurred 
     */
    void behaviorProgress(String behaviorId, String syncId);
    
    /**
     * Called to inform the BMLBlock that on of its behaviors is dropped 
     */
    void dropBehaviour(String behs);
    
    /**
     * Called to potentially update the BMLBlock's state 
     */
    void update(ImmutableMap<String,TimedPlanUnitState> allBlocks);
    
    boolean isPending();
    
    String getBMLId();
    void setState(TimedPlanUnitState state);
    TimedPlanUnitState getState();
}
