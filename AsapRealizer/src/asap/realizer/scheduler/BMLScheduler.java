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

import hmi.util.Clock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.BMLParser;
import saiba.bml.parser.InvalidSyncRefException;
import saiba.bml.parser.SyncPoint;
import asap.bml.ext.bmla.feedback.BMLABlockPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockProgressFeedback;
import asap.realizer.BehaviorNotFoundException;
import asap.realizer.Engine;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.TimePegAlreadySetException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerport.BMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * BMLScheduler, handles BML block states, feedback listeners and maintains engines and
 * anticipators. A scheduling strategy needs to be provided to hand off the actual scheduling to.
 * 
 * @author Herwin van Welbergen
 */
public final class BMLScheduler
{
    private Map<String, BehaviourBlock> bmlBlockMap = new HashMap<String, BehaviourBlock>();

    private final Logger logger = LoggerFactory.getLogger(BMLScheduler.class.getName());

    private final BMLParser parser;

    private final BMLBlockManager bmlBlocksManager;

    private final Map<Class<? extends Behaviour>, Engine> planSelector;

    private final Set<Engine> engines;

    private final PegBoard pegBoard;

    private final Clock schedulingClock;

    private final SchedulingHandler schedulingHandler;

    private interface FeedbackManagerDelegates
    {
        void warn(BMLWarningFeedback w);

        void prediction(BMLPredictionFeedback bpf);

        void addFeedbackListener(BMLFeedbackListener e);

        void removeFeedbackListener(BMLFeedbackListener e);

        void removeAllFeedbackListeners();
    }

    @Delegate(types = FeedbackManagerDelegates.class)
    private final FeedbackManager fbManager;

    public double getSchedulingTime()
    {
        return schedulingClock.getMediaSeconds();
    }

    public BMLScheduler(String characterId, BMLParser s, FeedbackManager bfm, Clock c, SchedulingHandler sh, BMLBlockManager bbm,
            PegBoard pb)
    {
        fbManager = bfm;
        parser = s;
        schedulingClock = c;
        schedulingHandler = sh;
        pegBoard = pb;
        planSelector = new HashMap<Class<? extends Behaviour>, Engine>();
        engines = new HashSet<Engine>();
        bmlBlocksManager = bbm;
    }

    public double getRigidity(Behaviour beh) // throws NoEngineForBehaviourException
    {
        Engine e = planSelector.get(beh.getClass());
        if (e == null)
        {
            // throw new NoEngineForBehaviourException(beh);
            return 0;
        }
        return e.getRigidity(beh);
    }

    /**
     * Get the syncs of behavior id that have a TimePeg value != TimePeg.ValueUnknown
     */
    public Set<String> getTimedSyncs(String bmlId, String behId)
    {
        return pegBoard.getTimedSyncs(bmlId, behId);
    }

    public TimePeg getTimePeg(String bmlId, String behId, String syncId) throws BehaviorNotFoundException, SyncPointNotFoundException,
            TimePegAlreadySetException
    {
        TimePeg tp = pegBoard.getTimePeg(bmlId, behId, syncId);
        if (tp == null)
        {
            for (Engine e : getEngines())
            {
                if (e.containsBehaviour(bmlId, behId))
                {
                    tp = e.createOffsetPeg(bmlId, behId, syncId);
                    break;
                }
            }
        }
        return tp;
    }

    public void setParameterValue(String bmlId, String behId, String paramId, float value) throws ParameterException,
            BehaviorNotFoundException
    {
        boolean found = false;
        for (Engine e : getEngines())
        {
            if (e.containsBehaviour(bmlId, behId))
            {
                e.setFloatParameterValue(bmlId, behId, paramId, value);
                found = true;
            }
        }
        if (!found) throw new BehaviorNotFoundException(bmlId, behId);
    }

    public void setParameterValue(String bmlId, String behId, String paramId, String value) throws ParameterException,
            BehaviorNotFoundException
    {
        boolean found = false;
        for (Engine e : getEngines())
        {
            if (e.containsBehaviour(bmlId, behId))
            {
                e.setParameterValue(bmlId, behId, paramId, value);
                found = true;
            }
        }
        if (!found) throw new BehaviorNotFoundException(bmlId, behId);
    }

    public float getFloatParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException
    {
        for (Engine e : getEngines())
        {
            if (e.containsBehaviour(bmlId, behId))
            {
                return e.getFloatParameterValue(bmlId, behId, paramId);
            }
        }
        throw new BehaviorNotFoundException(bmlId, behId);
    }

    public String getParameterValue(String bmlId, String behId, String paramId) throws ParameterException, BehaviorNotFoundException
    {
        for (Engine e : getEngines())
        {
            if (e.containsBehaviour(bmlId, behId))
            {
                return e.getParameterValue(bmlId, behId, paramId);
            }
        }
        throw new BehaviorNotFoundException(bmlId, behId);
    }

    public void updateTiming(String bmlId)
    {
        for (Engine e : getEngines())
        {
            e.updateTiming(bmlId);
        }
    }

    /**
     * Get the list of engines
     */
    public ImmutableSet<Engine> getEngines()
    {
        return ImmutableSet.copyOf(engines);
    }

    public void addBMLBlock(BMLBlock bbm)
    {
        for (Engine e : getEngines())
        {
            e.setBMLBlockState(bbm.getBMLId(), TimedPlanUnitState.PENDING);
        }
    }

    public Set<String> getBMLBlocks()
    {
        return bmlBlocksManager.getBMLBlocks();
    }

    /**
     * Forces an update in the state machines of the bml blocks
     */
    public void updateBMLBlocks()
    {
        bmlBlocksManager.updateBlocks();
    }

    /**
     * Resets all engines, that is: stops running behaviour and restores their players to the start
     * state.
     */
    public void reset()
    {
        for (Engine e : getEngines())
        {
            e.reset(schedulingClock.getMediaSeconds());
        }
        bmlBlocksManager.updateBlocks();
        bmlBlocksManager.clear();
        pegBoard.clear();
    }

    public void shutdown()
    {
        for (Engine e : getEngines())
        {
            e.shutdown();
        }
    }

    /**
     * Adds an engine that can plan Behaviour class c
     * 
     * @param c
     *            behaviour class the engine can plan
     * @param p
     *            the engine
     */
    public void addEngine(Class<? extends Behaviour> c, Engine e)
    {
        planSelector.put(c, e);
        engines.add(e);
    }

    private void addBehaviorPredictions(BehaviourBlock bb, BMLPredictionFeedback bpf)
    {
        for (Behaviour b : bb.behaviours)
        {
            Behaviour bPred = b;
            bPred.removeSyncPoints(b.getSyncPoints());
            double start = pegBoard.getRelativePegTime(bb.id, b.id, "start");
            double end = pegBoard.getRelativePegTime(bb.id, b.id, "end");
            if (start != TimePeg.VALUE_UNKNOWN)
            {
                SyncPoint sp = new SyncPoint(bb.getBmlId(), b.id, "start");
                try
                {
                    sp.setRefString("" + start);
                }
                catch (InvalidSyncRefException e)
                {
                    throw new AssertionError(e);
                }
                bPred.addSyncPoint(sp);
            }
            if (end != TimePeg.VALUE_UNKNOWN)
            {
                SyncPoint sp = new SyncPoint(bb.getBmlId(), b.id, "end");
                try
                {
                    sp.setRefString("" + end);
                }
                catch (InvalidSyncRefException e)
                {
                    throw new AssertionError(e);
                }
                bPred.addSyncPoint(sp);
            }
            bpf.addBehaviorPrediction(bPred);
        }

    }

    private BMLABlockPredictionFeedback createBMLABlockPredictionFeedback(String bmlId, double predictedStart, double predictedEnd)
    {
        long timeOffset = System.currentTimeMillis() - (long) (getSchedulingTime() * 1000);
        long posixStart = (long) (predictedStart * 1000) + timeOffset, posixEnd = 0;
        if (predictedEnd != BMLBlockPredictionFeedback.UNKNOWN_TIME)
        {
            posixEnd = (long) (predictedEnd * 1000) + timeOffset;
        }
        return new BMLABlockPredictionFeedback(bmlId, predictedStart, predictedEnd, posixStart, posixEnd);
    }

    private BMLPredictionFeedback createStartPrediction(BehaviourBlock bb)
    {
        BMLPredictionFeedback bpf = new BMLPredictionFeedback();
        bpf.addBMLBlockPrediction(createBMLABlockPredictionFeedback(bb.getBmlId(), getSchedulingTime(), predictEndTime(bb.getBmlId())));
        addBehaviorPredictions(bb, bpf);
        return bpf;
    }

    private BMLPredictionFeedback createFilledBlockPrediction(BehaviourBlock bb, double predictedStart, double predictedEnd)
    {
        BMLPredictionFeedback bpf = new BMLPredictionFeedback();
        bpf.addBMLBlockPrediction(createBMLABlockPredictionFeedback(bb.getBmlId(), predictedStart, predictedEnd));
        addBehaviorPredictions(bb, bpf);
        return bpf;
    }

    private BMLPredictionFeedback createSingleBlockPrediction(String bmlId, double predictedStart, double predictedEnd)
    {
        BMLPredictionFeedback bpf = new BMLPredictionFeedback();
        bpf.addBMLBlockPrediction(createBMLABlockPredictionFeedback(bmlId, predictedStart, predictedEnd));
        return bpf;
    }

    public void updatePredictions(String bmlId)
    {
        //TODO: implement this
    }

    public void planningStart(String bmlId, double predictedStart)
    {
        prediction(createSingleBlockPrediction(bmlId, predictedStart, BMLBlockPredictionFeedback.UNKNOWN_TIME));
    }

    public void planningFinished(BehaviourBlock bb, double predictedStart, double predictedEnd)
    {
        prediction(createFilledBlockPrediction(bb, predictedStart, predictedEnd));
    }

    /**
     * Sends feedback to all feedback listeners
     * 
     * @param fb
     *            the feedback
     */
    public void blockStopFeedback(String bmlId)
    {
        fbManager.blockProgress(new BMLABlockProgressFeedback(bmlId, "end", schedulingClock.getMediaSeconds()));
    }

    public void blockStartFeedback(String bmlId)
    {
        fbManager.blockProgress(new BMLABlockProgressFeedback(bmlId, "start", schedulingClock.getMediaSeconds()));
    }

    /**
     * Get the engine that can plan Behaviour class c
     * 
     * @param c
     *            behaviour class the engine can plan
     */
    public Engine getEngine(Class<? extends Behaviour> c)
    {
        return planSelector.get(c);
    }

    public void removeBehaviour(String bmlId, String behaviourId)
    {
        for (Engine e : getEngines())
        {
            e.stopBehaviour(bmlId, behaviourId, schedulingClock.getMediaSeconds());
        }
        pegBoard.removeBehaviour(bmlId, behaviourId);
    }

    boolean isPending(String bmlId, Set<String> bmlIdsChecked)
    {
        return bmlBlocksManager.isPending(bmlId, bmlIdsChecked);
    }

    public boolean isPending(String bmlId)
    {
        return bmlBlocksManager.isPending(bmlId);
    }

    public double getStartTime(String bmlId)
    {
        return pegBoard.getBMLBlockTime(bmlId);
    }

    public double getEndTime(String bmlId, String behId)
    {
        for (Engine e : getEngines())
        {
            double endTime = e.getEndTime(bmlId, behId);
            if (endTime != TimePeg.VALUE_UNKNOWN)
            {
                return endTime;
            }
        }
        return TimePeg.VALUE_UNKNOWN;
    }

    public Set<String> getBehaviours(String bmlId)
    {
        HashSet<String> behaviours = new HashSet<String>();
        for (Engine e : getEngines())
        {
            behaviours.addAll(e.getBehaviours(bmlId));
        }
        return behaviours;
    }

    /**
     * predict the subsiding time of a BML blocks
     */
    public double predictSubsidingTime(String bmlId)
    {
        List<Double> subsidingTimes = new ArrayList<Double>();
        subsidingTimes.add(schedulingClock.getMediaSeconds());
        for (Engine e : getEngines())
        {
            subsidingTimes.add(e.getBlockSubsidingTime(bmlId));
        }
        return Collections.max(subsidingTimes);
    }

    /**
     * predict the subsiding time of a set of BML blocks
     */
    public double predictSubsidingTime(Set<String> bmlIds)
    {
        List<Double> subsidingTimes = new ArrayList<Double>();
        subsidingTimes.add(schedulingClock.getMediaSeconds());
        for (String bmlId : bmlIds)
        {
            subsidingTimes.add(predictSubsidingTime(bmlId));
        }
        return Collections.max(subsidingTimes);
    }

    /**
     * predict the end time of a set of BML blocks
     */
    public double predictEndTime(Set<String> bmlIds)
    {
        List<Double> endTimes = new ArrayList<Double>();
        endTimes.add(schedulingClock.getMediaSeconds());
        for (String bmlId : bmlIds)
        {
            endTimes.add(predictEndTime(bmlId));
        }
        return Collections.max(endTimes);
    }

    /**
     * predict the endtime of the BML block with id
     * blockId
     */
    public double predictEndTime(String blockId)
    {
        List<Double> endTimes = new ArrayList<Double>();
        endTimes.add(schedulingClock.getMediaSeconds());
        for (Engine e : getEngines())
        {
            endTimes.add(e.getBlockEndTime(blockId));
        }
        if (endTimes.size() == 0)
        {
            return BMLPredictionFeedback.UNKNOWN_TIME;
        }
        return Collections.max(endTimes);
    }

    /**
     * Schedules the behaviors provided to this scheduler (e.g. by grabbing them from the parser)
     */
    public void schedule()
    {
        for (BehaviourBlock bb : parser.getBehaviourBlocks())
        {
            bmlBlockMap.put(bb.id, bb);
            schedulingHandler.schedule(bb, this);
        }
        parser.clear();
    }

    public void stopBehavior(String bmlId, String behaviourId)
    {
        for (Engine e : getEngines())
        {
            e.stopBehaviour(bmlId, behaviourId, schedulingClock.getMediaSeconds());
        }
        bmlBlocksManager.updateBlocks();
    }

    public void interruptBehavior(String bmlId, String behaviourId)
    {
        for (Engine e : getEngines())
        {
            e.interruptBehaviour(bmlId, behaviourId, schedulingClock.getMediaSeconds());
        }
        bmlBlocksManager.updateBlocks();
    }

    /**
     * Stops and removes all behaviors of block bmlId
     */
    public void interruptBlock(String bmlId)
    {
        if (!bmlBlocksManager.getBMLBlocks().contains(bmlId))
        {
            logger.debug("Attempting to stop non existing bml block {}", bmlId);
            return;
        }

        for (Engine e : getEngines())
        {
            e.interruptBehaviourBlock(bmlId, schedulingClock.getMediaSeconds());
        }

        bmlBlocksManager.finishBlock(bmlId);
        bmlBlocksManager.removeBMLBlock(bmlId);
    }

    public void activateBlock(String bmlId)
    {
        bmlBlocksManager.activateBlock(bmlId);
    }

    /**
     * Add the blockpeg to the peg board
     */
    public void addBMLBlockPeg(BMLBlockPeg peg)
    {
        pegBoard.addBMLBlockPeg(peg);
    }

    /**
     * Set the state of all behaviors in block with bmlId to LURKING
     * 
     * @param bmlId
     */
    public void startBlock(String bmlId)
    {
        if (!bmlBlocksManager.getBMLBlocks().contains(bmlId))
        {
            logger.warn("Attempting to start non existing bml block {}", bmlId);
            return;
        }

        pegBoard.setBMLBlockTime(bmlId, schedulingClock.getMediaSeconds());
        logger.debug("Starting bml block {}", bmlId);

        // prediction(createBehaviorPrediction(bmlBlockMap.get(bmlId)));
        bmlBlocksManager.startBlock(bmlId);

        for (Engine e : getEngines())
        {
            e.setBMLBlockState(bmlId, TimedPlanUnitState.LURKING);
        }
        bmlBlocksManager.updateBlocks();
        if(bmlBlocksManager.getBMLBlock(bmlId).getState()!=TimedPlanUnitState.DONE)
        {
            prediction(createStartPrediction(bmlBlockMap.get(bmlId)));
        }
    }

    public void activateBlock(String bmlId, double time)
    {
        bmlBlocksManager.activateBlock(bmlId);
    }

    public BMLParser getParser()
    {
        return parser;
    }

    public void removeInvalidBehaviors(String bmlBlock)
    {
        for (Engine e : getEngines())
        {
            logger.debug("Checking behavior validity for engine {}", e.getClass());
            Collection<String> invalidBehaviours = e.getInvalidBehaviours();
            for (String id : invalidBehaviours)
            {
                String bmlId = id.split(":")[0];
                if (bmlId.equals(bmlBlock))
                {
                    String behId = id.substring(bmlId.length() + 1);
                    String warningText = "Invalid timing for behavior " + id + ", behavior ommitted";
                    warn(new BMLWarningFeedback(bmlId + ":" + behId, "INVALID_TIMING", warningText));
                    removeBehaviour(bmlId, behId);
                }
            }
        }
    }

    public void removeInvalidBehaviors()
    {
        logger.debug("Checking behavior validity");
        // remove all invalid behaviours and warn
        for (Engine e : getEngines())
        {
            logger.debug("Checking behavior validity for engine {}", e.getClass());
            Collection<String> invalidBehaviours = e.getInvalidBehaviours();
            for (String id : invalidBehaviours)
            {
                String bmlId = id.split(":")[0];
                String behId = id.substring(bmlId.length() + 1);
                String warningText = "Invalid timing for behavior " + id + ", behavior ommitted";
                warn(new BMLWarningFeedback(bmlId + ":" + behId, "INVALID_TIMING", warningText));
                removeBehaviour(bmlId, behId);
            }
        }
    }

    /**
     * Get a copy of the set of progress messages send for a certain behavior
     * 
     * @see asap.realizer.scheduler.BMLBlockManager#getSyncProgress(java.lang.String, java.lang.String)
     */
    public ImmutableSet<BMLSyncPointProgressFeedback> getSyncProgress(String bmlId, String behaviorId)
    {
        return bmlBlocksManager.getSyncProgress(bmlId, behaviorId);
    }

    /**
     * Get the set of syncs that are finished for a certain behavior
     */
    public ImmutableSet<String> getSyncsPassed(String bmlId, String behaviorId)
    {
        return bmlBlocksManager.getSyncsPassed(bmlId, behaviorId);
    }

    public TimedPlanUnitState getBMLBlockState(String bmlId)
    {
        return bmlBlocksManager.getBMLBlockState(bmlId);
    }

    public BMLBlockManager getBMLBlockManager()
    {
        return bmlBlocksManager;
    }
}
