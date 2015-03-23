/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;

import com.google.common.collect.ImmutableList;

/**
 * TimePlanUnit that only has a start and is then finished instantly 
 * @author herwinvw
 */
@Slf4j
public abstract class TimedEventUnit extends TimedAbstractPlanUnit
{
    public TimedEventUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);        
    }

    private TimePeg startPeg;
    
    /**
     * @param startPeg the startPeg to set
     */
    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
        log.debug("Setting start peg to {}",startPeg);
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }
    
    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        log.debug("stopping interrupt unit {} {}",getBMLId(),getId());        
        feedback("end",time);        
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
            log.warn("Can't set TimePeg for sync {}, only setting start is allowed",syncId);            
        }
    }    
    
    @Override
    public List<String> getAvailableSyncs()
    {
        return ImmutableList.of("start","end");
    }
}
