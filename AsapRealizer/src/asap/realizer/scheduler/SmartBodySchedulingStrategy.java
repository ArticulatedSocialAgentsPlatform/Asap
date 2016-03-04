/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.Constraint;
import saiba.bml.parser.SyncPoint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Engine;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Strategy implementing the <a href="http://sourceforge.net/apps/mediawiki/smartbody">SmartBody</a> scheduling algorithm:<br>
 * 
 * The behaviors are processed in BML order. The timing of the first behavior is constrained only by
 * the absolute time constraints it defines and by constraints imposed by Anticipators. Subsequent
 * behaviors are timed so that they adhere to time constraints imposed by the already processed
 * behaviors. If time constraints allow it, each behavior will be assigned its preferred duration.
 * 
 * @author Herwin van Welbergen
 * 
 */
public class SmartBodySchedulingStrategy implements SchedulingStrategy
{
    private final PegBoard pegBoard;

    public SmartBodySchedulingStrategy(PegBoard pb)
    {
        pegBoard = pb;
    }

    private static final Logger logger = LoggerFactory.getLogger(SmartBodySchedulingStrategy.class.getName());

    private static class ConstrInfo
    {
        String syncId;

        double offset;

        public ConstrInfo(String s, double o)
        {
            syncId = s;
            offset = o;
        }
    }

    @Override
    public void schedule(BMLBlockComposition mechanism, BehaviourBlock bb, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler,
            double scheduleTime)
    {
        ArrayList<Behaviour> scheduledBehaviors = new ArrayList<Behaviour>();
        HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap = new HashMap<Behaviour, ArrayList<TimePegAndConstraint>>();

        for (Behaviour b : bb.behaviours)
        {
            scheduleBehaviour(mechanism, bb.id, bmlBlockPeg, scheduler, scheduleTime, scheduledBehaviors, syncMap, b);
        }
        timeShiftPass(bb, scheduler, scheduledBehaviors, syncMap);
    }

    /**
     * // Time-shifting pass for time pegs with negative time
     */
    private void timeShiftPass(BehaviourBlock bb, BMLScheduler scheduler, ArrayList<Behaviour> scheduledBehaviors,
            HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap)
    {
        boolean moving = true;
        ArrayList<Behaviour> behToRemove = new ArrayList<Behaviour>();
        while (moving)
        {
            moving = false;
            for (Behaviour b : scheduledBehaviors)
            {
                for (TimePegAndConstraint tpac : syncMap.get(b))
                {
                    double t = pegBoard.getRelativePegTime(bb.id, tpac.peg) - tpac.offset;

                    // allow behaviors to start one microsecond too early here to avoid infinite
                    // loops
                    // with very small t
                    if (t < -0.000001 && t != TimePeg.VALUE_UNKNOWN)
                    {
                        logger.debug("negative time: {}:{}", b.id, tpac.syncId);
                        logger.debug("Value: {} Offset: {}", tpac.peg.getGlobalValue(), tpac.offset);
                        double move = -t;
                        if (!tpac.peg.isAbsoluteTime())
                        {
                            movePeg(move, b, syncMap);
                            moving = true;
                        }
                        else if (tpac.peg.getGlobalValue() < 0 || tpac.peg.getBmlId().equals(bb.id))
                        {
                            String warningText = "Can't satisfy time constraints " + tpac + " on <" + b.getXMLTag()
                                    + ">, behavior ommitted";
                            scheduler.warn(new BMLWarningFeedback(b.getBmlId() + ":" + b.id, "CANNOT_SATISFY_CONSTRAINTS", warningText),
                                    scheduler.getSchedulingTime());

                            // TODO: create error and cancel the block if the
                            // behaviour is a
                            // required behavior/its constraints are required
                            behToRemove.add(b);
                        }
                        else
                        {
                            logger.warn("Absolute constraints require behavior {}:{} to start earlier than its BML block", b.getBmlId(),
                                    b.id);
                        }
                        break;
                    }
                }
                if (moving) break;
            }
        }
        for (Behaviour b : behToRemove)
        {
            scheduler.removeBehaviour(b.getBmlId(), b.id);
        }
    }

    private void scheduleBehaviour(BMLBlockComposition mechanism, String bmlId, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler,
            double scheduleTime, ArrayList<Behaviour> scheduledBehaviors, HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap,
            Behaviour b)
    {
        // constraints to solve for this behavior
        ArrayList<TimePegAndConstraint> syncList = new ArrayList<TimePegAndConstraint>();

        // link realizer syncs to behavior syncs (+ constraints)
        for (Constraint c : scheduler.getParser().getConstraints())
        {
            ArrayList<ConstrInfo> constraintInfo = findConstraints(b, c);
            setupTimePegAndConstraints(bmlId, bmlBlockPeg, scheduler, scheduledBehaviors, syncMap, b, syncList, c, constraintInfo);
        }
        ensureStartResolved(bmlBlockPeg, syncList);

        // resolve unknown syncs
        Engine eng = scheduler.getEngine(b.getClass());
        if (eng != null)
        {
            try
            {
                TimedPlanUnit planElement = eng.resolveSynchs(bmlBlockPeg, b, syncList);
                List<SyncAndTimePeg> satp = eng.addBehaviour(bmlBlockPeg, b, syncList, planElement);
                pegBoard.addTimePegs(satp);
            }
            catch (BehaviourPlanningException ex)
            {
                // TODO: create error and cancel the block if the behaviour
                // is a required
                // behavior/its constraints are required
                scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, "BehaviourPlanningException", ex.getMessage() + ":\n"
                        + Arrays.toString(ex.getStackTrace())),scheduler.getSchedulingTime());
                return;
            }
        }
        else
        {
            // TODO: create error and cancel the block if the behaviour is a
            // required
            // behavior/its constraints are required
            String warningText = "No planner found to plan behavior of type <" + b.getXMLTag() + ">, behavior ommitted";
            scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, BMLWarningFeedback.CANNOT_CREATE_BEHAVIOR, warningText),
                    scheduler.getSchedulingTime());
            return;
        }
        syncMap.put(b, syncList);
        scheduledBehaviors.add(b);
    }

    private void setupTimePegAndConstraints(String bmlId, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler,
            ArrayList<Behaviour> scheduledBehaviors, HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap, Behaviour b,
            ArrayList<TimePegAndConstraint> syncList, Constraint c, ArrayList<ConstrInfo> constraintInfo)
    {
        for (ConstrInfo ci : constraintInfo)
        {
            if (linkSyncToOtherBehavior(scheduledBehaviors, syncMap, syncList, c, ci))
            {
                continue;
            }
            if (linkSyncToOwnBehavior(syncList, c, ci))
            {
                continue;
            }
            if (linkSyncToExternal(bmlId, bmlBlockPeg, scheduler, b, syncList, c, ci))
            {
                continue;
            }
            // add new sync, unknown timing
            TimePeg sync = new TimePeg(bmlBlockPeg);
            sync.setGlobalValue(TimePeg.VALUE_UNKNOWN);
            syncList.add(new TimePegAndConstraint(ci.syncId, sync, c, ci.offset));
        }
    }

    private boolean linkSyncToExternal(String bmlId, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler, Behaviour b,
            ArrayList<TimePegAndConstraint> syncList, Constraint c, ConstrInfo ci)
    {
        // TimePegAndConstraint linked to static time or
        // anticipator?
        boolean syncExists = false;
        for (SyncPoint s : c.getTargets())
        {
            if (s.getBmlId().equals(BMLBlockPeg.ANTICIPATOR_PEG_ID))
            {
                // synched to anticipator
                TimePeg apPeg = pegBoard.getTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID, s.getBehaviourId(), s.getName());
                syncExists = true;

                if (apPeg == null)
                {
                    String warningText = "Unknown anticipator synchronization point " + s.getName() + " sync ignored.";
                    scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, "UNKNOWN_ANTICIPATOR_SYNC", warningText),
                            scheduler.getSchedulingTime());
                    break;
                }

                syncList.add(new TimePegAndConstraint(ci.syncId, apPeg, c, ci.offset - s.offset));
                logger.debug("Link to anticipator: {} at time {} with offset {}", new Object[] { s.getName(), apPeg.getGlobalValue(),
                        ci.offset });
                apPeg.setAbsoluteTime(true);
                break;
            }
            else if (s.getBehaviourId() == null)
            {
                // static sync
                syncExists = true;
                TimePeg sync = new TimePeg(bmlBlockPeg);
                sync.setAbsoluteTime(true);
                sync.setLocalValue(s.getOffset());
                syncList.add(new TimePegAndConstraint(ci.syncId, sync, c, ci.offset));
                break;
            }
            else if (!s.getBmlId().equals(bmlId))
            {
                // link to outside block
                syncExists = true;
                TimePeg sp = pegBoard.getTimePeg(s.getBmlId(), s.getBehaviourId(), s.getName());
                syncList.add(new TimePegAndConstraint(ci.syncId, sp, c, ci.offset - s.offset));
                logger.debug("Link to behavior in other block: {}:{} at time {} with offset {}",
                        new Object[] { s.getBmlId(), s.getBehaviourId(), sp.getGlobalValue(), ci.offset - s.offset });
                sp.setAbsoluteTime(true);
                break;
            }
        }
        return syncExists;
    }

    // / try to link to an existing Constraint + TimePeg in this behavior
    private boolean linkSyncToOwnBehavior(ArrayList<TimePegAndConstraint> syncList, Constraint c, ConstrInfo ci)
    {
        boolean syncExists = false;

        for (TimePegAndConstraint said : syncList)
        {
            if (said.constr == c)
            {
                syncList.add(new TimePegAndConstraint(ci.syncId, said.peg, c, ci.offset + said.offset));
                syncExists = true;
                break;
            }
        }

        return syncExists;
    }

    // / try to link to an existing Constraint + TimePeg in another behavior
    private boolean linkSyncToOtherBehavior(ArrayList<Behaviour> scheduledBehaviors,
            HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap, ArrayList<TimePegAndConstraint> syncList, Constraint c,
            ConstrInfo ci)
    {
        boolean syncExists = false;
        for (Behaviour b2 : scheduledBehaviors)
        {
            for (TimePegAndConstraint said : syncMap.get(b2))
            {
                if (said.constr == c)
                {
                    syncList.add(new TimePegAndConstraint(ci.syncId, said.peg, c, ci.offset));
                    syncExists = true;
                    break;
                }
            }
            if (syncExists) break;
        }
        return syncExists;
    }

    private ArrayList<ConstrInfo> findConstraints(Behaviour b, Constraint c)
    {
        // find all constraints on this behavior
        ArrayList<ConstrInfo> constraintInfo = new ArrayList<ConstrInfo>();
        for (SyncPoint s : c.getTargets())
        {
            if (s.getBehaviourId() != null && s.getBehaviourId().equals(b.id) && s.getBmlId().equals(b.getBmlId()))
            {
                double offset = s.getOffset();
                String syncId = s.getName();
                constraintInfo.add(new ConstrInfo(syncId, offset));
                logger.debug("Constraint info:{} sync:{} offset:{}", new Object[] { b, syncId, offset });
            }
        }
        return constraintInfo;
    }

    private void ensureStartResolved(BMLBlockPeg bmlBlockPeg, ArrayList<TimePegAndConstraint> syncList)
    {
        boolean resolveStart = true;
        for (TimePegAndConstraint sac : syncList)
        {
            if (sac.syncId.equals("start"))
            {
                resolveStart = false;
                break;
            }
        }
        if (resolveStart)
        {
            TimePeg temp = new TimePeg(bmlBlockPeg);
            OffsetPeg sync = new OffsetPeg(temp, 0, bmlBlockPeg);
            boolean offsetRes = false;
            if (syncList.size() > 0) offsetRes = true;
            syncList.add(new TimePegAndConstraint("start", sync, new Constraint(), 0, offsetRes));
        }
    }

    private boolean moveBehavior(double move, Behaviour b, ArrayList<TimePeg> movedPegs,
            HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap)
    {
        boolean pegAdded = false;

        TimePeg firstAbsolute = null;
        for (TimePegAndConstraint tpac : syncMap.get(b))
        {
            if (tpac.peg.isAbsoluteTime())
            {
                if (firstAbsolute == null || firstAbsolute.getGlobalValue() > tpac.peg.getGlobalValue())
                {
                    firstAbsolute = tpac.peg;
                }
            }
        }

        for (TimePegAndConstraint tpac : syncMap.get(b))
        {
            if (!movedPegs.contains(tpac.peg.getLink())
                    && (firstAbsolute == null || firstAbsolute.getGlobalValue() < tpac.peg.getGlobalValue()))
            {
                logger.debug("Moving {} : {} from {} to {}",
                        new Object[] { b.id, tpac.syncId, tpac.peg.getGlobalValue(), tpac.peg.getGlobalValue() + move });
                tpac.peg.setGlobalValue(tpac.peg.getGlobalValue() + move);
                movedPegs.add(tpac.peg.getLink());
                pegAdded = true;
            }
        }
        return pegAdded;
    }

    private void movePeg(double move, Behaviour b, HashMap<Behaviour, ArrayList<TimePegAndConstraint>> syncMap)
    {
        ArrayList<TimePeg> movedPegs = new ArrayList<TimePeg>();
        ArrayList<Behaviour> movedBehaviors = new ArrayList<Behaviour>();

        // move behavior
        moveBehavior(move, b, movedPegs, syncMap);
        movedBehaviors.add(b);

        boolean pegAdded = true;

        while (pegAdded)
        {
            pegAdded = false;
            // move pegs linked to other behaviors that share a peg with this
            // behavior
            for (Map.Entry<Behaviour, ArrayList<TimePegAndConstraint>> behTpac : syncMap.entrySet())
            {
                Behaviour beh = behTpac.getKey();
                if (!movedBehaviors.contains(beh))
                {
                    for (TimePegAndConstraint tpac : behTpac.getValue())
                    {
                        if (movedPegs.contains(tpac.peg.getLink()))
                        {
                            pegAdded = moveBehavior(move, beh, movedPegs, syncMap);
                            movedBehaviors.add(beh);
                            if (pegAdded) break;
                        }
                    }
                }
                if (pegAdded) break;
            }
        }
    }

}
