/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.TTSException;
import hmi.tts.TimingInfo;
import hmi.tts.Visime;
import hmi.tts.WordDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.util.FeedbackListUtils;
import asap.testutil.bml.feedback.FeedbackAsserts;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, BMLScheduler.class })
public class DirectTTSPlannerIntegrationTest extends TTSPlannerIntegrationTest
{
    private final TimedDirectTTSUnit ttsUnit = new TimedDirectTTSUnit(fbManager, bbPeg, SPEECHTEXT, BMLID, SPEECHID, mockTTSBinding,
            SpeechBehaviour.class);

    @Override
    protected void mockTTSUnitFactoryExpectations() throws TTSException
    {
        Phoneme p = new Phoneme(0, (int) (SPEECH_DURATION * 1000), false);
        List<Phoneme> ps = new ArrayList<Phoneme>();
        ps.add(p);
        WordDescription wd2 = new WordDescription("world", ps, new ArrayList<Visime>());
        final List<Bookmark> bms = new ArrayList<Bookmark>();
        final Bookmark bm = new Bookmark("s1", wd2, 500);
        bms.add(bm);
        final List<WordDescription> wds = new ArrayList<WordDescription>();
        wds.add(wd2);
        TimingInfo tInfo = new TimingInfo(wds, bms, new ArrayList<Visime>());

        when(
                mockTTSUnitFactory.createTimedTTSUnit(eq(bbPeg), anyString(), anyString(), eq(BMLID), eq(SPEECHID), eq(mockTTSBinding),
                        eq(SpeechBehaviour.class))).thenReturn(ttsUnit);
        when(mockTTSBinding.getTiming(SpeechBehaviour.class, SPEECHTEXT)).thenReturn(tInfo);
    }

    @Test
    public void testDirectTTSFeedback() throws BehaviourPlanningException, InterruptedException, IOException, TTSException
    {
        mockTTSUnitFactoryExpectations();

        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sp.setGlobalValue(1);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedTTSUnit pu = ttsPlanner.resolveSynchs(bbPeg, beh, sacs);
        pu.setState(TimedPlanUnitState.LURKING);
        ttsPlanner.addBehaviour(bbPeg, beh, sacs, pu);

        verbalPlayer.play(1);
        Thread.sleep(200);
        assertEquals(TimedPlanUnitState.IN_EXEC, pu.getState());
        assertEquals(0, exceptionList.size());

        assertThat(FeedbackListUtils.getSyncs(feedbackList), IsIterableContainingInOrder.contains("start"));
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","start",0.7,1), feedbackList.get(0));
        
        verbalPlayer.play(4);
        Thread.sleep(100);
        assertEquals(2, feedbackList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","end",3.7,4), feedbackList.get(1));        
    }

    @Test
    public void testDirectTTSFeedbackBlockManager() throws BehaviourPlanningException, InterruptedException, IOException, TTSException
    {
        mockTTSUnitFactoryExpectations();

        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sp.setGlobalValue(1);

        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        final PegBoard pegBoard = new PegBoard();

        TimedTTSUnit pu = ttsPlanner.resolveSynchs(bbPeg, beh, sacs);
        pu.setState(TimedPlanUnitState.LURKING);
        List<SyncAndTimePeg> satps = ttsPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        pegBoard.addTimePegs(satps);
        assertEquals(4d, planManager.getEndTime("bml1", "speech1"), 0.0001);

        assertEquals(1, pu.getStartTime(), 0.0001);
        assertEquals(4, pu.getEndTime(), 0.0001);

        verbalPlayer.play(1);
        Thread.sleep(500);
        assertTrue(pu.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(exceptionList.size() == 0);

        assertEquals(1, feedbackList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","start",0.7,1),feedbackList.get(0));        

        verbalPlayer.play(4);
        Thread.sleep(100);
        assertEquals(2, feedbackList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","end",3.7,4),feedbackList.get(1));
        
        assertTrue(pu.getState() == TimedPlanUnitState.DONE);
    }
}
