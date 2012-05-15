package asap.speechengine;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.ListFeedbackListener;
import asap.realizertestutil.util.FeedbackListUtils;

import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.speechengine.SpeechUnitPlanningException;
import asap.speechengine.TimedTTSUnit;

/**
 * Generic Unit test cases for classes implementing AbstractTTSUnit
 * @author welberge
 * 
 */
public abstract class AbstractTTSUnitTest
{
    private TimedTTSUnit ttsUnit;

    protected abstract TimedTTSUnit getTTSUnit(BMLBlockPeg bbPeg, String text, String id, String bmlId);

    private List<BMLSyncPointProgressFeedback> feedbackList;
    private final static double TTSUNIT_GLOBALSTART = 2;
    private final static double BMLBLOCKSTART = 0.3;
    private BMLBlockPeg bbPeg;
    private TimePeg startPeg;
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    protected FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTTSUnitTest.class.getName());
    private static final double TIMING_PRECISION = 0.001;
    
    public void setup() throws SpeechUnitPlanningException
    {
        bbPeg = new BMLBlockPeg("Peg1", BMLBLOCKSTART);
        startPeg = new TimePeg(bbPeg);
        startPeg.setGlobalValue(TTSUNIT_GLOBALSTART);
        feedbackList = newArrayList();
    }

    private void initTTSUnit(String text) throws SpeechUnitPlanningException
    {
        ttsUnit = getTTSUnit(bbPeg, text, "speech1", "bml1");
        LOGGER.debug("TTS Unit bookmarks: " + ttsUnit.getBookmarks());
        fbManager.addFeedbackListener(new ListFeedbackListener(feedbackList));
        ttsUnit.setStart(startPeg);
        ttsUnit.setup();
    }

    @Test
    public void testSetup() throws SpeechUnitPlanningException
    {
        initTTSUnit("Hello <sync id=\"s1\"/>world");
        assertThat(ttsUnit.getBookmarks(), hasSize(1));
        assertEquals("s1", ttsUnit.getBookmarks().get(0).getName());
        assertEquals(2.0, ttsUnit.getStartTime(),TIMING_PRECISION);
        assertThat(ttsUnit.getBookmarks().get(0).getOffset(), greaterThan(0));
    }

    @Test
    public void testTTSUnit() throws TimedPlanUnitPlayException, SpeechUnitPlanningException, InterruptedException
    {
        initTTSUnit("Hello world");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(2);
        assertTrue(TimedPlanUnitState.IN_EXEC == ttsUnit.getState());

        ttsUnit.play(2);
        Thread.sleep(200);

        assertThat(feedbackList, hasSize(1));
        assertEquals("bml1", feedbackList.get(0).bmlId);
        assertEquals("speech1", feedbackList.get(0).behaviorId);
        assertEquals(2 - BMLBLOCKSTART, feedbackList.get(0).bmlBlockTime, TIMING_PRECISION);
        assertEquals(2, feedbackList.get(0).timeStamp, 0.0001);
        assertEquals("start", feedbackList.get(0).syncId);

        ttsUnit.play(6);
        Thread.sleep(100);
        assertThat(feedbackList, hasSize(2));
        assertEquals("bml1", feedbackList.get(1).bmlId);
        assertEquals("speech1", feedbackList.get(1).behaviorId);
        assertEquals(6 - BMLBLOCKSTART, feedbackList.get(1).bmlBlockTime, TIMING_PRECISION);
        assertEquals(6, feedbackList.get(1).timeStamp, TIMING_PRECISION);
        assertEquals("end", feedbackList.get(1).syncId);
    }

    @Test
    public void testTTSUnitWithSync() throws TimedPlanUnitPlayException, SpeechUnitPlanningException, InterruptedException
    {
        initTTSUnit("Hello <sync id=\"s1\"/>world");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(2);
        assertEquals(TimedPlanUnitState.IN_EXEC, ttsUnit.getState());
        ttsUnit.play(2);
        Thread.sleep(200);

        assertEquals("bml1", feedbackList.get(0).bmlId);
        assertEquals("speech1", feedbackList.get(0).behaviorId);
        assertEquals(2 - BMLBLOCKSTART, feedbackList.get(0).bmlBlockTime, TIMING_PRECISION);
        assertEquals(2, feedbackList.get(0).timeStamp,TIMING_PRECISION);

        assertThat(FeedbackListUtils.getSyncs(feedbackList), hasItems("start"));

        ttsUnit.play(6);
        Thread.sleep(200);
        assertThat(FeedbackListUtils.getSyncs(feedbackList), IsIterableContainingInOrder.contains("start", "s1", "end"));

        assertEquals("bml1", feedbackList.get(2).bmlId);
        assertEquals("speech1", feedbackList.get(2).behaviorId);
        assertEquals(6 - BMLBLOCKSTART, feedbackList.get(2).bmlBlockTime, TIMING_PRECISION);
        assertEquals(6, feedbackList.get(2).timeStamp, 0.0001);

    }
}
