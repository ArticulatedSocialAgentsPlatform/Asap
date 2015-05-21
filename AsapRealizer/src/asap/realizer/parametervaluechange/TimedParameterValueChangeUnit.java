/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLGestureSync;
import asap.realizer.BehaviorNotFoundException;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLScheduler;

import com.google.common.collect.ImmutableList;

/**
 * TimedPlanUnit for the parametervaluechange bmlt behavior. Changes parameter values in other ongoing behavior.
 * @author welberge
 */
public class TimedParameterValueChangeUnit extends TimedAbstractPlanUnit
{
    private TimePeg startPeg;
    private TimePeg endPeg;
    private final ParameterValueTrajectory trajectory;
    private final BMLScheduler scheduler;
    private final String paramId;
    private final String targetId;
    private final String targetBmlId;
    private float initialValue;
    private final float targetValue;
    private final boolean hasInitialValue;
    
    
    private static final Logger logger = LoggerFactory.getLogger(TimedParameterValueChangeUnit.class.getName());

    public TimedParameterValueChangeUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String behId, BMLScheduler sched,
            ParameterValueInfo paramValInfo, ParameterValueTrajectory traj)
    {
        super(bfm, bmlPeg, bmlId, behId);
        trajectory = traj;
        scheduler = sched;
        paramId = paramValInfo.getParamId();
        targetId = paramValInfo.getTargetId();
        targetBmlId = paramValInfo.getTargetBmlId();
        initialValue = paramValInfo.getInitialValue();
        targetValue = paramValInfo.getTargetValue();
        hasInitialValue = paramValInfo.hasInitialValue();
        endPeg = new TimePeg(bmlPeg);
        startPeg = new TimePeg(bmlPeg);        
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
        logger.debug("Setting start peg to {}", startPeg);
    }

    /**
     * @param endPeg the endPeg to set
     */
    public void setEndPeg(TimePeg endPeg)
    {
        this.endPeg = endPeg;
        logger.debug("Setting end peg to {}", endPeg);
    }

    @Override
    public double getEndTime()
    {
        if(!isPlaying()||endPeg==null)
        {
            return TimePeg.VALUE_UNKNOWN;       //ensure 1 time playback
        }
        return endPeg.getGlobalValue();        
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }
    
    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start"))
            return startPeg;
        else if (syncId.equals("end"))
            return endPeg;
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (BMLGestureSync.isBMLSync(syncId))
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
                logger.warn("Can't set TimePeg for {} on parameter value change behavior", syncId);
            }
        }
        else
        {
            logger.warn("Can't set TimePeg for non-BML sync {}", syncId);
        }
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
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if (syncId.equals("start") || syncId.equals("ready") || syncId.equals("strokeStart"))
            return 0;
        if (syncId.equals("strokeEnd") || syncId.equals("stroke") || syncId.equals("relax") || syncId.equals("end"))
            return 1;
        throw new SyncPointNotFoundException(getBMLId(), getId(), syncId);
    }

    @Override
    public double getTime(String syncId)
    {
        if (syncId.equals("start") || syncId.equals("ready") || syncId.equals("strokeStart"))
            return getStartTime();
        if (syncId.equals("strokeEnd") || syncId.equals("stroke") || syncId.equals("relax") || syncId.equals("end"))
            return endPeg.getGlobalValue();
        return TimePeg.VALUE_UNKNOWN;
    }

    private double setValue(double time)throws TimedPlanUnitPlayException
    {
        double t = 1; // instant change if end not set
        if (endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN && time < endPeg.getGlobalValue())
        {
            t = (time - getStartTime()) / (endPeg.getGlobalValue() - startPeg.getGlobalValue());
        }
        
        try
        {
            logger.debug("Setting parameter value {} in {}:{} to {})",
                    new Object[] { paramId, targetBmlId, targetId, trajectory.getValue(initialValue, targetValue, (float) t) });
            scheduler.setParameterValue(targetBmlId, targetId, paramId, trajectory.getValue(initialValue, targetValue, (float) t));
        }
        catch (ParameterException e)
        {
            throw new TimedPlanUnitPlayException("Parameter " + paramId + " not valid for " + targetBmlId + ":" + targetId, this, e);
        }
        catch (BehaviorNotFoundException e)
        {
            //FIXME: the targeted unit could end inbetween 
            //fbManager.getSyncsPassed(targetBmlId, targetId).contains("end")) and here
            //causing the behavior to fail here
            throw new TimedPlanUnitPlayException("Behavior " + targetBmlId + ":" + targetId + " not found at t="+time+".", this, e);            
        }
        return t;
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (fbManager.getSyncsPassed(targetBmlId, targetId).contains("end"))
        {
            logger.debug("End passed for {}:{}, stopping parametervaluechange behavior",targetBmlId,targetId);
            stop(time);
            return;
        }
               
        double t = setValue(time);
        
        if (t >= 1)
        {
            stop(time);
        }        
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("end", time);
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("start", time);
        if (!hasInitialValue)
        {
            try
            {
                initialValue = scheduler.getFloatParameterValue(targetBmlId, targetId, paramId);
            }
            catch (ParameterException e)
            {
                throw new TimedPlanUnitPlayException("ParameterException", this,e);                
            }
            catch (BehaviorNotFoundException e)
            {
                throw new TimedPlanUnitPlayException("BehaviorNotFoundException", this,e);                
            }
        }
        setValue(time);
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        return ImmutableList.of("start","end");
    }

}
