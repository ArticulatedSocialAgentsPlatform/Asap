package asap.realizer.interrupt;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLScheduler;

import com.google.common.collect.ImmutableSet;

/**
 * Implementation of the bmlt interrupt behavior.
 * Can gracefully interrupt the execution of ongoing or planned behaviors. 
 * @author welberge
 */
public class TimedInterruptUnit extends TimedAbstractPlanUnit
{
    private static final Logger logger = LoggerFactory.getLogger(TimedInterruptUnit.class.getName());
    
    private TimePeg startPeg;
    private final BMLScheduler scheduler;
    private final String target;
    private ImmutableSet<String>include = new ImmutableSet.Builder<String>().build();
    private ImmutableSet<String>exclude = new ImmutableSet.Builder<String>().build();
    
    public void setInclude(ImmutableSet<String>include)
    {
        this.include = include;
    }
    
    public void setExclude(ImmutableSet<String>exclude)
    {
        this.exclude = exclude;
    }
    
    /**
     * @param startPeg the startPeg to set
     */
    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
        logger.debug("Setting start peg to {}",startPeg);
    }

    public TimedInterruptUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String id, String iTarget, BMLScheduler s)
    {
        super(bfm,bmlPeg, bmlId, id);    
        scheduler = s;
        target = iTarget;
        logger.debug("Created interrupt unit {} {} {}",new String[]{getBMLId(),getId(),target});
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        logger.debug("Starting interrupt unit {} {}",getBMLId(),getId());
        Set<String> stopBehs = new HashSet<String>();
        stopBehs.addAll(scheduler.getBehaviours(target));
        if(include.size()>0)
        {
            stopBehs.retainAll(include);
        }
        stopBehs.removeAll(exclude);
        
        for(String beh:stopBehs)
        {
            logger.debug("Immidiatly interrupting behavior {}:{}",target,beh);
            scheduler.interruptBehavior(target,beh);
        }
        //scheduler.interruptBlock(target);    
        
        sendFeedback("start",time);        
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }
    
    /*
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        List<InterruptTarget>interruptTargets = new ArrayList<InterruptTarget>();
        synchronized(interruptSpecMap)
        {
            for(String beh:interruptSpecMap.keySet())
            {
                //not planned, no need to interrupt it
                if(!scheduler.getBehaviours(target).contains(beh))
                {
                    logger.debug("Behavior {}:{} not planned, prematurely removed from interrupt unit",target,beh);
                    interruptSpecMap.remove(beh);
                    break;
                }            
                
                
                ImmutableSet<String> syncsFinished = scheduler.getSyncsPassed(target,beh);
                //logger.debug("Syncs finished {}",syncsFinished);
                InterruptSpec is = interruptSpecMap.get(beh);
                if (is==null)continue;
                if(syncsFinished.contains(is.getSyncPoint()))
                {
                    logger.debug("Adding interrupt for {}:{}",target,beh);
                    interruptTargets.add(new InterruptTarget(target,beh));                    
                }   
            }            
        }
        
        for(InterruptTarget iTarget: interruptTargets)
        {
            logger.debug("Interrupting {}:{}",iTarget.getBmlId(),iTarget.getBehaviorId());
            scheduler.interruptBehavior(iTarget.getBmlId(), iTarget.getBehaviorId());
            logger.debug("Interrupt finished");
            InterruptSpec is = interruptSpecMap.get(iTarget.getBehaviorId());
            if (is==null)continue;
            for(String onStartBlock : is.getOnStartList())
            {
                logger.debug("Starting {}",onStartBlock);
                scheduler.startBlock(onStartBlock);
            }
            interruptSpecMap.remove(iTarget.getBehaviorId());
            logger.debug("interruptSpecMap {}",interruptSpecMap);
        }
    }
    */
    
    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        logger.debug("stopping interrupt unit {} {}",getBMLId(),getId());        
        sendFeedback("end",time);        
    }

    @Override
    public double getEndTime()
    {
        return TimePeg.VALUE_UNKNOWN;
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }
    
    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public boolean hasValidTiming()
    {
        return true;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if(syncId.equals("start"))return startPeg;
        return null;
    }    
    
    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if(syncId.equals("start"))
        {
            startPeg = peg;            
        }
        else
        {
            logger.warn("Can't set TimePeg for sync {}, only setting start is allowed",syncId);            
        }
    }    
}
