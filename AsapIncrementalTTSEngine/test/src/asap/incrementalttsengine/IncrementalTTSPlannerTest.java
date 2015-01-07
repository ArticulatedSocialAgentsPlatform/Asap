/*******************************************************************************
 *******************************************************************************/
package asap.incrementalttsengine;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.util.SystemClock;
import hmi.xml.XMLTokenizer;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.parser.BMLParser;
import saiba.bml.parser.Constraint;
import asap.incrementalspeechengine.IncrementalTTSPlanner;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.incrementalspeechengine.PhraseIUManager;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLASchedulingHandler;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;

/**
 * unit tests for the IncrementalTTSPlanner
 * @author hvanwelbergen
 * 
 */
public class IncrementalTTSPlannerTest
{
    private PlannerTests<IncrementalTTSUnit> plannerTests;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private static final String BMLID = "bml1";
    private IncrementalTTSPlanner incTTSPlanner;
    private static DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
    private BMLBlockPeg bbPeg = new BMLBlockPeg(BMLID, 0.3);
    private static final double TIME_PRECISION = 0.0001;
    private PegBoard pegBoard = new PegBoard();
    
    @AfterClass
    public static void oneTimeCleanup() throws IOException
    {
        dispatcher.close();
    }

    private SpeechBehaviour createSpeechBehaviour() throws IOException
    {
        return new SpeechBehaviour(
                BMLID,
                new XMLTokenizer(
                        "<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"speech1\"><text>Hello <sync id=\"s1\"/> world.</text></speech>"));
    }

    @Before
    public void setup()
    {
        SystemClock clock = new SystemClock();
        clock.start();        
        BMLScheduler bmlScheduler = new BMLScheduler("id1", new BMLParser(), NullFeedbackManager.getInstance(),clock ,
                new BMLASchedulingHandler(new SortedSmartBodySchedulingStrategy(pegBoard), pegBoard), new BMLBlockManager(), pegBoard);
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
        incTTSPlanner = new IncrementalTTSPlanner(mockBmlFeedbackManager, new PlanManager<IncrementalTTSUnit>(),
                new PhraseIUManager(dispatcher,null,bmlScheduler), new NullPhonemeToVisemeMapping(), new HashSet<IncrementalLipSynchProvider>());
        plannerTests = new PlannerTests<IncrementalTTSUnit>(incTTSPlanner, bbPeg);
    }

    @Test
    public void testResolveUnsetStart() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveUnsetStart(createSpeechBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createSpeechBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createSpeechBehaviour());
    }

    @Test
    public void testResolveUnknownStartAndEnd() throws BehaviourPlanningException, IOException
    {
        SpeechBehaviour beh = createSpeechBehaviour();

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg endPeg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0, false));
        TimePeg s1Peg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("s1", s1Peg, new Constraint(), 0, false));
        TimePeg startPeg = new OffsetPeg(new TimePeg(bbPeg), 0, bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, true));

        IncrementalTTSUnit pu = incTTSPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(0.3, startPeg.getGlobalValue(), TIME_PRECISION);

        incTTSPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIME_PRECISION);
        assertThat(pu.getEndTime(), greaterThan(pu.getStartTime()));
        assertThat(s1Peg.getGlobalValue(), greaterThan(pu.getStartTime()));
        assertThat(s1Peg.getGlobalValue(), lessThan(pu.getEndTime()));
        assertThat(endPeg, not(startPeg.getLink()));
    }

    @Test
    public void testSatp() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        IncrementalTTSUnit pu = incTTSPlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> satp = incTTSPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals("start", satp.get(0).sync);
        assertEquals("s1", satp.get(1).sync);
        assertEquals("relax", satp.get(2).sync);
        assertEquals("end", satp.get(3).sync);
    }
}
