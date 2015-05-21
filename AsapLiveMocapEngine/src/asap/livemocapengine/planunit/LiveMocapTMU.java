/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.planunit;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableList;

/**
 * A LivemocapTMU is a superclass for all planunits that read from some input, process it 
 * and write it to some output. Classes implementing the LivemocapTMU should implement playUnit
 * to do so. LivemocapTMU takes care of running tmu at the right time, sending feedback, etc.
 * @author welberge
 */
@Slf4j
public abstract class LiveMocapTMU extends TimedAbstractPlanUnit
{
    protected TimePeg startPeg;
    protected TimePeg endPeg;
    
    public LiveMocapTMU(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);  
        endPeg = new TimePeg(bmlPeg);
        startPeg = new TimePeg(bmlPeg);
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
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

    /**
     * @param startPeg the startPeg to set
     */
    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
    }

    /**
     * @param endPeg the endPeg to set
     */
    public void setEndPeg(TimePeg endPeg)
    {
        this.endPeg = endPeg;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start")) return startPeg;
        else if (syncId.equals("end")) return endPeg;
        return null;
    }

    @Override
    public boolean hasValidTiming()
    {
        if (endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            return endPeg.getGlobalValue() >= startPeg.getGlobalValue();
        }
        return true;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (syncId.equals("start"))
        {
            startPeg = peg;
        }
        else if (syncId.equals("end"))
        {
            endPeg = peg;
        }
        else
        {
            log.warn("Can't set TimePeg for sync {}", syncId);
        }
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("start", time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("end", time);
    } 
    
    @Override
    public List<String> getAvailableSyncs()
    {
        return ImmutableList.of("start","end");
    }
}
