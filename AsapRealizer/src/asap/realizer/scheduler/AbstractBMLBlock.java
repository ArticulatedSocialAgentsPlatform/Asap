package asap.realizer.scheduler;

import saiba.bml.BMLGestureSync;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Skeleton implementation of the BML block. New BMLBlock implementations need to implement the
 * BMLBlock state machine that is triggered through e.g. the update function
 * @author hvanwelbergen
 * 
 */
public abstract class AbstractBMLBlock implements BMLBlock
{
    protected final String bmlId;

    protected final BMLScheduler scheduler;

    protected AtomicReference<TimedPlanUnitState> state = new AtomicReference<TimedPlanUnitState>();

    private static final Logger logger = LoggerFactory.getLogger(AbstractBMLBlock.class.getName());

    protected final ConcurrentHashMap<String, Set<String>> behaviorSyncsPassed = new ConcurrentHashMap<String, Set<String>>();

    protected final Set<String> droppedBehaviours = new CopyOnWriteArraySet<String>();

    public AbstractBMLBlock(String id, BMLScheduler s)
    {
        bmlId = id;
        scheduler = s;
        state.set(TimedPlanUnitState.IN_PREP);
    }

    public void start()
    {
        state.set(TimedPlanUnitState.IN_EXEC);
        scheduler.blockStartFeedback(bmlId);
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
    
    public void activate()
    {
        state.set(TimedPlanUnitState.LURKING);
    }

    public void finish()
    {
        state.set(TimedPlanUnitState.DONE);
        scheduler.blockStopFeedback(bmlId);
    }

    @Override
    public boolean isPending()
    {
        return getState().equals(TimedPlanUnitState.PENDING);
    }
    
    protected boolean isFinished()
    {
        for (String behId : scheduler.getBehaviours(bmlId))
        {
            if (droppedBehaviours.contains(behId)) continue;
            logger.debug("checking isFinished {}:{}", bmlId, behId);

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

    protected boolean isSubsiding()
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
        logger.debug("{} is subsiding at {}", bmlId, scheduler.getSchedulingTime());
        return true;
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

    public void dropBehaviour(String beh)
    {
        droppedBehaviours.add(beh);
    }

    /**
     * @return the bmlId
     */
    public String getBMLId()
    {
        return bmlId;
    }

    public void behaviorProgress(String behaviorId, String syncId)
    {
        Set<String> newSet = new HashSet<String>();
        Set<String> behInfo = behaviorSyncsPassed.putIfAbsent(behaviorId, newSet);
        if (behInfo == null) behInfo = newSet;
        behInfo.add(syncId);
    }
}
