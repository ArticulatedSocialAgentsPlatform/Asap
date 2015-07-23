/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Manages the state of BML blocks on the basis of behavior feedback, warnings and exceptions.
 * The BMLBBlockManager manages the transition from PENDING to LURKING;
 * all other transitions are managed in the BMLBBlock (extensions) themselves.
 * @author welberge
 */
@Slf4j
public final class BMLBlockManager
{
    private final ConcurrentHashMap<String, BMLBBlock> finishedBMLBBlocks = new ConcurrentHashMap<String, BMLBBlock>();

    private final ConcurrentHashMap<String, BMLBBlock> bmlBlocks = new ConcurrentHashMap<String, BMLBBlock>();

    private final SetMultimap<BehaviorKey, BMLSyncPointProgressFeedback> behaviorProgress;

    public BMLBlockManager()
    {
        HashMultimap<BehaviorKey, BMLSyncPointProgressFeedback> behaviorProgressMap = HashMultimap.create();
        behaviorProgress = Multimaps.synchronizedSetMultimap(HashMultimap.create(behaviorProgressMap));
    }

    private static class BehaviorKey
    {
        public BehaviorKey(String bmlId, String id)
        {
            this.id = id;
            this.bmlId = bmlId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof BehaviorKey)) return false;
            BehaviorKey pk = (BehaviorKey) o;
            return pk.bmlId.equals(bmlId) && pk.id.equals(id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(id, bmlId);
        }

        final String id;

        final String bmlId;
    }

    public synchronized void addBMLBlock(BMLBBlock bbm)
    {
        bmlBlocks.put(bbm.getBMLId(), bbm);
    }

    public synchronized void removeBMLBlock(String bmlId, double time)
    {
        bmlBlocks.remove(bmlId);
        finishedBMLBBlocks.remove(bmlId);        
        updateBlocks(time);
    }

    boolean isPending(String bmlId, Set<String> bmlIdsChecked)
    {
        BMLBBlock b = getBMLBlock(bmlId);
        if (b == null)
        {
            return false;
        }
        return b.isPending(bmlIdsChecked);
    }
    
    /**
     * A block is pending if this block or any of its append/chunkafter targets are pending
     */
    public boolean isPending(String bmlId)
    {
        BMLBBlock b = getBMLBlock(bmlId);
        if (b == null)
        {
            return false;
        }
        return b.isPending();
    }
    
    public void interruptBlock(String bmlId, double time)
    {
        BMLBBlock b = bmlBlocks.get(bmlId);
        if (b != null)
        {
            b.interrupt(time);
        }
    }
    
    public void finishBlock(String bmlId, double time)
    {
        BMLBBlock b = bmlBlocks.get(bmlId);
        if (b != null)
        {
            b.finish(time);
        }
    }

    public synchronized void startBlock(String bmlId, double time)
    {
        BMLBBlock b = bmlBlocks.get(bmlId);
        if (b != null)
        {
            b.start(time);
        }
    }

    /**
     * Get block state of BML block with id bmlId
     * 
     * @return state of the block, PlanUnitState.Done if the block is not (or no longer) in the list
     *         of blocks managed by the blockmanager
     */
    public synchronized TimedPlanUnitState getBMLBlockState(String bmlId)
    {
        BMLBBlock b = bmlBlocks.get(bmlId);
        if (b == null)
        {
            return TimedPlanUnitState.DONE;
        }
        return b.getState();
    }

    public synchronized Set<String> getBMLBlocks()
    {
        HashSet<String> bBlocks = new HashSet<String>();
        for (String bmlId : bmlBlocks.keySet())
        {
            bBlocks.add(bmlId);
        }
        return bBlocks;
    }

    private ImmutableMap<String, TimedPlanUnitState> getBlockStates()
    {
        Map<String, TimedPlanUnitState> blockStates = new HashMap<String, TimedPlanUnitState>();
        for (BMLBBlock block : bmlBlocks.values())
        {
            blockStates.put(block.getBMLId(), block.getState());
        }
        return ImmutableMap.copyOf(blockStates);
    }

    public synchronized void updateBlocks(double time)
    {
        ImmutableMap<String, TimedPlanUnitState> m = getBlockStates();
        for (BMLBBlock block : bmlBlocks.values())
        {
            block.update(m, time);
        }
    }

    public BMLBBlock getBMLBlock(String bmlId)
    {
        return bmlBlocks.get(bmlId);
    }

    public synchronized void clear()
    {
        finishedBMLBBlocks.clear();
        bmlBlocks.clear();
        behaviorProgress.clear();
    }

    public synchronized void activateBlock(String bmlId, double time)
    {
        BMLBBlock bb = bmlBlocks.get(bmlId);
        if (bb == null)
        {
            log.warn("Attempting to activate unknown block {}", bmlId);
            return;
        }
        bb.activate();
        updateBlocks(time);
    }

    public synchronized void blockProgress(BMLBlockProgressFeedback psf)
    {
        if (psf.getSyncId().equals("end"))
        {
            BMLBBlock block = bmlBlocks.get(psf.getBmlId());
            if (block == null)
            {
                log.warn("Performance stop of block " + psf.getBmlId() + " not managed by the BMLBBlockManager");
                return;
            }
        }
        updateBlocks(psf.getGlobalTime());
    }

    public synchronized void warn(BMLWarningFeedback bw, double time)
    {
        String idSplit[] = bw.getId().split(":");
        if (idSplit.length == 2)
        {
            for (BMLBBlock block : bmlBlocks.values())
            {
                if (block.getBMLId().equals(idSplit[0]))
                {
                    block.dropBehaviour(idSplit[1]);
                }
            }
        }
        updateBlocks(time);
    }

    public synchronized void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        behaviorProgress.put(new BehaviorKey(spp.getBMLId(), spp.getBehaviourId()), spp);
        log.debug("Adding sync {}:{}:{} to behaviorProgress", new String[] { spp.getBMLId(), spp.getBehaviourId(), spp.getSyncId() });
        for (BMLBBlock block : bmlBlocks.values())
        {
            if (block.getBMLId().equals(spp.getBMLId()))
            {
                block.behaviorProgress(spp.getBehaviourId(), spp.getSyncId());
            }
        }
        updateBlocks(spp.getGlobalTime());
    }

    /**
     * Get a Immutable copy of the set of progress messages send for a certain behavior
     */
    public ImmutableSet<BMLSyncPointProgressFeedback> getSyncProgress(String bmlId, String behaviorId)
    {
        synchronized (behaviorProgress)
        {
            return ImmutableSet.copyOf(behaviorProgress.get(new BehaviorKey(bmlId, behaviorId)));
        }
    }

    /**
     * Get the ImmutableSet of syncs that are finished for a certain behavior
     */
    public ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId)
    {
        Set<String> progress = new HashSet<String>();
        synchronized (behaviorProgress)
        {
            Set<BMLSyncPointProgressFeedback> sppf = behaviorProgress.get(new BehaviorKey(bmlId, behaviorId));
            for (BMLSyncPointProgressFeedback spp : sppf)
            {
                progress.add(spp.getSyncId());
            }
        }
        return ImmutableSet.copyOf(progress);
    }
    
    public synchronized void predictionUpdate(String bmlId)
    {
        for (BMLBBlock block : bmlBlocks.values())
        {
            block.predictionUpdate(bmlId);
        }
    }
}
