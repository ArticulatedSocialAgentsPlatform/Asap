package hmi.elckerlyc.scheduler;

import hmi.elckerlyc.planunit.TimedPlanUnitState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Captures the feedback of behaviors of a BML Block and sends the BML stop feedback when the block
 * is finished. A BML block is finished when:<br>
 * 1. For all behaviors in the block with known end time, the behavior end feedback was sent and
 * time &gt; end time<br>
 * 2. And for all behaviors with unknown end time, feedback was received on all their set sync
 * points (=TimePegs)<br>
 * 
 * @author welberge
 */
public class BMLTBlock extends AbstractBMLBlock
{
    private final Set<String> appendSet = new CopyOnWriteArraySet<String>();

    private final Set<String> onStartSet = new CopyOnWriteArraySet<String>();
    
    private static final Logger logger = LoggerFactory.getLogger(BMLTBlock.class.getName());
    /**
     * @return an unmodifiable copy of the onStartSet
     */
    public Set<String> getOnStartSet()
    {
        return Collections.unmodifiableSet(onStartSet);
    }

    public BMLTBlock(String id, BMLScheduler s, Set<String> appendAfter, Set<String> onStart)
    {
        super(id,s);
        appendSet.addAll(appendAfter);
        onStartSet.addAll(onStart);
    }

    public BMLTBlock(String id, BMLScheduler s)
    {
        this(id, s, new HashSet<String>(), new HashSet<String>());
    }

    @Override
    public void start()
    {
        super.start();
        activateOnStartBlocks();        
    }
    
    public void update(ImmutableMap<String,TimedPlanUnitState> allBlocks)
    {
        if(state.get() == TimedPlanUnitState.LURKING)
        {
            updateFromLurking(allBlocks);
        }
        else if(state.get() == TimedPlanUnitState.IN_EXEC || state.get() == TimedPlanUnitState.SUBSIDING)
        {
            updateFromExecOrSubSiding();
        }        
    }
    
    private void activateOnStartBlocks()
    {
        for (String id : getOnStartSet())
        {
            if (scheduler.getBMLBlockState(id).equals(TimedPlanUnitState.PENDING))
            {
                scheduler.activateBlock(id);
            }
        }
    }
    /**
     * Starts block if all its append targets are finished and it is in lurking state
     */
    private void updateFromLurking(ImmutableMap<String,TimedPlanUnitState> allBlocks)
    {
        appendSet.retainAll(allBlocks.keySet());
        for (String apId : appendSet)
        {
            if (!allBlocks.get(apId).isDone())
            {
                return;
            }
        }    
        scheduler.startBlock(bmlId);        
    }   
    
    
    private void updateFromExecOrSubSiding()
    {
        if(state.get()!=TimedPlanUnitState.SUBSIDING && isSubsiding())
        {
            state.set(TimedPlanUnitState.SUBSIDING);            
            scheduler.updateBMLBlocks();            
        }
        if (state.get()!=TimedPlanUnitState.DONE && isFinished())
        {
            logger.debug("bml block {} finished", bmlId);            
            finish();            
        }
    }    
}
