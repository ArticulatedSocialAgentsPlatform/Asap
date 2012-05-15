package asap.realizer.scheduler;

import saiba.bml.core.BehaviourBlock;
import hmi.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import hmi.bml.ext.bmlt.BMLTSchedulingMechanism;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Schedules a BMLT block
 * @author hvanwelbergen
 * 
 */
public class BMLTSchedulingHandler implements SchedulingHandler
{
    private final SchedulingStrategy strategy;

    private final Logger logger = LoggerFactory.getLogger(BMLTSchedulingHandler.class.getName());

    public BMLTSchedulingHandler(SchedulingStrategy strategy)
    {
        this.strategy = strategy;
    }
    
    public BMLTSchedulingHandler(SchedulingStrategy strategy, PegBoard pb)
    {
        this(strategy);
    }

    @Override
    public void schedule(BehaviourBlock bb, BMLScheduler scheduler)
    {
        BMLTBMLBehaviorAttributes bmltAttr = bb.getBMLBehaviorAttributeExtension(BMLTBMLBehaviorAttributes.class);

        Set<String> appendAfter = new HashSet<String>();
        for (String bmlId : bmltAttr.getInterruptList())
        {
            logger.debug("interrupting {}", bmlId);
            scheduler.interruptBlock(bmlId);
        }
        switch (BMLTSchedulingMechanism.parse(bb.getSchedulingMechanism().getNameStart()))
        {
        case REPLACE:
            scheduler.reset();
            break;
        default:
        case MERGE:
            break;
        case APPEND:
            appendAfter.addAll(scheduler.getBMLBlocks());
            break;
        case APPEND_AFTER:
            appendAfter.addAll(bmltAttr.getAppendList());
            appendAfter.retainAll(scheduler.getBMLBlocks());
            break;
        }
        double predictedStart = scheduler.predictEndTime(appendAfter);
        scheduler.planningStart(bb.id, predictedStart);

        // logger.debug("Scheduling started at: {}",schedulingClock.getTime());
        BMLTBlock bbm = new BMLTBlock(bb.id, scheduler, appendAfter, bmltAttr.getOnStartList());
        BMLBlockPeg bmlBlockPeg = new BMLBlockPeg(bb.id, predictedStart);
        scheduler.addBMLBlockPeg(bmlBlockPeg);

        strategy.schedule(bb.getSchedulingMechanism(), bb, bmlBlockPeg, scheduler, scheduler.getSchedulingTime());
        logger.debug("Scheduling finished at: {}", scheduler.getSchedulingTime());
        scheduler.removeInvalidBehaviors(bb.id);

        predictedStart = scheduler.predictEndTime(appendAfter);
        scheduler.addBMLBlock(bbm);

        scheduler.planningFinished(bb.id, predictedStart, scheduler.predictEndTime(bb.id));
        if (bmltAttr.isPrePlanned())
        {
            logger.debug("Preplanning {}.", bb.id);
            bbm.setState(TimedPlanUnitState.PENDING);
        }
        else
        {
            switch (BMLTSchedulingMechanism.parse(bb.getSchedulingMechanism().getNameStart()))
            {
            default:
            case REPLACE:
            case MERGE:
                scheduler.startBlock(bb.id);
                break;
            case APPEND_AFTER:
            case APPEND:                
                bbm.setState(TimedPlanUnitState.LURKING);
                scheduler.updateBMLBlocks();
                break;
            }
        }
    }

}
