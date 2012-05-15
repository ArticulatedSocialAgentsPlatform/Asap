package asap.realizer.scheduler;

import saiba.bml.core.BehaviourBlock;
import hmi.bml.ext.bmlt.BMLTBMLBehaviorAttributes;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.bmlb.BMLBBMLBehaviorAttributes;
import asap.bmlb.BMLBComposition;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Creates BMLT blocks and handles the first part of the BMLT state machine
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class BMLBandTSchedulingHandler implements SchedulingHandler
{
    private final SchedulingStrategy strategy;
    private final PegBoard pegBoard;
    
    public BMLBandTSchedulingHandler(SchedulingStrategy strategy, PegBoard pb)
    {
        this.strategy = strategy;
        this.pegBoard = pb;
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
            log.debug("interrupting {}", bmlId);
            scheduler.interruptBlock(bmlId);
        }

        switch (BMLBComposition.parse(bb.getSchedulingMechanism().getNameStart()))
        {
        case REPLACE:
            scheduler.reset();
            break;
        default:
        case UNKNOWN:
            log.warn("Unknown scheduling composition {} in BML block {}, defaulting to merge",bb.getSchedulingMechanism(),bb.getBmlId());
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

        double predictedStart = Math.max(scheduler.predictEndTime(appendAfter), scheduler.predictSubsidingTime(chunkAfter));
        scheduler.planningStart(bb.id, predictedStart);

        // logger.debug("Scheduling started at: {}",schedulingClock.getTime());
        BMLBBlock bbm = new BMLBBlock(bb.id, scheduler, pegBoard, appendAfter, bmltAttr.getOnStartList(), chunkAfter);
        BMLBlockPeg bmlBlockPeg = new BMLBlockPeg(bb.id, predictedStart);
        scheduler.addBMLBlockPeg(bmlBlockPeg);

        strategy.schedule(bb.getSchedulingMechanism(), bb, bmlBlockPeg, scheduler, scheduler.getSchedulingTime());
        log.debug("Scheduling finished at: {}", scheduler.getSchedulingTime());
        scheduler.removeInvalidBehaviors(bb.id);

        predictedStart = Math.max(scheduler.predictEndTime(appendAfter), scheduler.predictSubsidingTime(chunkAfter));
        scheduler.addBMLBlock(bbm);

        scheduler.planningFinished(bb.id, predictedStart, scheduler.predictEndTime(bb.id));
        if (bmltAttr.isPrePlanned())
        {
            log.debug("Preplanning {}.", bb.id);
            bbm.setState(TimedPlanUnitState.PENDING);
        }
        else
        {
            switch (BMLBComposition.parse(bb.getSchedulingMechanism().getNameStart()))
            {
            case REPLACE:
            case UNKNOWN:
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
