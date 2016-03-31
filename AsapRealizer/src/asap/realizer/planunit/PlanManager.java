/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.BehaviorNotFoundException;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.TimePegAlreadySetException;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * Manages a plan consisting of a list of TimedPlanUnits in a thread-safe manner
 * 
 * @author welberge
 * @param <T>
 */
@ThreadSafe
public final class PlanManager<T extends TimedPlanUnit>
{
    @GuardedBy("planUnits")
    private List<T> planUnits = new ArrayList<T>();

    private static final Logger logger = LoggerFactory.getLogger(PlanUnitTimeManager.class.getName());

    /**
     * Get an immutable copy of the list of planunits
     */
    public ImmutableList<T> getPlanUnits()
    {
        ImmutableList<T> l;
        synchronized (planUnits)
        {
            l = new ImmutableList.Builder<T>().addAll(planUnits).build();
        }
        return l;
    }

    /**
     * Get an immutable copy of the list of planunits, filtered by bmlId
     */
    public Collection<T> getPlanUnits(final String bmlId)
    {
        return Collections2.filter(getPlanUnits(), new Predicate<T>()
        {
            @Override
            public boolean apply(T arg)
            {
                return arg.getBMLId().equals(bmlId);
            }
        });
    }

    /**
     * Get a string containing bmlId:behaviorId of the planunits
     */
    public Set<String> getInvalidBehaviours()
    {
        Set<String> invalidBehaviours = new HashSet<String>();
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (!pu.hasValidTiming())
                {
                    invalidBehaviours.add(pu.getBMLId() + ":" + pu.getId());
                }
            }
        }
        return invalidBehaviours;
    }

    public void addPlanUnits(Collection<T> pus)
    {
        synchronized (planUnits)
        {
            planUnits.addAll(pus);
        }
    }

    public void addPlanUnit(T pu)
    {
        synchronized (planUnits)
        {
            planUnits.add(pu);
        }
    }

    /**
     * Stops and removes a selected collection of planunits.
     */
    public void removePlanUnits(Collection<T> puRemove, double time)
    {
        synchronized (planUnits)
        {
            planUnits.removeAll(puRemove);
        }
        for (T pu : puRemove)
        {
            logger.debug("Removing planunit {}:{}", pu.getBMLId(), pu.getId());
            if (pu.isPlaying())
            {
                try
                {
                    pu.stop(time);
                }
                catch (TimedPlanUnitPlayException e)
                {
                    logger.warn("Exception stopping behaviour: ", e);
                }
            }
        }
    }

    private void interruptPlanUnit(T pu, double time)
    {
        try
        {
            logger.debug("Interrupting " + pu.getBMLId() + ":" + pu.getId() + " " + time);
            pu.interrupt(time);
        }
        catch (TimedPlanUnitPlayException e)
        {
            logger.warn("Exception gracefully interrupting behavior: ", e);
            try
            {
                pu.stop(time);
            }
            catch (TimedPlanUnitPlayException e1)
            {
                logger.warn("Exception stopping behaviour: ", e);
            }
        }
    }

    /**
     * Gracefully interrupts a selected collection of planunits.
     */
    public void interruptPlanUnits(Collection<T> puInterrupt, double time)
    {
        for (T pu : puInterrupt)
        {
            interruptPlanUnit(pu, time);
        }
        removeFinishedPlanUnits();
    }

    /**
     * Get an immutable copy of the list of planunits, filtered by bmlId and behaviour id
     */
    public Collection<T> getPlanUnits(final String bmlId, final String id)
    {
        return Collections2.filter(getPlanUnits(), new Predicate<T>()
        {
            @Override
            public boolean apply(T arg)
            {
                return arg.getBMLId().equals(bmlId) && arg.getId().equals(id);
            }
        });
    }

    private T getMainPlanUnit(String bmlId, String id)
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getId().equals(id) && pu.getBMLId().equals(bmlId) && !pu.isSubUnit())
                {
                    return pu;
                }
            }
        }
        return null;
    }

    private void interruptPlanUnit(String bmlId, String id, double globalTime, List<T> handledUnits)
    {
        synchronized (planUnits)
        {
            for (T pu : getPlanUnits(bmlId, id))
            {
                interruptPlanUnit(pu, globalTime);
            }
        }
        removeFinishedPlanUnits();
    }

    public void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        interruptPlanUnit(bmlId, id, globalTime, new ArrayList<T>());
    }

    public void stopPlanUnit(String bmlId, String id, double globalTime)
    {
        List<T> planUnitsToInterrupt = new ArrayList<T>();
        synchronized (planUnits)
        {
            for (T pu : getPlanUnits(bmlId, id))
            {
                planUnitsToInterrupt.add(pu);
                planUnits.remove(pu);
            }
        }

        for (T pu : planUnitsToInterrupt)
        {
            if (pu.isPlaying())
            {
                try
                {
                    pu.stop(globalTime);
                }
                catch (TimedPlanUnitPlayException e)
                {
                    logger.warn("Exception stopping behaviour: ", e);
                }
            }
        }
    }

    public int getNumberOfPlanUnits()
    {
        synchronized (planUnits)
        {
            return planUnits.size();
        }
    }

    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    logger.debug("Setting BML {} block state of {}:{} to {}.", new String[] { pu.isSubUnit() ? "sub" : "", pu.getBMLId(),
                            pu.getId(), state.toString() });
                    pu.setState(state);
                }
            }
        }
    }

    /**
     * Removes all planunits, if appropiate sends feedback at time
     * 
     * @param time
     */
    public void removeAllPlanUnits(double time)
    {
        Collection<T> copyOfPlanUnits;
        synchronized (planUnits)
        {
            copyOfPlanUnits = ImmutableList.copyOf(planUnits);
        }
        removePlanUnits(copyOfPlanUnits, time);
    }

    public void removeFinishedPlanUnits()
    {
        synchronized (planUnits)
        {
            List<T> finishedUnits = new ArrayList<T>();
            for (T pu : planUnits)
            {
                if (pu.isDone())
                {
                    finishedUnits.add(pu);
                }
            }
            planUnits.removeAll(finishedUnits);
        }

    }

    /**
     * Return true if a non-sub planunit corresponding to bmlId:behId is in the plan
     */
    public boolean containsMainBehaviour(String bmlId, String behId)
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId) && !pu.isSubUnit())
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Return true if a PlanUnit corresponding to bmlId:behId is in the plan
     */
    public boolean containsBehaviour(String bmlId, String behId)
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getBehaviours(String bmlId)
    {
        HashSet<String> behaviours = new HashSet<String>();
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    behaviours.add(pu.getId());
                }
            }
        }
        return behaviours;
    }

    private double getLastSetTime(T pu)
    {
        double endTime = 0;
        if (pu.getEndTime() != TimePeg.VALUE_UNKNOWN)
        {
            return pu.getEndTime();
        }
        else
        {
            for (String syncId : pu.getAvailableSyncs())
            {
                if (pu.getTime(syncId) != TimePeg.VALUE_UNKNOWN)
                {
                    endTime = pu.getTime(syncId);
                }
            }
        }
        return endTime;
    }

    public double getSubsidingTime(String bmlId)
    {
        double subsidingTime = 0;
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    if (pu.getRelaxTime() != TimePeg.VALUE_UNKNOWN && pu.getRelaxTime() > subsidingTime)
                    {
                        subsidingTime = pu.getRelaxTime();
                    }
                }
            }
        }
        return subsidingTime;
    }

    public double getEndTime(String bmlId)
    {
        double endTime = 0;
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    double lastTime = getLastSetTime(pu);
                    if (lastTime > endTime)
                    {
                        endTime = lastTime;
                    }
                }
            }
        }
        return endTime;
    }

    public double getEndTime(String bmlId, String behId)
    {
        synchronized (planUnits)
        {
            T pu = getMainPlanUnit(bmlId, behId);
            if (pu == null) return TimePeg.VALUE_UNKNOWN;
            return pu.getEndTime();
        }
    }

    public void stopBehaviourBlock(String bmlId, double time)
    {

        List<T> removeUnits = new ArrayList<T>();
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    removeUnits.add(pu);
                }
            }
        }
        removePlanUnits(removeUnits, time);
    }

    public void interruptBehaviourBlock(String bmlId, double time)
    {

        List<T> interruptUnits = new ArrayList<T>();
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId))
                {
                    interruptUnits.add(pu);
                }
            }
        }
        interruptPlanUnits(interruptUnits, time);
        removeFinishedPlanUnits();
    }

    public OffsetPeg createOffsetPeg(String bmlId, String behId, String syncId) throws BehaviorNotFoundException,
            SyncPointNotFoundException, TimePegAlreadySetException
    {
        synchronized (planUnits)
        {
            T pu = getMainPlanUnit(bmlId, behId);
            if (pu == null)
            {
                throw new BehaviorNotFoundException(bmlId, behId);
            }
            if (pu.getTime(syncId) != TimePeg.VALUE_UNKNOWN)
            {
                throw new TimePegAlreadySetException(bmlId, behId, syncId);
            }
            double relTime = pu.getRelativeTime(syncId);

            String tpBefore = null;
            String tpAfter = null;
            boolean before = true;
            for (String sync : pu.getAvailableSyncs())
            {
                if (sync.equals(syncId))
                {
                    before = false;
                    continue;
                }
                if (pu.getTime(sync) != TimePeg.VALUE_UNKNOWN)
                {
                    if (before)
                    {
                        tpBefore = sync;
                    }
                    else
                    {
                        tpAfter = sync;
                        break;
                    }
                }
            }
            if (tpBefore == null && tpAfter == null)
            {
                throw new AssertionError("TimedPlanUnit with no set syncpoints!");
            }
            TimePeg link;
            double globalDuration = 0;
            double relativeDuration = 0;

            double offset = 0;
            if (tpBefore != null && tpAfter != null)
            {
                globalDuration = pu.getTime(tpAfter) - pu.getTime(tpBefore);
                relativeDuration = pu.getRelativeTime(tpAfter) - pu.getRelativeTime(tpBefore);
            }

            if (tpBefore == null)
            {
                link = pu.getTimePeg(tpAfter);
            }
            else if (tpAfter == null)
            {
                link = pu.getTimePeg(tpBefore);
            }
            else if (relTime - pu.getRelativeTime(tpBefore) < pu.getRelativeTime(tpAfter) - relTime)
            {
                link = pu.getTimePeg(tpBefore);
                offset = globalDuration * ((relTime - pu.getRelativeTime(tpBefore)) / relativeDuration);
            }
            else
            {
                link = pu.getTimePeg(tpAfter);
                offset = globalDuration * ((relTime - pu.getRelativeTime(tpAfter)) / relativeDuration);
            }
            return new OffsetPeg(link, offset, pu.getBMLBlockPeg());
        }
    }

    public void setFloatParameterValue(String bmlId, String behId, String paramId, float value) throws ParameterException,
            BehaviorNotFoundException
    {
        boolean found = false;
        logger.debug("setFloatParameterValue parameter:{} value:{}", paramId, value);
        synchronized (planUnits)
        {
            logger.debug("setFloatParameterValue in sync");
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId))
                {
                    try
                    {
                        pu.setFloatParameterValue(paramId, value);
                    }
                    catch (ParameterException ex)
                    {
                        if (!pu.isSubUnit()) throw ex;
                    }
                    found = true;
                }
            }
            logger.debug("setFloatParameterValue done");
        }
        if (!found) throw new BehaviorNotFoundException(bmlId, behId);
    }

    public void setParameterValue(String bmlId, String behId, String paramId, String value) throws ParameterException,
            BehaviorNotFoundException
    {
        boolean found = false;
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId))
                {
                    try
                    {
                        pu.setParameterValue(paramId, value);
                    }
                    catch (ParameterException ex)
                    {
                        if (!pu.isSubUnit()) throw ex;
                    }
                    found = true;
                }
            }
        }
        if (!found) throw new BehaviorNotFoundException(bmlId, behId);
    }

    public float getFloatParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId) && !pu.isSubUnit())
                {
                    return pu.getFloatParameterValue(paramId);
                }
            }
        }
        throw new BehaviorNotFoundException(bmlId, behId);
    }

    public String getParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException
    {
        synchronized (planUnits)
        {
            for (T pu : planUnits)
            {
                if (pu.getBMLId().equals(bmlId) && pu.getId().equals(behId) && !pu.isSubUnit())
                {
                    return pu.getParameterValue(paramId);
                }
            }
        }
        throw new BehaviorNotFoundException(bmlId, behId);
    }
}
