package asap.realizer.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLPerformanceStartFeedback;
import saiba.bml.feedback.BMLPerformanceStopFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.ListFeedbackListener;
import saiba.bml.parser.BMLParser;

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

import asap.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import asap.bml.ext.bmlt.feedback.ListBMLTSchedulingListener;
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
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.PlanUnitParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.BMLTSchedulingHandler;
import asap.realizer.scheduler.SchedulingStrategy;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.utils.SchedulingClock;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for BMLScheduler
 * 
 * @author Herwin
 * 
 */
public class BMLSchedulerTest
{
    private StubEngine stubEngine;
    private PegBoard pegBoard = new PegBoard();
    private static final double PREDICTION_PRECISION = 0.0001;
    
    private static class StubSchedulingClock implements SchedulingClock
    {
        private double time = 0;

        public void setTime(double time)
        {
            this.time = time;
        }

        @Override
        public double getTime()
        {
            return time;
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
        public void interruptBehaviour(String behaviourId, String bmlId, double globalTime)
        {
            if (behMap.get(bmlId) != null)
            {
                behMap.get(bmlId).remove(behaviourId);
            }
        }

        @Override
        public void interruptBehaviourBlock(String bmlId, double globalTime)
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
    }

    private BMLScheduler scheduler;

    private BMLParser parser;

    private ListFeedbackListener listFeedbackListener;

    private List<BMLSyncPointProgressFeedback> feedBackList;

    private List<BMLPerformanceStartFeedback> startFeedbackList;

    private List<BMLPerformanceStopFeedback> endFeedbackList;

    private List<BMLTSchedulingStartFeedback> planningStartFeedback = new ArrayList<BMLTSchedulingStartFeedback>();

    private List<BMLTSchedulingFinishedFeedback> planningFinishedFeedback = new ArrayList<BMLTSchedulingFinishedFeedback>();

    private StubSchedulingClock stubSchedulingClock = new StubSchedulingClock();

    private BMLBlockManager bbManager = new BMLBlockManager();
    private FeedbackManager fbManager = new FeedbackManagerImpl(bbManager, "character1");

    private List<String> getBMLIdsFromStartFeedback(List<BMLPerformanceStartFeedback> fbList)
    {
        List<String> bmlIds = new ArrayList<String>();
        for (BMLPerformanceStartFeedback fb : fbList)
        {
            bmlIds.add(fb.bmlId);
        }
        return bmlIds;
    }

    private List<String> getBMLIdsFromEndFeedback(List<BMLPerformanceStopFeedback> fbList)
    {
        List<String> bmlIds = new ArrayList<String>();
        for (BMLPerformanceStopFeedback fb : fbList)
        {
            bmlIds.add(fb.bmlId);
        }
        return bmlIds;
    }

    private String createEmptyBML(String bmlId, String extraAttributes)
    {
        return "<bml xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"" + bmlId + "\" " + extraAttributes + "/>";
    }

    private String createNonEmptyBML(String bmlId, String extraAttributes)
    {

        return "<bml xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"" + bmlId + "\" " + extraAttributes
                + "><speech id=\"s1\"><text/></speech></bml>";
    }

    private String createNonEmptyBML(String bmlId)
    {

        return "<bml xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"" + bmlId + "\"><speech id=\"s1\"><text/></speech></bml>";
    }

    @Before
    public void setup()
    {
        parser = new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().add(
                BMLTBMLBehaviorAttributes.class).build());
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
        scheduler = new BMLScheduler("avatar1", parser, fbManager, stubSchedulingClock, new BMLTSchedulingHandler(
                new StubSchedulingStrategy()), bbManager, pegBoard);

        scheduler.addEngine(SpeechBehaviour.class, stubEngine);

        feedBackList = new ArrayList<BMLSyncPointProgressFeedback>();
        startFeedbackList = new ArrayList<BMLPerformanceStartFeedback>();
        endFeedbackList = new ArrayList<BMLPerformanceStopFeedback>();
        listFeedbackListener = new ListFeedbackListener(feedBackList, startFeedbackList, endFeedbackList);
        scheduler.addFeedbackListener(listFeedbackListener);

        scheduler.addPlanningListener(new ListBMLTSchedulingListener(planningStartFeedback, planningFinishedFeedback));
    }

    private void parseBML(String str)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLTBMLBehaviorAttributes());
        bb.readXML(str);
        parser.addBehaviourBlock(bb);
    }

    @Test
    public void testStart()
    {
        parseBML("<bml id=\"bml1\"/>");
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
    }

    @Test
    public void testAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
        scheduler.getBMLBlockManager().finishBlock("bml1");
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
        assertEquals(2, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(1).bmlId);

        assertThat(listFeedbackListener.getIndex(startFeedbackList.get(1)),
                greaterThan(listFeedbackListener.getIndex(endFeedbackList.get(0))));
    }

    @Test
    public void testEmptyMerge()
    {
        parseBML(createEmptyBML("bml1", "composition=\"MERGE\""));
        scheduler.schedule();

        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
    }

    @Test
    public void testEmptyReplace()
    {
        parseBML(createEmptyBML("bml1", "composition=\"REPLACE\""));
        scheduler.schedule();

        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
    }

    @Test
    public void testEmptyAppend()
    {
        parseBML(createNonEmptyBML("bml1", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);

        scheduler.blockStopFeedback("bml1");
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
    }

    @Test
    public void testAppend2()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();

        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml1");
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
        assertEquals(2, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(1).bmlId);
    }

    @Test
    public void testAppendAfter()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3"));
        parseBML(createNonEmptyBML("bml4", "composition=\"APPEND-AFTER(bml2,bml3)\""));
        scheduler.schedule();

        assertEquals(3, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
        assertEquals("bml2", startFeedbackList.get(1).bmlId);
        assertEquals("bml3", startFeedbackList.get(2).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml2");
        assertEquals(3, startFeedbackList.size());
        assertEquals(1, endFeedbackList.size());

        scheduler.getBMLBlockManager().finishBlock("bml3");
        assertEquals(4, startFeedbackList.size());
        assertEquals(2, endFeedbackList.size());
        assertEquals("bml4", startFeedbackList.get(3).bmlId);
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

        assertEquals(4, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);
        assertEquals("bml2", startFeedbackList.get(1).bmlId);
        assertEquals("bml3", startFeedbackList.get(2).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml2");
        assertEquals(4, startFeedbackList.size());
        assertEquals(1, endFeedbackList.size());

        scheduler.getBMLBlockManager().finishBlock("bml3");
        assertEquals(5, startFeedbackList.size());
        assertEquals(2, endFeedbackList.size());
        assertEquals("bml4", startFeedbackList.get(4).bmlId);
    }

    @Test
    public void testStartNonExisting()
    {
        scheduler.startBlock("bml1");
        assertEquals(0, startFeedbackList.size());
    }

    @Test
    public void testInteruptNonExisting()
    {
        scheduler.interruptBlock("bml1");
        assertEquals(0, startFeedbackList.size());
        assertEquals(0, endFeedbackList.size());
    }

    @Test
    public void testInterrupt()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());

        scheduler.interruptBlock("bml1");
        assertEquals(1, endFeedbackList.size());
    }

    @Test
    public void testInterruptThroughBML()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        scheduler.schedule();
        assertEquals(2, startFeedbackList.size());

        parseBML(createNonEmptyBML("bml3", "bmlt:interrupt=\"bml1,bml2\""));
        scheduler.schedule();
        assertEquals(3, startFeedbackList.size());

        assertEquals(2, endFeedbackList.size());
        if (endFeedbackList.get(0).bmlId.equals("bml1"))
        {
            assertEquals("bml2", endFeedbackList.get(1).bmlId);
        }
        else if (endFeedbackList.get(0).bmlId.equals("bml2"))
        {
            assertEquals("bml1", endFeedbackList.get(1).bmlId);
        }
        else
        {
            fail();
        }
    }

    @Test
    public void testInterruptAppender()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());

        scheduler.interruptBlock("bml2");
        assertEquals(0, endFeedbackList.size());

        scheduler.blockStopFeedback("bml1");
        assertEquals(1, startFeedbackList.size());
        assertEquals(1, endFeedbackList.size());
    }

    @Test
    public void testAppendAfterInterrupted()
    {
        // interrupting bml1 should start bml2
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);

        scheduler.interruptBlock("bml1");
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);

        assertEquals(2, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(1).bmlId);
        assertEquals(1, endFeedbackList.size());
        assertEquals("bml1", endFeedbackList.get(0).bmlId);
    }

    @Test
    public void testPreplan()
    {
        parseBML(createNonEmptyBML("bml2", "bmlt:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, startFeedbackList.size());
        assertEquals(0, endFeedbackList.size());
    }

    @Test
    public void testEmptyPreplan()
    {
        parseBML(createEmptyBML("bml2", "bmlt:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, startFeedbackList.size());
        assertEquals(0, endFeedbackList.size());
    }

    @Test
    public void testStartEmptyPreplan()
    {
        parseBML(createEmptyBML("bml2", "bmlt:preplan=\"true\""));
        scheduler.schedule();
        parseBML(createEmptyBML("bml3", "bmlt:onStart=\"bml2\""));
        scheduler.schedule();
        assertEquals(2, startFeedbackList.size());
        assertEquals(2, endFeedbackList.size());
    }

    @Test
    public void testPreplanAndAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\" bmlt:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);

        scheduler.blockStopFeedback("bml1");
        assertEquals(1, startFeedbackList.size());
    }

    @Test
    public void testPreplanAndActivate()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();
        assertEquals(0, startFeedbackList.size());

        parseBML(createNonEmptyBML("bml2", "bmlt:onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(2, startFeedbackList.size());

        assertEquals("bml2", startFeedbackList.get(0).bmlId);
        assertEquals("bml1", startFeedbackList.get(1).bmlId);
    }

    @Test
    public void testPreplanAndActivateAndAppend()
    {
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\" preplan=\"true\""));
        scheduler.schedule();
        assertEquals(1, startFeedbackList.size());
        assertEquals("bml1", startFeedbackList.get(0).bmlId);

        parseBML(createNonEmptyBML("bml3", "onStart=\"bml2\""));
        scheduler.schedule();
        assertEquals(2, startFeedbackList.size());
        assertEquals("bml3", startFeedbackList.get(1).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml1");
        assertEquals(3, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(2).bmlId);
    }

    @Test
    public void testPreplanAndActivateTwice()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());

        parseBML(createNonEmptyBML("bml2", "bmlt:onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(2, startFeedbackList.size());

        parseBML(createNonEmptyBML("bml3", "onStart=\"bml1\""));
        scheduler.schedule();
        assertEquals(3, startFeedbackList.size());
    }

    @Test
    public void testReplaceAppended()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        // scheduler.blockStopFeedback("bml1"); // feedback: bml1 end
        scheduler.interruptBlock("bml1");
        parseBML(createEmptyBML("bml3", "composition=\"REPLACE\"")); // end
                                                                     // feedback
                                                                     // for
                                                                     // behaviors
                                                                     // stopped
                                                                     // in this
                                                                     // way
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(startFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
        assertThat(getBMLIdsFromEndFeedback(endFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
    }

    @Test
    public void testReplaceAppended2()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();
        // scheduler.blockStopFeedback("bml1"); // feedback: bml1 end
        scheduler.interruptBlock("bml1");

        parseBML(createNonEmptyBML("bml3", "composition=\"REPLACE\"")); // end
                                                                        // feedback
                                                                        // for
                                                                        // behaviors
                                                                        // stopped
                                                                        // in
                                                                        // this
                                                                        // way
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(startFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
        assertThat(getBMLIdsFromEndFeedback(endFeedbackList), IsIterableContainingInOrder.contains("bml1", "bml2"));
    }

    @Test
    public void testReplace()
    {
        parseBML(createNonEmptyBML("bml3", "composition=\"REPLACE\""));
        scheduler.schedule();

        assertThat(getBMLIdsFromStartFeedback(startFeedbackList), IsIterableContainingInOrder.contains("bml3"));
        assertThat(getBMLIdsFromEndFeedback(endFeedbackList), hasSize(0));
    }

    @Test
    public void testPreplannedInterrupt()
    {
        parseBML(createNonEmptyBML("bml1"));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmlt:preplan=\"true\" bmlt:interrupt=\"bml1\""));
        scheduler.schedule();

        assertEquals(1, startFeedbackList.size());
        assertEquals(1, endFeedbackList.size());
    }

    @Test
    public void testPreplannedInterrupt2()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmlt:preplan=\"true\" bmlt:interrupt=\"bml1\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());
        assertEquals(0, endFeedbackList.size());
        assertEquals(0, scheduler.getBehaviours("bml1").size());
    }

    @Test
    public void testOnStartPreplan()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmlt:preplan=\"true\" onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());
    }

    @Test
    public void testAppendPreplanAndActivate()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml3", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "bmlt:preplan=\"true\" composition=\"APPEND-AFTER(bml1)\" onStart=\"bml3\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());
    }

    @Test
    public void testAppendAfterPrePlanned()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND-AFTER(bml1)\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());

        parseBML(createEmptyBML("bml3", "bmlt:onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(2, startFeedbackList.size());
        assertEquals("bml3", startFeedbackList.get(0).bmlId);
        assertEquals("bml1", startFeedbackList.get(1).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml1");
        assertEquals(3, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(2).bmlId);
    }

    @Test
    public void testAppendPrePlanned()
    {
        parseBML(createNonEmptyBML("bml1", "bmlt:preplan=\"true\""));
        scheduler.schedule();

        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        scheduler.schedule();

        assertEquals(0, startFeedbackList.size());

        parseBML(createEmptyBML("bml3", "bmlt:onStart=\"bml1\""));
        scheduler.schedule();

        assertEquals(2, startFeedbackList.size());
        assertEquals("bml3", startFeedbackList.get(0).bmlId);
        assertEquals("bml1", startFeedbackList.get(1).bmlId);

        scheduler.getBMLBlockManager().finishBlock("bml1");
        assertEquals(3, startFeedbackList.size());
        assertEquals("bml2", startFeedbackList.get(2).bmlId);
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
        assertEquals(3, planningStartFeedback.size());
        assertEquals(2d, planningStartFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(2, pegBoard.getBMLBlockPeg("bml3").getValue(), PREDICTION_PRECISION);
    }

    @Test
    public void testStartPredictionWithAppendAfterPreplannedBlock()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 3);
        parseBML(createNonEmptyBML("bml1", "preplan=\"true\""));
        parseBML(createNonEmptyBML("bml2", "composition=\"APPEND\""));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(3, planningStartFeedback.size());
        assertEquals(0d, planningStartFeedback.get(0).predictedStart, PREDICTION_PRECISION);
        assertEquals(2d, planningStartFeedback.get(1).predictedStart, PREDICTION_PRECISION);
        assertEquals(3d, planningStartFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(3, pegBoard.getBMLBlockPeg("bml3").getValue(), PREDICTION_PRECISION);
    }

    @Test
    public void testStartPredictionWithAppendAfter()
    {
        stubEngine.addBlockEnd("bml1", 2);
        stubEngine.addBlockEnd("bml2", 1);
        stubEngine.addBlockEnd("bml3", 5);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND-AFTER(bml2)\""));
        scheduler.schedule();
        assertEquals(3, planningStartFeedback.size());
        assertEquals(1d, planningStartFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(1d, planningFinishedFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(5d, planningFinishedFeedback.get(2).predictedEnd, PREDICTION_PRECISION);
        assertEquals(1, pegBoard.getBMLBlockPeg("bml3").getValue(), PREDICTION_PRECISION);
    }

    @Test
    public void testStartPredictionWithAppendAndLateClock()
    {
        stubSchedulingClock.setTime(3);
        stubEngine.addBlockEnd("bml1", 5);
        stubEngine.addBlockEnd("bml2", 4);
        stubEngine.addBlockEnd("bml3", 6);
        parseBML(createNonEmptyBML("bml1"));
        parseBML(createNonEmptyBML("bml2"));
        parseBML(createNonEmptyBML("bml3", "composition=\"APPEND\""));
        scheduler.schedule();
        assertEquals(3, planningStartFeedback.size());
        assertEquals(5d, planningStartFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(5d, planningFinishedFeedback.get(2).predictedStart, PREDICTION_PRECISION);
        assertEquals(6d, planningFinishedFeedback.get(2).predictedEnd, PREDICTION_PRECISION);

        assertEquals(5, pegBoard.getBMLBlockPeg("bml3").getValue(), PREDICTION_PRECISION);
    }

    @Test
    public void testEndPredictionEmptyBlock()
    {
        stubSchedulingClock.setTime(3);
        stubEngine.addBlockEnd("bml1", 0);
        parseBML(createEmptyBML("bml1", ""));
        scheduler.schedule();
        assertEquals(3d, planningStartFeedback.get(0).predictedStart, PREDICTION_PRECISION);
        assertEquals(3d, planningFinishedFeedback.get(0).predictedStart, PREDICTION_PRECISION);
        assertEquals(3d, planningFinishedFeedback.get(0).predictedEnd, PREDICTION_PRECISION);
    }
    
    @Test
    public void testAddEngineForSameBehavior()
    {
        Engine e1 = new StubEngine("e1");
        Engine e2 = new StubEngine("e2");        
        scheduler.addEngine(SpeechBehaviour.class, e1);
        scheduler.addEngine(SpeechBehaviour.class, e2);
        assertEquals(e2,scheduler.getEngine(SpeechBehaviour.class));
        assertThat(scheduler.getEngines(),IsIterableContainingInAnyOrder.containsInAnyOrder(stubEngine,e1,e2));
    }
    
    @Test
    public void testSameAddEngineForDifferentBehavior()
    {
        Engine e1 = new StubEngine("e1");
        scheduler.addEngine(SpeechBehaviour.class, e1);
        scheduler.addEngine(GestureBehaviour.class, e1);
        
        assertThat(scheduler.getEngines(),IsIterableContainingInAnyOrder.containsInAnyOrder(stubEngine,e1));
        assertEquals(2,scheduler.getEngines().size());
    }
}
