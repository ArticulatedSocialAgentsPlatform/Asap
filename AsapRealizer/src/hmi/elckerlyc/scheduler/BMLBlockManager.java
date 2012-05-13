package hmi.elckerlyc.scheduler;

import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.BMLPerformanceStartFeedback;
import saiba.bml.feedback.BMLPerformanceStopFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import hmi.elckerlyc.planunit.TimedPlanUnitState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Manages the state of BML blocks on the basis of behavior feedback, warnings and exceptions.
 * The BMLBlockManager manages the transition from PENDING to LURKING;
 * all other transitions are managed in the BMLBlock (extensions) themselves.
 * @author welberge
 */
public final class BMLBlockManager
{
    private final ConcurrentHashMap<String, BMLBlock> finishedBMLBlocks = new ConcurrentHashMap<String, BMLBlock>();

    private final ConcurrentHashMap<String, BMLBlock> BMLBlocks = new ConcurrentHashMap<String, BMLBlock>();

    private final Logger logger = LoggerFactory.getLogger(BMLBlockManager.class.getName());

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

    public synchronized void addBMLBlock(BMLBlock bbm)
    {
        BMLBlocks.put(bbm.getBMLId(), bbm);
    }

    public synchronized void removeBMLBlock(String bmlId)
    {
        BMLBlocks.remove(bmlId);
        finishedBMLBlocks.remove(bmlId);
        updateBlocks();
    }

    public void finishBlock(String bmlId)
    {
        BMLBlock b = BMLBlocks.get(bmlId);
        if (b != null)
        {
            b.finish();
        }
    }

    public synchronized void startBlock(String bmlId)
    {
        BMLBlock b = BMLBlocks.get(bmlId);
        if (b != null)
        {
            b.start();
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
        BMLBlock b = BMLBlocks.get(bmlId);
        if (b == null)
        {
            return TimedPlanUnitState.DONE;
        }
        return b.getState();
    }

    public synchronized Set<String> getBMLBlocks()
    {
        HashSet<String> bmlBlocks = new HashSet<String>();
        for (String bmlId : BMLBlocks.keySet())
        {
            bmlBlocks.add(bmlId);
        }
        return bmlBlocks;
    }

    private ImmutableMap<String, TimedPlanUnitState> getBlockStates()
    {
        Map<String, TimedPlanUnitState> blockStates = new HashMap<String, TimedPlanUnitState>();
        for (BMLBlock block : BMLBlocks.values())
        {
            blockStates.put(block.getBMLId(), block.getState());
        }
        return ImmutableMap.copyOf(blockStates);
    }

    public synchronized void updateBlocks()
    {
        ImmutableMap<String, TimedPlanUnitState> m = getBlockStates();
        for (BMLBlock block : BMLBlocks.values())
        {
            block.update(m);
        }
    }

    public synchronized void clear()
    {
        finishedBMLBlocks.clear();
        BMLBlocks.clear();
        behaviorProgress.clear();
    }

    public synchronized void activateBlock(String bmlId)
    {
        BMLBlock bb = BMLBlocks.get(bmlId);
        if (bb == null)
        {
            logger.warn("Attempting to activate unknown block {}", bmlId);
            return;
        }
        bb.activate();
        updateBlocks();
    }

    public synchronized void performanceStop(BMLPerformanceStopFeedback psf)
    {
        BMLBlock block = BMLBlocks.get(psf.bmlId);
        if (block == null)
        {
            logger.warn("Performance stop of block " + psf.bmlId + " not managed by the BMLBlockManager");
            return;
        }
        updateBlocks();
    }

    public synchronized void performanceStart(BMLPerformanceStartFeedback psf)
    {
        updateBlocks();
    }

    public synchronized void warn(BMLWarningFeedback bw)
    {
        updateBlocks();

    }

    public synchronized void exception(BMLExceptionFeedback be)
    {
        for (BMLBlock block : BMLBlocks.values())
        {
            if (block.getBMLId().equals(be.bmlId))
            {
                block.dropBehaviours(be.failedBehaviours);
            }
        }
        updateBlocks();
    }

    public synchronized void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        behaviorProgress.put(new BehaviorKey(spp.bmlId, spp.behaviorId), spp);
        logger.debug("Adding sync {}:{}:{} to behaviorProgress", new String[] { spp.bmlId, spp.behaviorId, spp.syncId });
        for (BMLBlock block : BMLBlocks.values())
        {
            if (block.getBMLId().equals(spp.bmlId))
            {
                block.behaviorProgress(spp.behaviorId, spp.syncId);
            }
        }
        updateBlocks();
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
                progress.add(spp.syncId);
            }
        }
        return ImmutableSet.copyOf(progress);
    }
}
