/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.BMLGestureSync;
import asap.bml.ext.bmla.feedback.BMLABlockStatus;
import asap.realizer.pegboard.BehaviorCluster;
import asap.realizer.pegboard.BehaviorKey;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Manages the state of a BML block used in ASAP.
 * Captures the feedback of behaviors of a BML Block, and update the BML block state accordingly.
 * @author hvanwelbergen 
 */
@Slf4j
public class BMLBBlock
{
    private final String bmlId;
    private final BMLScheduler scheduler;
    private AtomicReference<TimedPlanUnitState> state = new AtomicReference<TimedPlanUnitState>();
    private final Set<String> droppedBehaviours = new CopyOnWriteArraySet<String>();
    private final ConcurrentHashMap<String, Set<String>> behaviorSyncsPassed = new ConcurrentHashMap<String, Set<String>>();

    
    private final Set<String> appendSet = new CopyOnWriteArraySet<String>();
    private final List<String> onStartList = new CopyOnWriteArrayList<String>();
    private final Set<String> chunkAfterSet = new CopyOnWriteArraySet<String>();
    private final PegBoard pegBoard;
    

    public BMLBBlock(String id, BMLScheduler s, PegBoard pb, Set<String> appendAfter, List<String> onStart, Set<String> chunkAfter)
    {
        bmlId = id;
        scheduler = s;
        state.set(TimedPlanUnitState.IN_PREP);
        pegBoard = pb;
        appendSet.addAll(appendAfter);
        onStartList.addAll(onStart);
        chunkAfterSet.addAll(chunkAfter);
    }

    /**
     * @return the bmlId
     */
    public String getBMLId()
    {
        return bmlId;
    }
    
    /**
     * @param state
     *            the state to set
     */
    public void setState(TimedPlanUnitState state)
    {
        this.state.set(state);
    }
    
    public TimedPlanUnitState getState()
    {
        return state.get();
    }
    
    public boolean isPending(Set<String> checked)
    {
        if (getState().equals(TimedPlanUnitState.PENDING)) return true;
        if (isPending(chunkAfterSet, checked)) return true;
        if (isPending(appendSet, checked)) return true;
        return false;
    }

    public boolean isPending(Set<String> ids, Set<String> checked)
    {
        for (String bmlId : ids)
        {
            if(!checked.contains(bmlId))
            {
                if (scheduler.isPending(bmlId,checked))
                {
                    return true;
                }
                checked.add(bmlId);
            }
        }
        return false;
    }
    
    public boolean isPending()
    {
        if (getState().equals(TimedPlanUnitState.PENDING)) return true;
        Set<String> checked = new HashSet<String>();
        checked.add(bmlId);
        if (isPending(Sets.difference(chunkAfterSet, checked), checked)) return true;
        if (isPending(Sets.difference(appendSet, checked), checked)) return true;
        return false;
    }

    public void addChunkTarget(String bmlId)
    {
        chunkAfterSet.add(bmlId);
    }

    public void addAppendTarget(String bmlId)
    {
        appendSet.add(bmlId);
        scheduler.updatePredictions(this.bmlId);
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

    /**
     * Set IN_EXEC state and generate appropriate feedback 
     */
    public void start(double time)
    {
        scheduler.updateTiming(getBMLId());
        reAlignBlock();
        state.set(TimedPlanUnitState.IN_EXEC);
        scheduler.blockStartFeedback(bmlId, time);
        activateOnStartBlocks(time);
    }

    /**
     * Called to potentially update the BMLBlock's state 
     */
    public void update(ImmutableMap<String, TimedPlanUnitState> allBlocks, double time)
    {
        if (state.get() == TimedPlanUnitState.LURKING)
        {
            updateFromLurking(allBlocks, time);
        }
        else if (state.get() == TimedPlanUnitState.IN_EXEC || state.get() == TimedPlanUnitState.SUBSIDING)
        {
            updateFromExecOrSubSiding(time);
        }
    }

    private void activateOnStartBlocks(double time)
    {
        for (String id : getOnStartSet())
        {
            if (scheduler.getBMLBlockState(id).equals(TimedPlanUnitState.PENDING))
            {
                scheduler.activateBlock(id, time);
            }
        }
    }

    /**
     * Starts block if all its append targets are finished and it is in lurking state
     */
    private void updateFromLurking(ImmutableMap<String, TimedPlanUnitState> allBlocks, double time)
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
        log.debug("{} started at {}", bmlId, time);
        scheduler.startBlock(bmlId, time);
    }

    private void updateFromExecOrSubSiding(double time)
    {
        if (getState() != TimedPlanUnitState.SUBSIDING && isSubsiding())
        {
            state.set(TimedPlanUnitState.SUBSIDING);
            scheduler.updateBMLBlocks(time);
        }
        if (getState() != TimedPlanUnitState.DONE && isFinished())
        {
            log.debug("bml block {} finished", bmlId);
            finish(time);
        }
    }

    private boolean isFinished()
    {
        for (String behId : scheduler.getBehaviours(bmlId))
        {
            if (droppedBehaviours.contains(behId)) continue;
            log.debug("checking isFinished {}:{}", bmlId, behId);

            Set<String> finishedInfo = behaviorSyncsPassed.get(behId);
            if (finishedInfo == null)
            {
                return false;
            }            
            if (!finishedInfo.contains(BMLGestureSync.END.getId()))
            {
                return false;
            }
        }
        return true;
    }
    
    public void interrupt(double time)
    {
        TimedPlanUnitState prevState = state.getAndSet(TimedPlanUnitState.DONE);
        
        if(prevState.isPlaying())
        {
            scheduler.blockStopFeedback(bmlId, BMLABlockStatus.INTERRUPTED, time);
        }
        else if(prevState!=TimedPlanUnitState.DONE)
        {
            scheduler.blockStopFeedback(bmlId, BMLABlockStatus.REVOKED, time);
        }
    }
    
    /**
     * Set DONE state and generate appropriate feedback
     */
    public void finish(double time)
    {
        if(state.getAndSet(TimedPlanUnitState.DONE)!=TimedPlanUnitState.DONE)
        {
            scheduler.blockStopFeedback(bmlId, BMLABlockStatus.DONE, time);
        }
    }
    
    private boolean isSubsiding()
    {
        for (String behId : scheduler.getBehaviours(bmlId))
        {
            if (droppedBehaviours.contains(behId)) continue;
            Set<String> finishedInfo = behaviorSyncsPassed.get(behId);
            if (finishedInfo == null)
            {
                return false;
            }
            if (!finishedInfo.contains(BMLGestureSync.RELAX.getId()) && !finishedInfo.contains("end"))
            {
                return false;
            }
        }
        log.debug("{} is subsiding at {}", bmlId, scheduler.getSchedulingTime());
        return true;
    }
    
    /**
     * Called to inform the BMLBlock that on of its behaviors is dropped 
     */
    public void dropBehaviour(String beh)
    {
        droppedBehaviours.add(beh);
    }
    
    /**
     * Called to inform the BMLBlock that sync point behaviorId:syncId has occurred 
     */
    public void behaviorProgress(String behaviorId, String syncId)
    {
        Set<String> newSet = new HashSet<String>();
        Set<String> behInfo = behaviorSyncsPassed.putIfAbsent(behaviorId, newSet);
        if (behInfo == null) behInfo = newSet;
        behInfo.add(syncId);
    }
    
    /**
     * Set Lurking state
     */
    public void activate()
    {
        state.set(TimedPlanUnitState.LURKING);
    }
    
    /**
     * Prediction of timing block bmlId is updated
     */
    public void predictionUpdate(String bmlId)
    {
        if (bmlId.equals(getBMLId()))
        {
            return;
        }
        
        if(getAppendSet().contains(bmlId) || getChunkAfterSet().contains(bmlId))
        {
            scheduler.updatePredictions(getBMLId());
        }
    }
}
