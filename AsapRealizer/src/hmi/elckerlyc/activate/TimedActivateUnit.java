package hmi.elckerlyc.activate;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedAbstractPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.scheduler.BMLScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the bmlt activate behavior.
 * Activates preplanned bml blocks 
 * @author welberge
 */
public class TimedActivateUnit extends TimedAbstractPlanUnit
{
    private TimePeg startPeg;
    private final BMLScheduler scheduler;
    private final String target;
    private static final Logger logger = LoggerFactory.getLogger(TimedActivateUnit.class.getName());
    
    public TimedActivateUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String id, String target, BMLScheduler s)
    {
        super(bfm,bmlPeg, bmlId, id);    
        scheduler = s;
        this.target = target;
        logger.debug("Created activate unit {} {} {}",new String[]{getBMLId(),getId(),target});
    }
    
    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        logger.debug("starting activate unit {} {}",getBMLId(),getId());
        scheduler.activateBlock(target,time);
        sendFeedback("start",time);              
    }
    
    /**
     * @param startPeg the startPeg to set
     */
    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
        logger.debug("Setting start peg to {}",startPeg);
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        logger.debug("stopping activate unit {} {}",getBMLId(),getId());
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
