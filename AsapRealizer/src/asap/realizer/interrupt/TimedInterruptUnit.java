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
            logger.debug("Interrupting behavior {}:{}",target,beh);
            scheduler.interruptBehavior(target,beh);
        }
        sendFeedback("start",time);        
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }
    
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
