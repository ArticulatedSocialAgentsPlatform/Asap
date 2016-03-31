/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.TTSException;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.tts.WordDescription;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Planner;
import asap.realizer.Player;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.util.TTSUnitStub;

import com.google.common.collect.ImmutableList;

final class TestUtil
{
    private TestUtil()
    {
    }

    static void assertInRangeExclusive(double x, double min, double max)
    {
        assertTrue(x + "is not in range <" + min + "," + max + ">", x > min);
        assertTrue(x + "is not in range <" + min + "," + max + ">", x < max);
    }

    static void assertInRangeInclusive(double x, double min, double max)
    {
        assertTrue(x + "is not in range [" + min + "," + max + "]", x >= min);
        assertTrue(x + "is not in range [" + min + "," + max + "]", x <= max);
    }
}

@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TTSPlannerIntegrationTest
{
    protected TTSBinding mockTTSBinding = mock(TTSBinding.class);

    protected TimedTTSUnitFactory mockTTSUnitFactory = mock(TimedTTSUnitFactory.class);

    protected BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);

    protected FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");

    protected PlanManager<TimedTTSUnit> planManager = new PlanManager<TimedTTSUnit>();

    public static final String SPEECHID = "speech1";

    public static final String BMLID = "bml1";

    public static final String SPEECHTEXT = "Hello<sync id=\"s1\"/> world";

    public static final double SPEECH_DURATION = 3.0;

    public static final double SYNC1_OFFSET = 1.0;

    protected BMLBlockPeg bbPeg = new BMLBlockPeg(BMLID, 0.3);

    protected Engine ttsEngine;

    protected Planner<TimedTTSUnit> ttsPlanner;
    protected Player verbalPlayer;

    protected List<BMLWarningFeedback> exceptionList;

    protected List<BMLSyncPointProgressFeedback> feedbackList;

    private final ImmutableList<Bookmark> BOOKMARKS = new ImmutableList.Builder<Bookmark>()
            .add(new Bookmark("s1", new WordDescription("world", new ArrayList<Phoneme>(), new ArrayList<Visime>()),
                    (int) (SYNC1_OFFSET * 1000))).build();
    
    private TTSTiming mockTiming = mock(TTSTiming.class);
    
    private final TTSUnitStub ttsUnit = new TTSUnitStub(fbManager, bbPeg, SPEECHTEXT, SPEECHID, BMLID, mockTTSBinding,
            SpeechBehaviour.class, mockTiming);

    private final TimedTTSUnit ttsUnitPlayExeception = new TTSUnitPlayExceptionStub(fbManager, bbPeg, SPEECHTEXT, SPEECHID, BMLID,
            mockTTSBinding, SpeechBehaviour.class, mockTiming);

    protected static class TTSUnitPlayExceptionStub extends TTSUnitStub
    {
        public TTSUnitPlayExceptionStub(FeedbackManager bbm, BMLBlockPeg bbPeg, String text, String id, String bmlId, TTSBinding ttsBin,
                Class<? extends Behaviour> behClass, TTSTiming timing)
        {
            super(bbm, bbPeg, text, id, bmlId, ttsBin, behClass, timing);
        }

        @Override
        protected void playUnit(double time) throws TimedPlanUnitPlayException
        {
            throw new TimedPlanUnitPlayException("Play failed!", this);
        }
    }

    protected void mockTTSUnitFactoryExpectations() throws TTSException
    {
        when(
                mockTTSUnitFactory.createTimedTTSUnit(eq(bbPeg), anyString(), anyString(), eq(BMLID), eq(SPEECHID), eq(mockTTSBinding),
                        eq(SpeechBehaviour.class))).thenReturn(ttsUnit);
    }

    protected void mockTTSUnitFactoryExpectationsTTSUnitPlayException()
    {
        when(
                mockTTSUnitFactory.createTimedTTSUnit(eq(bbPeg), anyString(), anyString(), eq(BMLID), eq(SPEECHID), eq(mockTTSBinding),
                        eq(SpeechBehaviour.class))).thenReturn(ttsUnitPlayExeception);
    }

    @Before
    public void setUp()
    {
        when(mockTiming.getBookmarks()).thenReturn(BOOKMARKS);
        when(mockTiming.getDuration()).thenReturn(SPEECH_DURATION);
        verbalPlayer = new DefaultPlayer(new MultiThreadedPlanPlayer<TimedTTSUnit>(fbManager, planManager));
        ttsPlanner = new TTSPlanner(fbManager, mockTTSUnitFactory, mockTTSBinding, planManager);
        ttsEngine = new DefaultEngine<TimedTTSUnit>(ttsPlanner, verbalPlayer, planManager);
        exceptionList = Collections.synchronizedList(new ArrayList<BMLWarningFeedback>());

        feedbackList = Collections.synchronizedList(new ArrayList<BMLSyncPointProgressFeedback>());
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(exceptionList).feedBackList(feedbackList).build());        
    }

    @After
    public void tearDown() throws InterruptedException
    {
        verbalPlayer.shutdown();
    }

    protected SpeechBehaviour createSpeechBehaviour(String speechBML, String bmlId) throws IOException
    {
        return new SpeechBehaviour(bmlId, new XMLTokenizer(speechBML));
    }

    protected SpeechBehaviour createSpeechBehaviour(String id, String bmlId, String speech) throws IOException
    {
        return createSpeechBehaviour(String.format("<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"%s\"><text>%s</text></speech>", id, speech), bmlId);
    }

    @Test
    public void testResolveUnknownStartAndEnd() throws BehaviourPlanningException, IOException, TTSException
    {
        mockTTSUnitFactoryExpectations();

        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg endPeg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0, false));
        TimePeg s1Peg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("s1", s1Peg, new Constraint(), 0, false));
        TimePeg startPeg = new OffsetPeg(new TimePeg(bbPeg), 0, bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, true));

        TimedTTSUnit pu = ttsPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(0.3, startPeg.getGlobalValue(), 0.0001);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());

        ttsPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), 0.0001);
        assertEquals(3.3, pu.getEndTime(), 0.0001);
        assertEquals(endPeg, startPeg.getLink());
        assertEquals(3.3, endPeg.getGlobalValue(), 0.0001);
        assertEquals(0.3, startPeg.getGlobalValue(), 0.0001);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());
    }

    @Test
    public void testWarning() throws BehaviourPlanningException, InterruptedException, IOException
    {
        mockTTSUnitFactoryExpectationsTTSUnitPlayException();

        // checks if TTSUnit failure properly appears as BMLWarningFeedback
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sp.setGlobalValue(1);

        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedTTSUnit pu = ttsPlanner.resolveSynchs(bbPeg, beh, sacs);
        pu.setState(TimedPlanUnitState.LURKING);
        ttsPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(1, pu.getStartTime(), 0.0001);
        assertEquals(4, pu.getEndTime(), 0.0001);

        verbalPlayer.play(0);
        assertEquals(0, exceptionList.size());

        verbalPlayer.play(2);
        Thread.sleep(500);
        assertEquals(1, exceptionList.size());
    }

    public void testRemoveBehaviour() throws BehaviourPlanningException, IOException, TTSException
    {
        mockTTSUnitFactoryExpectations();

        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedTTSUnit pu = ttsPlanner.resolveSynchs(bbPeg, beh, sacs);
        ttsPlanner.addBehaviour(bbPeg, beh, sacs, pu);

        ttsEngine.stopBehaviour(BMLID, SPEECHID, 0);

        assertEquals(0, ttsEngine.getBehaviours(BMLID).size());
    }
}
