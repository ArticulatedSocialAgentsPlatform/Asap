/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hmi.util.Clock;
import hmi.util.ClockListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.BMLParser;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmla.feedback.BMLABlockPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockProgressFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockStatus;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.realizer.BehaviorNotFoundException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Engine;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.TimePegAlreadySetException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.PlanUnitParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerport.util.ListBMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for BMLScheduler
 * 
 * @author Herwin
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ System.class, BMLABlockProgressFeedback.class, BMLScheduler.class })
public class BMLSchedulerTest
{
    private StubEngine stubEngine;
    private PegBoard pegBoard = new PegBoard();
    private static final double PRECISION = 0.0001;

    private static class StubClock implements Clock
    {
        private double time = 0;

        public void setTime(double time)
        {
            this.time = time;
        }

        @Override
        public double getMediaSeconds()
        {
            return time;
        }

        @Override
        public void addClockListener(ClockListener l)
        {
        }
    }

    private static class StubEngine implements Engine
    {
        private Map<String, Set<String>> behMap = new HashMap<String, Set<String>>();
        private Map<String, Double> blockEndTimeMap = new HashMap<String, Double>();
        private String id;

        public StubEngine(String id)
        {
            this.id = id;
        }

        public void addBlockEnd(String bmlId, double time)
        {
            blockEndTimeMap.put(bmlId, time);
        }

        public void addBehaviour(String bmlId, String id)
        {
            if (behMap.get(bmlId) == null)
            {
                behMap.put(bmlId, new HashSet<String>());
            }
            behMap.get(bmlId).add(id);
        }

        @Override
        public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac, TimedPlanUnit planElement)
                throws BehaviourPlanningException
        {
            addBehaviour(b.getBmlId(), b.id);
            return new ArrayList<SyncAndTimePeg>();
        }

        @Override
        public TimedPlanUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
                throws BehaviourPlanningException
        {
            return null;
        }

        @Override
        public void reset(double time)
        {
            behMap.clear();
        }

        @Override
        public Set<String> getInvalidBehaviours()
        {
            return new HashSet<String>();
        }

        @Override
        public void stopBehaviour(String behaviourId, String bmlId, double globalTime)
        {
            if (behMap.get(bmlId) != null)
            {
                behMap.get(bmlId).remove(behaviourId);
            }
        }

        @Override
        public void stopBehaviourBlock(String bmlId, double globalTime)
        {
            behMap.remove(bmlId);
        }

        @Override
        public List<Class<? extends Behaviour>> getSupportedBehaviours()
        {
            return new ArrayList<Class<? extends Behaviour>>();
        }

        @Override
        public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
        {
            return new ArrayList<Class<? extends Behaviour>>();
        }

        @Override
        public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
        {

        }

        @Override
        public Set<String> getBehaviours(String bmlId)
        {
            if (behMap.get(bmlId) == null)
            {
                return new HashSet<String>();
            }
            return behMap.get(bmlId);
        }

        @Override
        public double getEndTime(String bmlId, String behId)
        {
            return 10000000;
        }

        @Override
        public void setFloatParameterValue(String bmlId, String behId, String paramId, float value)
        {
        }

        @Override
        public void setParameterValue(String bmlId, String behId, String paramId, String value)
        {
        }

        @Override
        public void shutdown()
        {
        }

        @Override
        public String getParameterValue(String bmlId, String behId, String paramId) throws PlanUnitParameterNotFoundException,
                BehaviorNotFoundException
        {
            return "";
        }

        @Override
        public float getFloatParameterValue(String bmlId, String behId, String paramId) throws PlanUnitFloatParameterNotFoundException,
                BehaviorNotFoundException
        {
            return 0;
        }

        @Override
        public boolean containsBehaviour(String bmlId, String behId)
        {
            return false;
        }
        
        @Override
        public boolean containsMainBehaviour(String bmlId, String behId)
        {
            return false;
        }

        @Override
        public OffsetPeg createOffsetPeg(String bmlId, String behId, String syncId) throws BehaviorNotFoundException,
                SyncPointNotFoundException, TimePegAlreadySetException
        {
            return null;
        }

        @Override
        public double getBlockEndTime(String bmlId)
        {
            if (blockEndTimeMap.get(bmlId) != null) return blockEndTimeMap.get(bmlId);
            return 0;
        }

        @Override
        public void setId(String newId)
        {
            this.id = newId;
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public void play(double time)
        {

        }

        @Override
        public double getRigidity(Behaviour beh)
        {
            return 0.5;
        }

        @Override
        public double getBlockSubsidingTime(String bmlId)
        {
            return 0;
        }

        @Override
        public void updateTiming(String bmlId)
        {

        }

        @Override
        public String toString()
        {
            return id;
        }

        @Override
        public void interruptBehaviourBlock(String bmlId, double time)
        {
            stopBehaviourBlock(bmlId, time);
        }

        @Override
        public void interruptBehaviour(String bmlId, String behaviourId, double time)
        {
            stopBehaviour(behaviourId, bmlId, time);
        }

       
    }

    private static final long CTM = 123;
    private long currentTimeMillis = CTM;

    private BMLScheduler scheduler;

    private BMLParser parser;

    private ListBMLFeedbackListener listFeedbackListener;

    private List<BMLSyncPointProgressFeedback> feedBackList;

    private List<BMLBlockProgressFeedback> blockProgressFeedbackList;

    private List<BMLWarningFeedback> warningList;

    private List<BMLPredictionFeedback> predictionFeedback = new ArrayList<BMLPredictionFeedback>();

    private StubClock stubClock = new StubClock();

    private BMLBlockManager bbManager = new BMLBlockManager();
    private FeedbackManager fbManager = new FeedbackManagerImpl(bbManager, "character1");

    private List<String> getBMLIdsFromStartFeedback(List<BMLBlockProgressFeedback> fbList)
    {
        List<String> bmlIds = new ArrayList<String>();
        for (BMLBlockProgressFeedback fb : fbList)
        {
            if (fb.getSyncId().equals("start"))
            {
                bmlIds.add(fb.getBmlId());
            }
        }
        return bmlIds;
    }

    private List<String> getBMLIdsFromEndFeedback(List<BMLBlockProgressFeedback> fbList)
    {
        List<String> bmlIds = new ArrayList<String>();
        for (BMLBlockProgressFeedback fb : fbList)
        {
            if (fb.getSyncId().equals("end"))
            {
                bmlIds.add(fb.getBmlId());
            }
        }
        return bmlIds;
    }

    private String createEmptyBML(String bmlId, String extraAttributes)
    {
        return "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "xmlns:bmla=\"http://www.asap-project.org/bmla\" id=\""
                + bmlId + "\" " + extraAttributes + "/>";
    }

    private String createNonEmptyBML(String bmlId, String extraAttributes)
    {

        return "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"" + " xmlns:bmla=\"http://www.asap-project.org/bmla\" id=\""
                + bmlId + "\" " + extraAttributes + "><speech id=\"s1\"><text/></speech></bml>";
    }

    private String createNonEmptyBML(String bmlId)
    {

        return "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"" + "xmlns:bmla=\"http://www.asap-project.org/bmla\" id=\"" + bmlId
                + "\"><speech id=\"s1\"><text/></speech></bml>";
    }

    @Before
    public void setup()
    {
        BMLTInfo.init();
        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(currentTimeMillis);

        parser = new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().add(
                BMLABMLBehaviorAttributes.class).build());
        stubEngine = new StubEngine("stubEngine");
        class StubSchedulingStrategy implements SchedulingStrategy
        {
            @Override
            public void schedule(BMLBlockComposition mechanism, BehaviourBlock bb, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler,
                    double time)
            {
                for (Behaviour b : bb.behaviours)
                {
                    stubEngine.addBehaviour(b.getBmlId(), b.id);
                }
            }
        }
        scheduler = new BMLScheduler("avatar1", parser, fbManager, stubClock, new BMLASchedulingHandler(new StubSchedulingStrategy(),
                pegBoard), bbManager, pegBoard);

        scheduler.addEngine(SpeechBehaviour.class, stubEngine);

        feedBackList = new ArrayList<BMLSyncPointProgressFeedback>();
        blockProgressFeedbackList = new ArrayList<BMLBlockProgressFeedback>();
        warningList = new ArrayList<BMLWarningFeedback>();
        listFeedbackListener = new ListBMLFeedbackListener.Builder().predictionList(predictionFeedback).feedBackList(feedBackList)
                .blockFeedbackList(blockProgressFeedbackList).warningList(warningList).build();
        scheduler.addFeedbackListener(listFeedbackListener);
    }

    private void parseBML(String str)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLABMLBehaviorAttributes());
        bb.readXML(str);
        parser.addBehaviourBlock(bb);
    }

    @Test
    public void testStartFeedback()
    {
        parseBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\"/>");
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        BMLABlockProgressFeedback fb = BMLABlockProgressFeedback.build(blockProgressFeedbackList.get(0));
        assertEquals(CTM, fb.getPosixTime(), PRECISION);
    }

    @Test
    public void testStartPredictionFeedbackEmptyBlock()
    {
        parseBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\"/>");
        scheduler.schedule();
        assertEquals(2, predictionFeedback.size()); // delivered, scheduling done, (no starting, since it finishes instantly)
        assertEquals("bml1", predictionFeedback.get(0).getBmlBlockPredictions().get(0).getId());
        assertEquals("bml1", predictionFeedback.get(1).getBmlBlockPredictions().get(0).getId());

        assertEquals(0, predictionFeedback.get(0).getBmlBlockPredictions().get(0).getGlobalStart(), PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, predictionFeedback.get(0).getBmlBlockPredictions().get(0).getGlobalEnd(), PRECISION);
        assertEquals(0, predictionFeedback.get(1).getBmlBlockPredictions().get(0).getGlobalStart(), PRECISION);

        assertEquals(0, predictionFeedback.get(1).getBmlBlockPredictions().get(0).getGlobalEnd(), PRECISION);

        BMLABlockPredictionFeedback fb0 = BMLABlockPredictionFeedback.build(predictionFeedback.get(0).getBmlBlockPredictions().get(0));
        BMLABlockPredictionFeedback fb1 = BMLABlockPredictionFeedback.build(predictionFeedback.get(1).getBmlBlockPredictions().get(0));
        assertEquals(CTM, fb0.getPosixStartTime(), PRECISION);
        assertEquals(CTM, fb1.getPosixStartTime(), PRECISION);
        assertEquals(0, fb0.getPosixEndTime(), PRECISION);
        assertEquals(CTM, fb1.getPosixEndTime(), PRECISION);
    }

    @Test
    public void testStartPredictionFeedback()
    {
        stubEngine.addBlockEnd("bml1", 5);
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();
        assertEquals(3, predictionFeedback.size()); // delivered, scheduling done, starting

        assertEquals(0, predictionFeedback.get(0).getBmlBlockPredictions().get(0).getGlobalStart(), PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, predictionFeedback.get(0).getBmlBlockPredictions().get(0).getGlobalEnd(), PRECISION);

        assertEquals(0, predictionFeedback.get(1).getBmlBlockPredictions().get(0).getGlobalStart(), PRECISION);
        assertEquals(0, predictionFeedback.get(2).getBmlBlockPredictions().get(0).getGlobalStart(), PRECISION);

        assertEquals(5, predictionFeedback.get(1).getBmlBlockPredictions().get(0).getGlobalEnd(), PRECISION);
        assertEquals(5, predictionFeedback.get(2).getBmlBlockPredictions().get(0).getGlobalEnd(), PRECISION);

        BMLABlockPredictionFeedback fb0 = BMLABlockPredictionFeedback.build(predictionFeedback.get(0).getBmlBlockPredictions().get(0));
        BMLABlockPredictionFeedback fb1 = BMLABlockPredictionFeedback.build(predictionFeedback.get(1).getBmlBlockPredictions().get(0));
        BMLABlockPredictionFeedback fb2 = BMLABlockPredictionFeedback.build(predictionFeedback.get(2).getBmlBlockPredictions().get(0));
        assertEquals(CTM, fb0.getPosixStartTime(), PRECISION);
        assertEquals(CTM, fb1.getPosixStartTime(), PRECISION);
        assertEquals(CTM, fb2.getPosixStartTime(), PRECISION);
        assertEquals(0, fb0.getPosixEndTime(), PRECISION);
        assertEquals(CTM + 5000, fb1.getPosixEndTime(), PRECISION);
        assertEquals(CTM + 5000, fb2.getPosixEndTime(), PRECISION);
    }

    @Test
    public void testEndFeedback()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();
        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
        BMLABlockProgressFeedback fb = BMLABlockProgressFeedback.build(blockProgressFeedbackList.get(1));
        assertEquals(CTM, fb.getPosixTime(), PRECISION);
    }

    @Test
    public void testAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
        assertThat(listFeedbackListener.getIndex(listFeedbackListener.getBlockProgress("bml2", "start")),
                greaterThan(listFeedbackListener.getIndex(listFeedbackListener.getBlockProgress("bml1", "start"))));
    }

    @Test
    public void testEmptyMerge()
    {
        parseBML(createEmptyBML("bml1", "composition=\"MERGE\""));
        scheduler.schedule();

        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
    }

    @Test
    public void testEmptyReplace()
    {
        parseBML(createEmptyBML("bml1", "composition=\"REPLACE\""));
        scheduler.schedule();

        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
    }

    @Test
    public void testEmptyAppend()
    {
        parseBML(createNonEmptyBML("bml1", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));

        scheduler.blockStopFeedback("bml1", BMLABlockStatus.DONE, 0);
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
    }

    @Test
    public void testAppend2()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();

        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));

        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
    }

    @Test
    public void testAppendAfter()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3"));
        parseBML(createNonEmptyBML("bml4", "composition=\"APPEND-AFTER(bml2,bml3)\""));
        scheduler.schedule();

        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
        assertEquals("bml3", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(2));

        scheduler.getBMLBlockManager().finishBlock("bml2", 0);
        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());

        scheduler.getBMLBlockManager().finishBlock("bml3", 0);
        assertEquals(4, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(2, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml4", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(3));
    }

    @Test
    public void testAppendAfterNonExisting()
    {
        // should ignore bml5, even if it is added later
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3"));
        parseBML(createNonEmptyBML("bml4", "composition=\"APPEND-AFTER(bml2,bml3,bml5)\""));
        parseBML(createNonEmptyBML("bml5"));
        scheduler.schedule();

        assertEquals(4, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
        assertEquals("bml3", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(2));

        scheduler.getBMLBlockManager().finishBlock("bml2", 0);
        assertEquals(4, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());

        scheduler.getBMLBlockManager().finishBlock("bml3", 0);
        assertEquals(5, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(2, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml4", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(4));
    }

    @Test
    public void testStartNonExisting()
    {
        scheduler.startBlock("bml1", 0);
        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testInteruptNonExisting()
    {
        scheduler.interruptBlock("bml1", 0);
        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(0, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testInterrupt()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        assertEquals(1, blockProgressFeedbackList.size());
        scheduler.interruptBlock("bml1", 0);

        BMLABlockProgressFeedback bbfb[] = constructBMLABlockProgress();
        assertEquals(2, bbfb.length);

        // bml 1 start
        assertEquals("bml1", bbfb[0].getBmlId());
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[0].getStatus());

        // bml1 interrupt
        assertEquals("bml1", bbfb[1].getBmlId());
        assertEquals(BMLABlockStatus.INTERRUPTED, bbfb[1].getStatus());
    }

    @Test
    public void testInterruptThroughBML()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        scheduler.schedule();
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createNonEmptyBML("bml3", "bmla:interrupt=\"bml1,bml2\""));
        scheduler.schedule();

        BMLABlockProgressFeedback bbfb[] = constructBMLABlockProgress();
        assertEquals(5, bbfb.length);

        // bml1 start
        assertEquals("bml1", bbfb[0].getBmlId());
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[0].getStatus());

        // bml2 start
        assertEquals("bml2", bbfb[1].getBmlId());
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[1].getStatus());

        // bml1 interrupt
        assertEquals("bml1", bbfb[2].getBmlId());
        assertEquals(BMLABlockStatus.INTERRUPTED, bbfb[2].getStatus());

        // bml2 interrupt
        assertEquals("bml2", bbfb[3].getBmlId());
        assertEquals(BMLABlockStatus.INTERRUPTED, bbfb[3].getStatus());

        // bml3 start
        assertEquals("bml3", bbfb[4].getBmlId());
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[4].getStatus());
    }

    @Test
    public void testInterruptAppender()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        scheduler.interruptBlock("bml2", 0);
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());

        scheduler.blockStopFeedback("bml1", BMLABlockStatus.DONE, 0);
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(2, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testAppendAfterInterrupted()
    {
        // interrupting bml1 should start bml2
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));

        scheduler.interruptBlock("bml1", 0);
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));

        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromEndFeedback(blockProgressFeedbackList).get(0));
    }

    @Test
    public void testPreplan()
    {
        parseBML(createNonEmptyBML("bml2", "bmla:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(0, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testEmptyPreplan()
    {
        parseBML(createEmptyBML("bml2", "bmla:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(0, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testStartEmptyPreplan()
    {
        parseBML(createEmptyBML("bml2", "bmla:preplan=\"true\""));
        scheduler.schedule();
        parseBML(createEmptyBML("bml3", "bmla:onStart=\"bml2\""));
        scheduler.schedule();
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(2, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testPreplanAndAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\" bmla:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));

        scheduler.blockStopFeedback("bml1", BMLABlockStatus.DONE, 0);
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testPreplanAndActivate()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createNonEmptyBML("bml2", "bmla:onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));
    }

    @Test
    public void testPreplanAndActivateAndAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\" bmla:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));

        parseBML(createNonEmptyBML("bml3", "bmla:onStart=\"bml2\""));
        scheduler.schedule();
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml3", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));

        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(2));
    }

    @Test
    public void testPreplanAndActivateTwice()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createNonEmptyBML("bml2", "bmla:onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createNonEmptyBML("bml3", "bmla:onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testReplaceAppended()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        // scheduler.blockStopFeedback("bml1"); // feedback: bml1 end
        scheduler.interruptBlock("bml1", 0);
        parseBML(createEmptyBML("bml3", "composition=\"REPLACE\"")); // end
                                                                     // feedback
                                                                     // for
                                                                     // behaviors
                                                                     // stopped
                                                                     // in this
                                                                     // way
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(blockProgressFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
        assertThat(getBMLIdsFromEndFeedback(blockProgressFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
    }

    @Test
    public void testReplaceAppended2()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        // scheduler.blockStopFeedback("bml1"); // feedback: bml1 end
        scheduler.interruptBlock("bml1", 0);

        parseBML(createNonEmptyBML("bml3", "composition=\"REPLACE\"")); // end
                                                                        // feedback
                                                                        // for
                                                                        // behaviors
                                                                        // stopped
                                                                        // in
                                                                        // this
                                                                        // way
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(blockProgressFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
        assertThat(getBMLIdsFromEndFeedback(blockProgressFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2"));
    }

    @Test
    public void testReplace()
    {
        parseBML(createNonEmptyBML("bml3", "composition=\"REPLACE\""));
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(blockProgressFeedbackList), IsIterableContainingInOrder.contains("bml3"));
        assertThat(getBMLIdsFromEndFeedback(blockProgressFeedbackList), hasSize(0));
    }

    @Test
    public void testPreplannedInterrupt()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmla:preplan=\"true\" bmla:interrupt=\"bml1\""));
        scheduler.schedule();

        assertEquals(1, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testPreplannedInterrupt2()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmla:preplan=\"true\" bmla:interrupt=\"bml1\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals(1, getBMLIdsFromEndFeedback(blockProgressFeedbackList).size());
        assertEquals(0, scheduler.getBehaviours("bml1").size());
    }

    @Test
    public void testOnStartPreplan()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmla:preplan=\"true\" bmla:onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testAppendPreplanAndActivate()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml3", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmla:preplan=\"true\" composition=\"APPEND-AFTER(bml1)\" bmla:onStart=\"bml3\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
    }

    @Test
    public void testAppendAfterPrePlanned()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND-AFTER(bml1)\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createEmptyBML("bml3", "bmla:onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml3", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));

        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(2));
    }

    @Test
    public void testAppendPrePlanned()
    {
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();

        assertEquals(0, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());

        parseBML(createEmptyBML("bml3", "bmla:onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(2, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml3", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(0));
        assertEquals("bml1", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(1));

        scheduler.getBMLBlockManager().finishBlock("bml1", 0);
        assertEquals(3, getBMLIdsFromStartFeedback(blockProgressFeedbackList).size());
        assertEquals("bml2", getBMLIdsFromStartFeedback(blockProgressFeedbackList).get(2));
    }

    @Test
    public void testReplace2()
    {
        pegBoard.addBMLBlockPeg(new BMLBlockPeg("bml1", 1));

        parseBML(createNonEmptyBML("bml2", "composition=\"REPLACE\""));
        scheduler.schedule();

        assertNull(pegBoard.getBMLBlockPeg("bml1"));
    }

    @Test
    public void testStartPredictionWithAppend()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 1);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND\""));
        scheduler.schedule();
        List<BMLBlockPredictionFeedback> blockStartPredictions = listFeedbackListener.getBlockStartOnlyPredictions();
        assertEquals(3, blockStartPredictions.size());
        assertEquals(2d, blockStartPredictions.get(2).getGlobalStart(), PRECISION);
        assertEquals(2, pegBoard.getBMLBlockPeg("bml3").getValue(), PRECISION);

        BMLABlockPredictionFeedback pred = BMLABlockPredictionFeedback.build(blockStartPredictions.get(2));
        assertEquals(2000d + CTM, pred.getPosixStartTime(), PRECISION);
    }

    @Test
    public void testStartPredictionWithAppendAfterPreplannedBlock()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 3);
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND\""));
        scheduler.schedule();
        List<BMLBlockPredictionFeedback> blockStartPredictions = listFeedbackListener.getBlockStartOnlyPredictions();
        assertEquals(3, blockStartPredictions.size());
        assertEquals(0d, blockStartPredictions.get(0).getGlobalStart(), PRECISION);
        assertEquals(2d, blockStartPredictions.get(1).getGlobalStart(), PRECISION);
        assertEquals(3d, blockStartPredictions.get(2).getGlobalStart(), PRECISION);
        assertEquals(3, pegBoard.getBMLBlockPeg("bml3").getValue(), PRECISION);
    }

    private BMLABlockProgressFeedback[] constructBMLABlockProgress()
    {
        BMLABlockProgressFeedback bbf[] = new BMLABlockProgressFeedback[blockProgressFeedbackList.size()];
        for (int i = 0; i < blockProgressFeedbackList.size(); i++)
        {
            bbf[i] = BMLABlockProgressFeedback.build(blockProgressFeedbackList.get(i));
        }
        return bbf;
    }

    private BMLABlockPredictionFeedback[] constructBMLABlockPredictions()
    {
        BMLABlockPredictionFeedback bbf[] = new BMLABlockPredictionFeedback[predictionFeedback.size()];
        for (int i = 0; i < predictionFeedback.size(); i++)
        {
            bbf[i] = BMLABlockPredictionFeedback.build(predictionFeedback.get(i).getBmlBlockPredictions().get(0));
        }
        return bbf;
    }

    @Test
    public void testStartPredictionWithBMLAAppendAfter()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 5);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "xmlns:bmla=\"" + BMLAInfo.BMLA_NAMESPACE + "\" bmla:appendAfter=\"bml1\""));
        scheduler.schedule();

        assertEquals(6, predictionFeedback.size());
        BMLABlockPredictionFeedback bbf[] = constructBMLABlockPredictions();

        // bml1 scheduling start
        assertEquals("bml1", bbf[0].getId());
        assertEquals(BMLABlockStatus.IN_PREP, bbf[0].getStatus());

        // bml1 scheduling end, PENDING
        assertEquals("bml1", bbf[1].getId());
        assertEquals(2, bbf[1].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[1].getStatus());

        // bml1 start
        assertEquals("bml1", bbf[2].getId());
        assertEquals(2, bbf[2].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[2].getStatus());

        // bml2 scheduling start
        assertEquals("bml2", bbf[3].getId());
        assertEquals(2, bbf[3].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[3].getStatus());

        // bml2 scheduling end, PENDING
        assertEquals("bml2", bbf[4].getId());
        assertEquals(2, bbf[4].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[4].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[4].getStatus());

        // bml2 scheduling end, LURKING
        assertEquals("bml2", bbf[5].getId());
        assertEquals(2, bbf[5].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[5].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[5].getStatus());
    }

    @Test
    public void testPredictionsWithAppendAfter()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 1);
        stubEngine.addBlockEnd("bml3", 5);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND-AFTER(bml2)\""));
        scheduler.schedule();

        assertEquals(9, predictionFeedback.size());
        BMLABlockPredictionFeedback bbf[] = constructBMLABlockPredictions();

        // bml1 scheduling start
        assertEquals("bml1", bbf[0].getId());
        assertEquals(BMLABlockStatus.IN_PREP, bbf[0].getStatus());

        // bml1 scheduling end, PENDING
        assertEquals("bml1", bbf[1].getId());
        assertEquals(2, bbf[1].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[1].getStatus());

        // bml1 start
        assertEquals("bml1", bbf[2].getId());
        assertEquals(2, bbf[2].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[2].getStatus());

        // bml2 scheduling start
        assertEquals("bml2", bbf[3].getId());
        assertEquals(0, bbf[3].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[3].getStatus());

        // bml2 scheduling end, PENDING
        assertEquals("bml2", bbf[4].getId());
        assertEquals(0, bbf[4].getGlobalStart(), PRECISION);
        assertEquals(1, bbf[4].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[4].getStatus());

        // bml2 scheduling end, IN_EXEC
        assertEquals("bml2", bbf[5].getId());
        assertEquals(0, bbf[5].getGlobalStart(), PRECISION);
        assertEquals(1, bbf[5].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[5].getStatus());

        // bml3 scheduling start
        assertEquals("bml3", bbf[6].getId());
        assertEquals(1, bbf[6].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[6].getStatus());

        // bml3 scheduling end, PENDING
        assertEquals("bml3", bbf[7].getId());
        assertEquals(1, bbf[7].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[7].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[7].getStatus());

        // bml3 scheduling end, LURKING
        assertEquals("bml3", bbf[8].getId());
        assertEquals(1, bbf[8].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[8].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[8].getStatus());
    }

    @Test
    public void testPredictionsWithAppendAndLateClock()
    {
        stubClock.setTime(3);
        stubEngine.addBlockEnd("bml1", 5);
        stubEngine.addBlockEnd("bml2", 4);
        stubEngine.addBlockEnd("bml3", 6);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND\""));
        scheduler.schedule();

        assertEquals(9, predictionFeedback.size());
        BMLABlockPredictionFeedback bbf[] = constructBMLABlockPredictions();

        // bml1 scheduling start
        assertEquals("bml1", bbf[0].getId());
        assertEquals(3, bbf[0].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[0].getStatus());

        // bml1 scheduling end, PENDING
        assertEquals("bml1", bbf[1].getId());
        assertEquals(3, bbf[1].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[1].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[1].getStatus());

        // bml1 start
        assertEquals("bml1", bbf[2].getId());
        assertEquals(3, bbf[2].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[2].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[2].getStatus());

        // bml2 scheduling start
        assertEquals("bml2", bbf[3].getId());
        assertEquals(3, bbf[3].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[3].getStatus());

        // bml2 scheduling end, PENDING
        assertEquals("bml2", bbf[4].getId());
        assertEquals(3, bbf[4].getGlobalStart(), PRECISION);
        assertEquals(4, bbf[4].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[4].getStatus());
        assertEquals(-3000 + 3000d + CTM, bbf[4].getPosixStartTime(), PRECISION);
        assertEquals(-3000 + 4000d + CTM, bbf[4].getPosixEndTime(), PRECISION);

        // bml2 scheduling end, IN_EXEC
        assertEquals("bml2", bbf[5].getId());
        assertEquals(3, bbf[5].getGlobalStart(), PRECISION);
        assertEquals(4, bbf[5].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[5].getStatus());

        // bml3 scheduling start
        assertEquals("bml3", bbf[6].getId());
        assertEquals(5, bbf[6].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[6].getStatus());

        // bml3 scheduling end, PENDING
        assertEquals("bml3", bbf[7].getId());
        assertEquals(5, bbf[7].getGlobalStart(), PRECISION);
        assertEquals(6, bbf[7].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[7].getStatus());

        // bml3 scheduling end, LURKING
        assertEquals("bml3", bbf[8].getId());
        assertEquals(5, bbf[8].getGlobalStart(), PRECISION);
        assertEquals(6, bbf[8].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[8].getStatus());

        assertEquals(5, pegBoard.getBMLBlockPeg("bml3").getValue(), PRECISION);
    }

    @Test
    public void testPredictionUpdatePrepend()
    {
        stubEngine.addBlockEnd("bml1", 5);
        stubEngine.addBlockEnd("bml2", 10);
        stubEngine.addBlockEnd("bml3", 20);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "xmlns:bmla=\"" + BMLAInfo.BMLA_NAMESPACE + "\" bmla:appendAfter=\"bml1\""));
        parseBML(createNonEmptyBML("bml3", "xmlns:bmla=\"" + BMLAInfo.BMLA_NAMESPACE + "\" bmla:prependBefore=\"bml2\""));
        scheduler.schedule();

        assertEquals(12, predictionFeedback.size());
        BMLABlockPredictionFeedback bbf[] = constructBMLABlockPredictions();

        // bml1 scheduling start
        assertEquals("bml1", bbf[0].getId());
        assertEquals(0, bbf[0].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[0].getStatus());

        // bml1 scheduling done
        assertEquals("bml1", bbf[1].getId());
        assertEquals(0, bbf[1].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[1].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[1].getStatus());

        // bml1 start
        assertEquals("bml1", bbf[2].getId());
        assertEquals(0, bbf[2].getGlobalStart(), PRECISION);
        assertEquals(5, bbf[2].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[2].getStatus());

        // bml2 scheduling start
        assertEquals("bml2", bbf[3].getId());
        assertEquals(5, bbf[3].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[3].getStatus());

        // bml2 scheduling done
        assertEquals("bml2", bbf[4].getId());
        assertEquals(5, bbf[4].getGlobalStart(), PRECISION);
        assertEquals(10, bbf[4].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.PENDING, bbf[4].getStatus());

        // bml2 lurking
        assertEquals("bml2", bbf[5].getId());
        assertEquals(5, bbf[5].getGlobalStart(), PRECISION);
        assertEquals(10, bbf[5].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[5].getStatus());

        // bml3 scheduling start
        assertEquals("bml3", bbf[6].getId());
        assertEquals(0, bbf[6].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, bbf[6].getStatus());

        // bml2 timing update
        assertEquals("bml2", bbf[7].getId());
        assertEquals(20, bbf[7].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[7].getStatus());

        // bml3 scheduling done
        assertEquals("bml3", bbf[8].getId());
        assertEquals(0, bbf[8].getGlobalStart(), PRECISION);
        assertEquals(20, bbf[8].getGlobalEnd(), PRECISION);

        // bml2 timing update
        assertEquals("bml2", bbf[9].getId());
        assertEquals(20, bbf[9].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[9].getStatus());

        // bml3 start
        assertEquals("bml3", bbf[10].getId());
        assertEquals(0, bbf[10].getGlobalStart(), PRECISION);
        assertEquals(20, bbf[10].getGlobalEnd(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbf[10].getStatus());

        // bml2 timing update
        assertEquals("bml2", bbf[11].getId());
        assertEquals(20, bbf[11].getGlobalStart(), PRECISION);
        assertEquals(BMLABlockStatus.LURKING, bbf[11].getStatus());
    }

    @Test
    public void testRevokePreplanned()
    {
        stubEngine.addBlockEnd("bml1", 5);
        parseBML(createNonEmptyBML("bml1", "bmla:preplan=\"true\""));
        parseBML(createEmptyBML("bml2", "bmla:interrupt=\"bml1\""));
        scheduler.schedule();

        assertEquals(3, blockProgressFeedbackList.size());
        BMLABlockProgressFeedback bbfb[] = constructBMLABlockProgress();

        // bml1 revoke
        assertEquals("bml1", bbfb[0].getBmlId());
        assertEquals(0, bbfb[0].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.REVOKED, bbfb[0].getStatus());

        // bml2 start
        assertEquals("bml2", bbfb[1].getBmlId());
        assertEquals(0, bbfb[1].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[1].getStatus());

        // bml2 finished
        assertEquals("bml2", bbfb[2].getBmlId());
        assertEquals(0, bbfb[2].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.DONE, bbfb[2].getStatus());
    }

    @Test
    public void testRevokeAppendAfter()
    {
        stubEngine.addBlockEnd("bml1", 5);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "bmla:appendAfter=\"bml1\""));
        parseBML(createEmptyBML("bml3", "bmla:interrupt=\"bml2\""));
        scheduler.schedule();

        assertEquals(4, blockProgressFeedbackList.size());
        BMLABlockProgressFeedback bbfb[] = constructBMLABlockProgress();

        // bml1 start
        assertEquals("bml1", bbfb[0].getBmlId());
        assertEquals(0, bbfb[0].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[0].getStatus());

        // bml2 revoke
        assertEquals("bml2", bbfb[1].getBmlId());
        assertEquals(0, bbfb[1].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.REVOKED, bbfb[1].getStatus());

        // bml3 start
        assertEquals("bml3", bbfb[2].getBmlId());
        assertEquals(0, bbfb[2].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, bbfb[2].getStatus());

        // bml3 finished
        assertEquals("bml3", bbfb[3].getBmlId());
        assertEquals(0, bbfb[3].getGlobalTime(), PRECISION);
        assertEquals(BMLABlockStatus.DONE, bbfb[3].getStatus());
    }

    @Test
    public void testEndPredictionEmptyBlock()
    {
        stubClock.setTime(3);
        stubEngine.addBlockEnd("bml1", 0);
        parseBML(createEmptyBML("bml1", ""));
        scheduler.schedule();
        List<BMLBlockPredictionFeedback> blockStartPredictions = listFeedbackListener.getBlockStartOnlyPredictions();
        List<BMLBlockPredictionFeedback> blockFinishedPredictions = listFeedbackListener.getBlockEndPredictions();
        assertEquals(3d, blockStartPredictions.get(0).getGlobalStart(), PRECISION);
        assertEquals(3d, blockFinishedPredictions.get(0).getGlobalStart(), PRECISION);
        assertEquals(3d, blockFinishedPredictions.get(0).getGlobalEnd(), PRECISION);
    }

    @Test
    public void testAddEngineForSameBehavior()
    {
        Engine e1 = new StubEngine("e1");
        Engine e2 = new StubEngine("e2");
        scheduler.addEngine(SpeechBehaviour.class, e1);
        scheduler.addEngine(SpeechBehaviour.class, e2);
        assertEquals(e2, scheduler.getEngine(SpeechBehaviour.class));
        assertThat(scheduler.getEngines(), IsIterableContainingInAnyOrder.containsInAnyOrder(stubEngine, e1, e2));
    }

    @Test
    public void testSameAddEngineForDifferentBehavior()
    {
        Engine e1 = new StubEngine("e1");
        scheduler.addEngine(SpeechBehaviour.class, e1);
        scheduler.addEngine(GestureBehaviour.class, e1);

        assertThat(scheduler.getEngines(), IsIterableContainingInAnyOrder.containsInAnyOrder(stubEngine, e1));
        assertEquals(2, scheduler.getEngines().size());
    }
}
