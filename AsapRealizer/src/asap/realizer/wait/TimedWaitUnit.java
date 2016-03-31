/*******************************************************************************
 *******************************************************************************/
package asap.realizer.wait;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLGestureSync;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableList;

/**
 * TimedPlanUnit realizations of the BML &lt;wait&gt; behavior.
 * Simply runs as a no-op behavior and sends the appropriate feedback.
 * @author Herwin
 * 
 */
public class TimedWaitUnit extends TimedAbstractPlanUnit
{
    private TimePeg startPeg;
    private TimePeg endPeg;
    private static final Logger logger = LoggerFactory.getLogger(TimedWaitUnit.class.getName());

    protected TimedWaitUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String id)
    {
        super(bfm, bmlPeg, bmlId, id);
    }

    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
    }

    public void setEndPeg(TimePeg endPeg)
    {
        this.endPeg = endPeg;
    }

    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        if (endPeg == null)
        {
            return TimePeg.VALUE_UNKNOWN;
        }
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }
    
    @Override
    public boolean hasValidTiming()
    {
        if (startPeg == null)
        {
            return false;
        }
        if (startPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            return false;
        }
        if (endPeg == null)
        {
            return true;
        }
        if (endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            return true;
        }
        return (startPeg.getGlobalValue() < endPeg.getGlobalValue());
    }

    @Override
    public void startUnit(double time)
    {
        feedback("start", time);        
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("end", time);
    }

    @Override
    public double getPreferedDuration()
    {
        if (getEndTime() != TimePeg.VALUE_UNKNOWN && getStartTime() != TimePeg.VALUE_UNKNOWN)
        {
            return getEndTime() - getStartTime();
        }
        return 0;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start")) return startPeg;
        if (syncId.equals("end")) return endPeg;
        return null;
    }
    
    @Override
    public List<String> getAvailableSyncs()
    {
        return ImmutableList.of("start","end");
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (BMLGestureSync.isBMLSync(syncId))
        {
            if (BMLGestureSync.get(syncId).isAfter(BMLGestureSync.STROKE))
            {
                setEndPeg(peg);
            }
            else
            {
                setStartPeg(peg);
            }
        }
        else
        {
            logger.warn("Can't set TimePeg on non-BML sync {}", syncId);
        }
    }

}
