/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.bml.ext.bmla.BMLASchedulingMechanism;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Creates BMLT blocks and handles the first part of the BMLT state machine
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class BMLASchedulingHandler implements SchedulingHandler
{
    private final SchedulingStrategy strategy;
    private final PegBoard pegBoard;

    public BMLASchedulingHandler(SchedulingStrategy strategy, PegBoard pb)
    {
        this.strategy = strategy;
        this.pegBoard = pb;
    }

    private boolean addBMLBlockAppendAfterTarget(String bmlId, String appender, BMLBlockManager bbm)
    {
        BMLBBlock b = bbm.getBMLBlock(bmlId);
        if (b == null)
        {
            return false;
        }
        if (b.getState().equals(TimedPlanUnitState.IN_PREP) || b.getState().equals(TimedPlanUnitState.LURKING))
        {
            b.addAppendTarget(appender);
            return true;
        }
        return false;
    }

    private boolean addBMLBlockChunkAfterTarget(String bmlId, String appender, BMLBlockManager bbm)
    {
        BMLBBlock b = bbm.getBMLBlock(bmlId);
        if (b == null)
        {
            return false;
        }
        if (b.getState().equals(TimedPlanUnitState.IN_PREP) || b.getState().equals(TimedPlanUnitState.LURKING))
        {
            b.addChunkTarget(appender);
            return true;

        }
        return false;
    }

    public boolean checkAndApplyBlockBeforeConstraints(BehaviourBlock bb, BMLScheduler scheduler, BMLABMLBehaviorAttributes bmlaAttr)
    {
        for (String chunkBefore : bmlaAttr.getChunkBeforeList())
        {
            if (!addBMLBlockChunkAfterTarget(chunkBefore, bb.getBmlId(), scheduler.getBMLBlockManager()))
            {
                scheduler.warn(new BMLWarningFeedback(bb.getBmlId(), "TargetBlockAlreadyStartedException", "Block " + chunkBefore + " in"
                        + "chunkBefore was already started"), scheduler.getSchedulingTime());
                return false;
            }
        }
        for (String prependBefore : bmlaAttr.getPrependBeforeList())
        {
            if (!addBMLBlockAppendAfterTarget(prependBefore, bb.getBmlId(), scheduler.getBMLBlockManager()))
            {
                scheduler.warn(new BMLWarningFeedback(bb.getBmlId(), "TargetBlockAlreadyStartedException", "Block " + prependBefore + " in"
                        + "prependBefore was already started"), scheduler.getSchedulingTime());
                return false;
            }
        }
        return true;
    }

    @Override
    public void schedule(BehaviourBlock bb, BMLScheduler scheduler, double time)
    {
        BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
        handleInterrupt(scheduler, bmlaAttr, time);

        Set<String> appendAfter = new HashSet<String>();
        Set<String> chunkAfter = new HashSet<String>();
        handleComposition(bb, scheduler, bmlaAttr, appendAfter, chunkAfter);

        double predictedStart = Math.max(scheduler.predictEndTime(appendAfter), scheduler.predictSubsidingTime(chunkAfter));
        scheduler.planningStart(bb.id, predictedStart);

        BMLBBlock bbm = new BMLBBlock(bb.id, scheduler, pegBoard, appendAfter, bmlaAttr.getOnStartList(), chunkAfter);
        scheduler.getBMLBlockManager().addBMLBlock(bbm);
        if (!checkAndApplyBlockBeforeConstraints(bb, scheduler, bmlaAttr))
        {
            return;
        }
        schedule(bb, scheduler, appendAfter, chunkAfter, bbm, predictedStart);
        setupBlockStartState(bb, scheduler, bmlaAttr, appendAfter, chunkAfter, bbm, scheduler.getSchedulingTime());
    }

    private void schedule(BehaviourBlock bb, BMLScheduler scheduler, Set<String> appendAfter, Set<String> chunkAfter, BMLBBlock bbm,
            double predictedStart)
    {
        BMLBlockPeg bmlBlockPeg = new BMLBlockPeg(bb.id, predictedStart);
        scheduler.addBMLBlockPeg(bmlBlockPeg);

        strategy.schedule(bb.getComposition(), bb, bmlBlockPeg, scheduler, scheduler.getSchedulingTime());
        log.debug("Scheduling finished at: {}", scheduler.getSchedulingTime());
        scheduler.removeInvalidBehaviors(bb.id, scheduler.getSchedulingTime());

        predictedStart = Math.max(scheduler.predictEndTime(appendAfter), scheduler.predictSubsidingTime(chunkAfter));
        scheduler.addBMLBlock(bbm);
        bmlBlockPeg.setValue(predictedStart);

        scheduler.planningFinished(bb, predictedStart, scheduler.predictEndTime(bb.id));
    }

    private void handleComposition(BehaviourBlock bb, BMLScheduler scheduler, BMLABMLBehaviorAttributes bmlaAttr, Set<String> appendAfter,
            Set<String> chunkAfter)
    {
        switch (BMLASchedulingMechanism.parse(bb.getComposition().getNameStart()))
        {
        case REPLACE:
            scheduler.reset();
            break;
        default:
        case UNKNOWN:
            log.warn("Unknown scheduling composition {} in BML block {}, defaulting to merge", bb.getComposition(), bb.getBmlId());
            break;
        case MERGE:
            break;
        case APPEND:
            appendAfter.addAll(scheduler.getBMLBlocks());
            break;
        case APPEND_AFTER:
            appendAfter.addAll(bmlaAttr.getAppendAfterList());
            appendAfter.retainAll(scheduler.getBMLBlocks());
            break;
        }
        appendAfter.addAll(bmlaAttr.getAppendAfterList());
        chunkAfter.addAll(bmlaAttr.getChunkAfterList());
    }

    private void handleInterrupt(BMLScheduler scheduler, BMLABMLBehaviorAttributes bmlaAttr, double time)
    {
        for (String bmlId : bmlaAttr.getInterruptList())
        {
            log.debug("interrupting {}", bmlId);
            scheduler.interruptBlock(bmlId, time);
        }
    }

    private void setupBlockStartState(BehaviourBlock bb, BMLScheduler scheduler, BMLABMLBehaviorAttributes bmlaAttr,
            Set<String> appendAfter, Set<String> chunkAfter, BMLBBlock bbm, double time)
    {
        if (bmlaAttr.isPrePlanned())
        {
            log.debug("Preplanning {}.", bb.id);
            bbm.setState(TimedPlanUnitState.PENDING);
        }
        else
        {
            switch (BMLASchedulingMechanism.parse(bb.getComposition().getNameStart()))
            {
            case REPLACE:
            case UNKNOWN:
            case MERGE:
                if (appendAfter.size() > 0 || chunkAfter.size() > 0)
                {
                    bbm.setState(TimedPlanUnitState.LURKING);
                    scheduler.updatePredictions(bbm.getBMLId());
                    scheduler.updateBMLBlocks(time);
                }
                else
                {
                    scheduler.startBlock(bb.id, time);
                }
                break;
            case APPEND_AFTER:
            case APPEND:
                bbm.setState(TimedPlanUnitState.LURKING);
                scheduler.updatePredictions(bbm.getBMLId());
                scheduler.updateBMLBlocks(time);
                break;
            }
        }
    }

}
