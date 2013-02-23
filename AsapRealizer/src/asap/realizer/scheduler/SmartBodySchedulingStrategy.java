/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.realizer.scheduler;

import java.util.ArrayList;
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
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Engine;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.anticipator.Anticipator;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Strategy implementing the <a
 * href="http://sourceforge.net/apps/mediawiki/smartbody">SmartBody></a> scheduling algorithm:<br>
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

        // Time-shifting pass for time pegs with negative time
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
                            scheduler.warn(new BMLWarningFeedback(b.getBmlId() + ":" + b.id, "CANNOT_SATISFY_CONSTRAINTS", warningText));

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
        ArrayList<TimePegAndConstraint> syncList = new ArrayList<TimePegAndConstraint>();

        // link realizer syncs to behavior syncs (+ constraints)
        for (Constraint c : scheduler.getParser().getConstraints())
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

            for (ConstrInfo ci : constraintInfo)
            {
                boolean syncExists = false;

                // try to link to an existing TimePegAndConstraint
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

                // can't be linked to an existing TimePegAndConstraint,
                // create a new one
                if (!syncExists)
                {
                    // TimePegAndConstraint linked to static time or
                    // anticipator?
                    boolean isStaticOrAnticipator = false;
                    for (SyncPoint s : c.getTargets())
                    {
                        if (s.getBehaviourId() == null)
                        {
                            if (s.getBmlId().equals(BMLTInfo.ANTICIPATORBLOCKID))
                            {
                                // synched to anticipator
                                String str[] = s.getName().split(":");
                                if (str.length == 2 && !str[0].equals("bml"))
                                {
                                    Anticipator ap = scheduler.getAnticipator(str[0]);
                                    isStaticOrAnticipator = true;
                                    if (ap != null)
                                    {
                                        TimePeg sp = ap.getSynchronisationPoint(str[1]);
                                        if (sp == null)
                                        {
                                            String warningText = "Unknown anticipator synchronization point " + s.getName()
                                                    + " sync ignored.";
                                            scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, "UNKNOWN_ANTICIPATOR_SYNC",
                                                    warningText));
                                            break;
                                        }

                                        syncList.add(new TimePegAndConstraint(ci.syncId, sp, c, ci.offset - s.offset));
                                        logger.debug("Link to anticipator: {} at time {} with offset {}",
                                                new Object[] { str[1], sp.getGlobalValue(), ci.offset });
                                        sp.setAbsoluteTime(true);
                                        break;
                                    }
                                    else
                                    {
                                        String warningText = "Unknown sync point " + s.getName() + " sync ignored.";
                                        scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, "UNKNOWN_SYNCPOINT", warningText));
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                // static sync
                                isStaticOrAnticipator = true;
                                TimePeg sync = new TimePeg(bmlBlockPeg);
                                sync.setAbsoluteTime(true);
                                sync.setLocalValue(s.getOffset());
                                syncList.add(new TimePegAndConstraint(ci.syncId, sync, c, ci.offset));
                                break;
                            }
                        }
                        if (!s.getBmlId().equals(bmlId))
                        {
                            isStaticOrAnticipator = true;
                            TimePeg sp = pegBoard.getTimePeg(s.getBmlId(), s.getBehaviourId(), s.getName());
                            syncList.add(new TimePegAndConstraint(ci.syncId, sp, c, ci.offset - s.offset));
                            logger.debug("Link to behavior in other block: {}:{} at time {} with offset {}",
                                    new Object[] { s.getBmlId(), s.getBehaviourId(), sp.getGlobalValue(), ci.offset - s.offset });
                            sp.setAbsoluteTime(true);
                            break;
                        }
                        else
                        {
                            // TODO: handle this
                        }
                    }
                    if (!isStaticOrAnticipator)
                    {
                        // add new sync, unknown timing
                        TimePeg sync = new TimePeg(bmlBlockPeg);
                        sync.setGlobalValue(TimePeg.VALUE_UNKNOWN);
                        syncList.add(new TimePegAndConstraint(ci.syncId, sync, c, ci.offset));
                    }
                }
            }
        }

        // make sure start is always resolved
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
                scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, "BehaviourPlanningException", ex.getMessage()));
                return;
            }
        }
        else
        {
            // TODO: create error and cancel the block if the behaviour is a
            // required
            // behavior/its constraints are required
            String warningText = "No planner found to plan behavior of type <" + b.getXMLTag() + ">, behavior ommitted";
            scheduler.warn(new BMLWarningFeedback(bmlId + ":" + b.id, BMLWarningFeedback.CANNOT_CREATE_BEHAVIOR, warningText));
            return;
        }
        syncMap.put(b, syncList);
        scheduledBehaviors.add(b);
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
