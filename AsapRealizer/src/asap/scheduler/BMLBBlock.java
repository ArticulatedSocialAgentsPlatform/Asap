package asap.scheduler;

import hmi.elckerlyc.pegboard.BehaviorCluster;
import hmi.elckerlyc.pegboard.BehaviorKey;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.AbstractBMLBlock;
import hmi.elckerlyc.scheduler.BMLScheduler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableMap;

/**
 * Manages the state of a BML block used in ASAP.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class BMLBBlock extends AbstractBMLBlock
{
    private final Set<String> appendSet = new CopyOnWriteArraySet<String>();
    private final Set<String> onStartSet = new CopyOnWriteArraySet<String>();
    private final Set<String> chunkAfterSet = new CopyOnWriteArraySet<String>();
    private final PegBoard pegBoard;

    public BMLBBlock(String id, BMLScheduler s, PegBoard pb, Set<String> appendAfter, Set<String> onStart, Set<String> chunkAfter)
    {
        super(id, s);
        pegBoard = pb;
        appendSet.addAll(appendAfter);
        onStartSet.addAll(onStart);
        chunkAfterSet.addAll(chunkAfter);
    }

    public Set<String> getOnStartSet()
    {
        return Collections.unmodifiableSet(onStartSet);
    }

    public BMLBBlock(String id, BMLScheduler s, PegBoard pb)
    {
        this(id, s, pb, new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
    }
    
    
    //assumes that for all behaviors in the cluster, their start is resolved and listed on the PegBoard
    private void reAlignUngroundedCluster(BehaviorCluster cluster)
    {
        double shift = Double.MAX_VALUE;
        boolean shiftRequired = false;
        
        //one (or more) of the behaviors should start at local time 0. Find the timeshift that makes this happen 
        for(BehaviorKey bk:cluster.getBehaviors())
        {
            double startTime = pegBoard.getPegTime(bk.getBmlId(), bk.getBehaviorId(), "start");
            if(startTime<shift)
            {
                startTime = shift;
                shiftRequired = true;
            }
        }
        
        if(shiftRequired)
        {
            pegBoard.shiftCluster(cluster, -shift);            
        }        
    }

    private void reAlignBlock()
    {
        Set<BehaviorCluster> clusters = new HashSet<BehaviorCluster>();
        for (String behaviorId : scheduler.getBehaviours(getBMLId()))
        {
            clusters.add(pegBoard.getBehaviorCluster(bmlId, behaviorId));
        }

        for (BehaviorCluster bc : clusters)
        {
            if (bc.isGrounded()) continue;
            reAlignUngroundedCluster(bc);
        }
    }

    @Override
    public void start()
    {
        //scheduler.updateTiming(getBMLId());
        //reAlignBlock();
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
                log.debug("{} waiting for subsiding at {}", bmlId, scheduler.getSchedulingTime());
                return;
            }
        }
        log.debug("{} started at {}", bmlId, scheduler.getSchedulingTime());
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
            log.debug("bml block {} finished", bmlId);
            finish();
        }
    }

}
