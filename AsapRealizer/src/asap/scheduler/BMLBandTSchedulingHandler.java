package asap.scheduler;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.bmlb.*;


import hmi.bml.core.BehaviourBlock;
import hmi.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.scheduler.SchedulingHandler;
import hmi.elckerlyc.scheduler.SchedulingStrategy;

/**
 * Creates BMLT blocks and handles the first part of the BMLT state machine 
 * @author hvanwelbergen
 *
 */
public class BMLBandTSchedulingHandler implements SchedulingHandler
{
private final SchedulingStrategy strategy;
    
    private final Logger logger = LoggerFactory.getLogger(BMLBandTSchedulingHandler.class.getName());
    
    public BMLBandTSchedulingHandler(SchedulingStrategy strategy)
    {
        this.strategy = strategy;
    }
    
    @Override
    public void schedule(BehaviourBlock bb, BMLScheduler scheduler)
    {
        BMLBBMLBehaviorAttributes bmlbAttr = bb.getBMLBehaviorAttributeExtension(BMLBBMLBehaviorAttributes.class);
        BMLTBMLBehaviorAttributes bmltAttr = bb.getBMLBehaviorAttributeExtension(BMLTBMLBehaviorAttributes.class);
        
        Set<String> appendAfter = new HashSet<String>();
        Set<String> chunkAfter = new HashSet<String>();
        for (String bmlId : bmltAttr.getInterruptList())
        {
            logger.debug("interrupting {}", bmlId);
            scheduler.interruptBlock(bmlId);
        }
        switch (BMLBComposition.parse(bb.getSchedulingMechanism().getNameStart()))
        {
        case REPLACE:
            scheduler.reset();
            break;
        case MERGE:
            break;
        case APPEND:
            appendAfter.addAll(scheduler.getBMLBlocks());
            break;
        case CHUNK_AFTER:
            chunkAfter.addAll(bmlbAttr.getChunkAfterList());
            break;
        case APPEND_AFTER:
            appendAfter.addAll(bmltAttr.getAppendList());
            appendAfter.retainAll(scheduler.getBMLBlocks());
            break;
        }
        
        double predictedStart = Math.max(scheduler.predictEndTime(appendAfter),scheduler.predictSubsidingTime(chunkAfter));        
        scheduler.planningStart(bb.id, predictedStart);

        // logger.debug("Scheduling started at: {}",schedulingClock.getTime());
        BMLBBlock bbm = new BMLBBlock(bb.id, scheduler, appendAfter, bmltAttr.getOnStartList(), chunkAfter);
        BMLBlockPeg bmlBlockPeg = new BMLBlockPeg(bb.id, predictedStart);        
        scheduler.addBMLBlockPeg(bmlBlockPeg);

        strategy.schedule(bb.getSchedulingMechanism(), bb, bmlBlockPeg, scheduler, scheduler.getSchedulingTime());
        logger.debug("Scheduling finished at: {}", scheduler.getSchedulingTime());
        scheduler.removeInvalidBehaviors(bb.id);

        predictedStart = Math.max(scheduler.predictEndTime(appendAfter),scheduler.predictSubsidingTime(chunkAfter));
        scheduler.addBMLBlock(bbm);
        

        scheduler.planningFinished(bb.id, predictedStart, scheduler.predictEndTime(bb.id));
        if (bmltAttr.isPrePlanned())
        {
            logger.debug("Preplanning {}.", bb.id);
            bbm.setState(TimedPlanUnitState.PENDING);
        }
        else
        {
            switch (BMLBComposition.parse(bb.getSchedulingMechanism().getNameStart()))
            {
            case REPLACE:
            case MERGE:
                scheduler.startBlock(bb.id);
                break;
            case APPEND_AFTER:
            case APPEND:
            case CHUNK_AFTER:
                bbm.setState(TimedPlanUnitState.LURKING);
                scheduler.updateBMLBlocks();                
                break;
            }
        }        
    }

}
