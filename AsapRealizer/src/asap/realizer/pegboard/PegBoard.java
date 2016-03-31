/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import asap.realizer.SyncAndTimePeg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Keeps track of TimePegs and BMLBlockPegs
 * @author Herwin van Welbergen
 */
@ThreadSafe
@Slf4j
public final class PegBoard
{
    private final TimePegMap pegs = new TimePegMap();
    private final ConcurrentHashMap<String, BMLBlockPeg> bmlBlockPegs = new ConcurrentHashMap<String, BMLBlockPeg>();
    
    public PegBoard()
    {
        bmlBlockPegs.put(BMLBlockPeg.GLOBALPEG.getId(), BMLBlockPeg.GLOBALPEG);
    }

    /**
     * Removes all bmlBlockPegs and pegs from the board
     */
    public void clear()
    {
        pegs.clear();
        bmlBlockPegs.clear();
        bmlBlockPegs.put(BMLBlockPeg.GLOBALPEG.getId(), BMLBlockPeg.GLOBALPEG);
    }

    public void addBMLBlockPeg(BMLBlockPeg p)
    {
        bmlBlockPegs.put(p.getId(), p);
    }

    /**
     * Get the block peg associated with the bml block, null for non-existing
     */
    public BMLBlockPeg getBMLBlockPeg(String bmlId)
    {
        return bmlBlockPegs.get(bmlId);
    }

    /**
     * Get an immutable copy of the BMLBlockPeg map. That is: a map from bmlId-&gt;BMLBlockPeg
     */
    public ImmutableMap<String, BMLBlockPeg> getBMLBlockPegs()
    {
        return ImmutableMap.copyOf(bmlBlockPegs);
    }

    public ImmutableMap<PegKey, TimePeg> getTimePegs()
    {
        return pegs.getTimePegMap();
    }

    /**
     * Get the timing of the peg relative to bmlTargetId
     */
    public double getRelativePegTime(String bmlTargetId, TimePeg p)
    {
        if (p == null) return TimePeg.VALUE_UNKNOWN;
        BMLBlockPeg bmlP = bmlBlockPegs.get(bmlTargetId);
        if (bmlP == null)
        {
            log.warn("getRelativePegTime with invalid bmlTargetId {}", bmlTargetId);
            return TimePeg.VALUE_UNKNOWN;
        }
        return p.getGlobalValue() - bmlP.getValue();
    }

    /**
     * Get the timing of the peg identified by (syncId, id, bmlId), relative to bmlTargetId
     */
    public double getRelativePegTime(String bmlTargetId, String bmlId, String behId, String syncId)
    {
        TimePeg p = pegs.get(new PegKey(bmlId, behId, syncId));
        if (p == null) return TimePeg.VALUE_UNKNOWN;
        BMLBlockPeg bmlP = bmlBlockPegs.get(bmlTargetId);
        if (bmlP == null)
        {
            log.warn("getRelativePegTime with invalid bmlTargetId {}", bmlTargetId);
            return TimePeg.VALUE_UNKNOWN;
        }
        return p.getGlobalValue() - bmlP.getValue();
    }
    
    public double getRelativePegTime(String bmlId, String behId, String syncId)
    {
        return getRelativePegTime(bmlId,bmlId,behId,syncId);
    }

    /**
     * Get the global time of bmlId:id:syncId, returns 
     * TimePeg.VALUE_UNKNOWN if no TimePeg is connect to this sync point 
     * (or if no value is set on it yet).
     */
    public double getPegTime(String bmlId, String id, String syncId)
    {
        TimePeg p = pegs.get(new PegKey(bmlId, id, syncId));
        if (p == null) return TimePeg.VALUE_UNKNOWN;
        else return p.getGlobalValue();
    }

    /**
     * Sets the time for a TimePeg on the board
     */
    public void setPegTime(String bmlId, String id, String syncId, double time)
    {
        TimePeg p = pegs.get(new PegKey(bmlId, id, syncId));
        if (p != null) p.setGlobalValue(time);
    }

    /**
     * Sets the time for a BMLBlock on the board
     */
    public void setBMLBlockTime(String bmlId, double time)
    {
        BMLBlockPeg bp = bmlBlockPegs.get(bmlId);
        if (bp != null) bp.setValue(time);
    }
    
    /**
     * Gets the time for a BMLBlock on the board, returns TimePeg.VALUE_UNKNOWN if the block doesn't exist.
     */
    public double getBMLBlockTime(String bmlId)
    {
        BMLBlockPeg bp = bmlBlockPegs.get(bmlId);
        if (bp != null) 
        {
            return bp.getValue();
        }
        return TimePeg.VALUE_UNKNOWN;
    }

    public void addTimePegs(List<SyncAndTimePeg> satps)
    {
        for (SyncAndTimePeg satp : satps)
        {
            pegs.put(new PegKey(satp.bmlId, satp.id, satp.sync), satp.peg);
        }
    }

    public ImmutableSet<SyncAndTimePeg> getSyncAndTimePegs(final String bmlId, final String behId)
    {
        Set<SyncAndTimePeg> satps = new HashSet<SyncAndTimePeg>();
        Collection<Entry<PegKey, TimePeg>> entries = pegs.getEntries(bmlId, behId);
        for(Entry<PegKey, TimePeg> entry:entries)
        {
            satps.add(new SyncAndTimePeg(bmlId, behId, entry.getKey().syncId, entry.getValue()));
        }
        return ImmutableSet.copyOf(satps);
    }
    /**
     * Adds p to the pegboard.
     */
    public void addTimePeg(String bmlId, String id, String syncId, TimePeg p)
    {
        pegs.put(new PegKey(bmlId, id, syncId), p);
    }

    public ImmutableSet<String> getSyncs(String bmlId, String behaviorId)
    {
        return pegs.getSyncs(bmlId, behaviorId);
    }
    
    public ImmutableSet<TimePeg> getTimePegs(String bmlId, String behaviorId)
    {
        return pegs.get(bmlId, behaviorId);
    }

    
    public Set<String> getBehaviours(final String bmlId)
    {
        return pegs.getBehaviours(bmlId);
    }
    
    public TimePeg getTimePeg(String bmlId, String id, String syncId)
    {
        return pegs.get(new PegKey(bmlId, id, syncId));
    }

    /**
     * Get the PegKeys connected to TimePeg tp
     */
    public ImmutableSet<PegKey> getPegKeys(TimePeg tp)
    {
        return pegs.get(tp);
    }

    /**
     * Get the syncs of behavior behId that have a TimePeg value != TimePeg.ValueUnknown
     */
    public Set<String> getTimedSyncs(String bmlId, String behId)
    {
        Set<String> syncs = new HashSet<String>();
        for (PegKey p : pegs.getPegKeySet())
        {
            if (p.id.equals(behId) && p.bmlId.equals(bmlId))
            {
                if (getPegTime(p.bmlId, p.id, p.syncId) != TimePeg.VALUE_UNKNOWN)
                {
                    syncs.add(p.syncId);
                }
            }
        }
        return syncs;
    }

    
    /**
     * Remove all links to TimePegs corresponding with the behavior bmlId:id
     */
    public void removeBehaviour(String bmlId, String id)
    {
        pegs.removeBehaviour(bmlId, id);
    }

    private boolean getBehaviorCluster(BehaviorKey behavior, Set<BehaviorKey> cluster, boolean grounded)
    {
        cluster.add(behavior);
        Set<TimePeg> timePegs = getTimePegs(behavior.getBmlId(), behavior.getBehaviorId());
        Set<PegKey> pegKeySet = new HashSet<PegKey>();
        for (TimePeg tp : timePegs)
        {
            if(tp.isAbsoluteTime())
            {
                grounded = true;
            }
            pegKeySet.addAll(pegs.get(tp));
        }
        Set<BehaviorKey> linkedBehaviors = new HashSet<BehaviorKey>();
        for (PegKey pk : pegKeySet)
        {
            linkedBehaviors.add(new BehaviorKey(pk.bmlId, pk.id));
        }
        linkedBehaviors.removeAll(cluster);
        for (BehaviorKey bk : linkedBehaviors)
        {
            grounded = getBehaviorCluster(bk, cluster, grounded);
        }
        return grounded;
    }

    /**
     * Get the cluster of behaviors that is connected to the behavior with at constraints
     */
    public BehaviorCluster getBehaviorCluster(String bmlId, String behaviorId)
    {
        Set<BehaviorKey> cluster = new HashSet<BehaviorKey>();
        boolean grounded = getBehaviorCluster(new BehaviorKey(bmlId, behaviorId), cluster, false);
        return new BehaviorCluster(ImmutableSet.copyOf(cluster), grounded);
    }
    
    /**
     * Shift all timepegs in the cluster by shift 
     */
    public void shiftCluster(BehaviorCluster bc, double shift)
    {
        pegs.shiftCluster(bc, shift);
    }
}
