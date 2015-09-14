/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.FeedbackListUtils;
import asap.testutil.bml.feedback.FeedbackAsserts;

/**
 * Generic Unit test cases for classes implementing AbstractTTSUnit
 * @author welberge
 * 
 */
@Slf4j
public abstract class AbstractTTSUnitTest extends AbstractTimedPlanUnitTest
{
    private TimedTTSUnit ttsUnit;

    protected abstract TimedTTSUnit getTTSUnit(BMLBlockPeg bbPeg, String text, String id, String bmlId);

    private List<BMLSyncPointProgressFeedback> feedbackList;
    private final static double TTSUNIT_GLOBALSTART = 2;
    private final static double BMLBLOCKSTART = 0.3;
    private BMLBlockPeg bbPeg;
    private TimePeg startPeg;
    private static final double TIMING_PRECISION = 0.001;

    @Override //behavior does not subside
    public void testSubsiding()
    {
        
    }
    
    @Override
    @Test
    public void testSetStrokePeg() 
    {
        //XXX: remove from super?
    }
    
    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
    
    @Before
    public void setupTTSUnitTest() throws SpeechUnitPlanningException
    {
        bbPeg = new BMLBlockPeg("Peg1", BMLBLOCKSTART);
        startPeg = new TimePeg(bbPeg);
        startPeg.setGlobalValue(TTSUNIT_GLOBALSTART);
        feedbackList = newArrayList();
    }

    private void initTTSUnit(String text) throws SpeechUnitPlanningException
    {
        ttsUnit = getTTSUnit(bbPeg, text, "speech1", "bml1");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(feedbackList).build());
        ttsUnit.setStart(startPeg);
        ttsUnit.setup();
        log.debug("TTS Unit bookmarks: " + ttsUnit.getBookmarks());        
    }

    @Test
    public void testSetup2() throws SpeechUnitPlanningException
    {
        initTTSUnit("Hello <sync id=\"s1\"/>world");
        assertThat(ttsUnit.getBookmarks(), hasSize(1));
        assertEquals("s1", ttsUnit.getBookmarks().get(0).getName());
        assertEquals(2.0, ttsUnit.getStartTime(), TIMING_PRECISION);
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
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "speech1", "start", 2 - BMLBLOCKSTART, 2),
                feedbackList.get(0));

        ttsUnit.play(6);
        Thread.sleep(100);
        assertThat(feedbackList, hasSize(2));
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "speech1", "end", 6 - BMLBLOCKSTART, 6),
                feedbackList.get(1));
    }

    @Test
    public void testTTSUnitWithSync() throws TimedPlanUnitPlayException, SpeechUnitPlanningException, InterruptedException
    {
        initTTSUnit("Hello <sync id=\"s1\"/>world");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(2);
        assertEquals(TimedPlanUnitState.IN_EXEC, ttsUnit.getState());
        ttsUnit.play(2);        

        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "speech1", "start", 2 - BMLBLOCKSTART, 2),
                feedbackList.get(0));
        assertThat(FeedbackListUtils.getSyncs(feedbackList), hasItems("start"));

        ttsUnit.play(6);
        Thread.sleep(200);
        assertThat(FeedbackListUtils.getSyncs(feedbackList), IsIterableContainingInOrder.contains("start", "s1", "end"));

        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "speech1", "end", 6 - BMLBLOCKSTART, 6),
                feedbackList.get(2));
    }
    
    @Test
    public void testTTSUnitWithSyncAtSamePoint() throws TimedPlanUnitPlayException, SpeechUnitPlanningException, InterruptedException
    {
        initTTSUnit("Hello <sync id=\"s1\"/><sync id=\"s2\"/>world");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(2);        
        assertEquals(TimedPlanUnitState.IN_EXEC, ttsUnit.getState());
        ttsUnit.play(6);
        Thread.sleep(200);
        assertThat(FeedbackListUtils.getSyncs(feedbackList), IsIterableContainingInOrder.contains("start", "s1", "s2", "end"));        
    }
}
