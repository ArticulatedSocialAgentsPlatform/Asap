/*******************************************************************************
 *******************************************************************************/
package asap.realizer.activate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedEventUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLScheduler;

/**
 * Implementation of the bmlt activate behavior.
 * Activates preplanned bml blocks 
 * @author welberge
 */
public class TimedActivateUnit extends TimedEventUnit
{
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
        feedback("start",time);              
    }
}
