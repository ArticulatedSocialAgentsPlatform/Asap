package asap.realizer.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.pegboard.BehaviorCluster;
import asap.realizer.pegboard.BehaviorKey;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Manages the state of a BML block used in ASAP.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class BMLBBlock extends AbstractBMLBlock
{
    private final Set<String> appendSet = new CopyOnWriteArraySet<String>();
    private final List<String> onStartList = new CopyOnWriteArrayList<String>();
    private final Set<String> chunkAfterSet = new CopyOnWriteArraySet<String>();
    private final PegBoard pegBoard;

    public BMLBBlock(String id, BMLScheduler s, PegBoard pb, Set<String> appendAfter, List<String> onStart, Set<String> chunkAfter)
    {
        super(id, s);
        pegBoard = pb;
        appendSet.addAll(appendAfter);
        onStartList.addAll(onStart);
        chunkAfterSet.addAll(chunkAfter);
    }

   

    @Override
    public boolean isPending(Set<String> checked)
    {
        if (super.isPending()) return true;
        if (isPending(chunkAfterSet,checked))return true;
        if (isPending(appendSet, checked))return true;
        return false;
    }
    
    @Override
    public boolean isPending()
    {
        if (super.isPending()) return true;
        Set<String> checked = new HashSet<String>();
        checked.add(bmlId);
        if (isPending(Sets.difference(chunkAfterSet,checked),checked))return true;
        if (isPending(Sets.difference(appendSet,checked), checked))return true;
        return false;
    }

    public void addChunkTarget(String bmlId)
    {
        chunkAfterSet.add(bmlId);
    }

    public void addAppendTarget(String bmlId)
    {
        appendSet.add(bmlId);
    }

    public List<String> getOnStartSet()
    {
        return Collections.unmodifiableList(onStartList);
    }

    public Set<String> getAppendSet()
    {
        return Collections.unmodifiableSet(appendSet);
    }

    public Set<String> getChunkAfterSet()
    {
        return Collections.unmodifiableSet(chunkAfterSet);
    }

    public BMLBBlock(String id, BMLScheduler s, PegBoard pb)
    {
        this(id, s, pb, new HashSet<String>(), new ArrayList<String>(), new HashSet<String>());
    }

    // assumes that for all behaviors in the cluster, their start is resolved and listed on the PegBoard
    private void reAlignUngroundedCluster(BehaviorCluster cluster)
    {
        double shift = Double.MAX_VALUE;
        boolean shiftRequired = false;

        // one (or more) of the behaviors should start at local time 0. Find the timeshift that makes this happen
        for (BehaviorKey bk : cluster.getBehaviors())
        {
            double startTime = pegBoard.getRelativePegTime(bmlId, bk.getBmlId(), bk.getBehaviorId(), "start");
            if (startTime == TimePeg.VALUE_UNKNOWN)
            {
                log.warn("Skipping realignment of behavior {}:{}, start peg not set.", bk.getBmlId(), bk.getBehaviorId());
            }
            else if (startTime < shift)
            {
                shift = startTime;
                log.debug("TimeShift from {}:{} = {}", new Object[] { bk.getBmlId(), bk.getBehaviorId(), "" + shift });
                shiftRequired = true;
            }
        }

        if (shiftRequired)
        {
            log.debug("TimeShift set: {}", shift);
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
