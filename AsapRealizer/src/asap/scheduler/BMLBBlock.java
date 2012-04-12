package asap.scheduler;

import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.AbstractBMLBlock;
import hmi.elckerlyc.scheduler.BMLScheduler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Manages the state of a BML block used in ASAP.
 * @author hvanwelbergen
 * 
 */
public class BMLBBlock extends AbstractBMLBlock
{
    private final Set<String> appendSet = new CopyOnWriteArraySet<String>();
    private final Set<String> onStartSet = new CopyOnWriteArraySet<String>();
    private final Set<String> chunkAfterSet = new CopyOnWriteArraySet<String>();
    private static final Logger logger = LoggerFactory.getLogger(BMLBBlock.class.getName());

    public BMLBBlock(String id, BMLScheduler s, Set<String> appendAfter, Set<String> onStart, Set<String> chunkAfter)
    {
        super(id, s);
        appendSet.addAll(appendAfter);
        onStartSet.addAll(onStart);
        chunkAfterSet.addAll(chunkAfter);
    }

    public Set<String> getOnStartSet()
    {
        return Collections.unmodifiableSet(onStartSet);
    }

    public BMLBBlock(String id, BMLScheduler s)
    {
        this(id, s, new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
    }

    private void reAlignBlock()
    {

        // TODO: move all behavior clusters as far to the 'left' as possible
    }

    @Override
    public void start()
    {
        scheduler.updateTiming(getBMLId());
        reAlignBlock();
        super.start();
        activateOnStartBlocks();
    }

    public void update(ImmutableMap<String, TimedPlanUnitState> allBlocks)
    {
        if (state.get() == TimedPlanUnitState.LURKING)
        {
            updateFromLurking(allBlocks);
        }
        else if (state.get() == TimedPlanUnitState.IN_EXEC || state.get() == TimedPlanUnitState.SUBSIDING)
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
    private void updateFromLurking(ImmutableMap<String, TimedPlanUnitState> allBlocks)
    {
        appendSet.retainAll(allBlocks.keySet());
        chunkAfterSet.retainAll(allBlocks.keySet());

        for (String apId : appendSet)
        {
            if (!allBlocks.get(apId).isDone())
            {
                return;
            }
        }
        for (String cuId : chunkAfterSet)
        {
            if (!allBlocks.get(cuId).isSubsidingOrDone())
            {
                logger.debug("{} waiting for subsiding at {}", bmlId, scheduler.getSchedulingTime());
                return;
            }
        }
        logger.debug("{} started at {}", bmlId, scheduler.getSchedulingTime());
        scheduler.startBlock(bmlId);
    }

    private void updateFromExecOrSubSiding()
    {
        if (getState() != TimedPlanUnitState.SUBSIDING && isSubsiding())
        {
            state.set(TimedPlanUnitState.SUBSIDING);
            scheduler.updateBMLBlocks();
        }
        if (getState() != TimedPlanUnitState.DONE && isFinished())
        {
            logger.debug("bml block {} finished", bmlId);
            finish();
        }
    }

}
