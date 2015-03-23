/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;


import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;

import com.google.common.collect.ImmutableCollection;

/**
 * Skeleton implemenation of the TimedPlanUnitPlayer
 * @author hvanwelbergen
 *
 */
@Slf4j
public abstract class AbstractTimedPlanUnitPlayer implements TimedPlanUnitPlayer
{
    protected List<TimedPlanUnitPlayException> playExceptions;
    protected List<TimedPlanUnitPlayException> stopExceptions;
    
    @Override
    public void handleStopExceptions(double t)
    {
        ImmutableCollection<TimedPlanUnitPlayException> exceptions = getStopExceptions();
        for (TimedPlanUnitPlayException tmuEx : exceptions)
        {
            log.warn("Exception stopping behaviour: ", tmuEx);            
        }
        clearStopExceptions(exceptions);
    }
    
    @Override
    public void handlePlayExceptions(double t, FeedbackManager fbManager)
    {
        ImmutableCollection<TimedPlanUnitPlayException> exceptions = getPlayExceptions();
        for (TimedPlanUnitPlayException tmuEx : exceptions)
        {
            TimedPlanUnit tmuR = tmuEx.getPlanUnit();
            fbManager.puException(tmuR,
                    "Runtime exception for behavior " + tmuR.getBMLId() + ":" + tmuR.getId() + ":" + tmuEx.getLocalizedMessage() + ".", t);
            stopUnit(tmuR, t);            
        }
        clearPlayExceptions(exceptions);
    }
    
    @Override
    public void clearStopExceptions(Collection<TimedPlanUnitPlayException> removeExceptions)
    {
        stopExceptions.removeAll(removeExceptions);        
    }
    
    @Override
    public void clearPlayExceptions(Collection<TimedPlanUnitPlayException> removeExceptions)
    {
        playExceptions.removeAll(removeExceptions);
    }
}
