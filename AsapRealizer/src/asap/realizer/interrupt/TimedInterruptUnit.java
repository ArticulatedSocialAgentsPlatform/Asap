/*******************************************************************************
 *******************************************************************************/
package asap.realizer.interrupt;


import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedEventUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLScheduler;

import com.google.common.collect.ImmutableSet;

/**
 * Implementation of the bmla interrupt behavior.
 * Can gracefully interrupt the execution of ongoing or planned behaviors. 
 * @author welberge
 */
@Slf4j
public class TimedInterruptUnit extends TimedEventUnit
{
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

    public TimedInterruptUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String id, String iTarget, BMLScheduler s)
    {
        super(bfm,bmlPeg, bmlId, id);    
        scheduler = s;
        target = iTarget;
        log.debug("Created interrupt unit {} {} {}",new String[]{getBMLId(),getId(),target});
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
    	log.debug("Starting interrupt unit {} {}",getBMLId(),getId());
    	
    	Set<String> stopBehs = new HashSet<String>();
        stopBehs.addAll(scheduler.getBehaviours(target));
        Set<String> containingBehs = new HashSet<String>(stopBehs);
        if(include.size()>0)
        {
            stopBehs.retainAll(include);
        }
        stopBehs.removeAll(exclude);
        
        feedback("start",time);
        if(stopBehs.equals(containingBehs))
        {
            scheduler.interruptBlock(target, time);
        }
        else
        {
            for(String beh:stopBehs)
            {
            	log.debug("Interrupting behavior {}:{}",target,beh);
                scheduler.interruptBehavior(target,beh, time);            
            }
        }                
    }    
}
