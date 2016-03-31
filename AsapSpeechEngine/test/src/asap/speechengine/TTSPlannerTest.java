/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.TTSException;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.tts.WordDescription;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.parser.Constraint;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.util.TimePegUtil;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.util.TTSUnitStub;

import com.google.common.collect.ImmutableList;
/**
 * Unit test cases for SpeechBehaviour planning using a TTSPlanner
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, PlanManager.class })
public class TTSPlannerTest extends AbstractSpeechPlannerTest<TimedTTSUnit>
{
    protected TTSBinding mockTTSBinding = mock(TTSBinding.class);
    protected TimedTTSUnitFactory mockTTSUnitFactory = mock(TimedTTSUnitFactory.class);
    protected LipSynchProvider mockLipSyncher = mock(LipSynchProvider.class);

    public static final double SPEECH_DURATION = 3.0;
    public static final double SYNC1_OFFSET = 1.0;
    private static final double TIMING_PRECISION = 0.0001;

    private TTSTiming mockTiming = mock(TTSTiming.class);
    private final ImmutableList<Bookmark> BOOKMARKS = new ImmutableList.Builder<Bookmark>()
            .add(new Bookmark("s1", new WordDescription("world", new ArrayList<Phoneme>(), new ArrayList<Visime>()),
                    (int) (SYNC1_OFFSET * 1000))).build();

    final TTSUnitStub stubTTSUnit = new TTSUnitStub(mockFeedbackManager, bbPeg, SPEECHTEXT, SPEECHID, BMLID, mockTTSBinding,
            SpeechBehaviour.class, mockTiming);

    protected void mockTTSUnitFactoryExpectations() throws TTSException
    {
        when(
                mockTTSUnitFactory.createTimedTTSUnit((BMLBlockPeg) any(), anyString(), anyString(), eq(BMLID), eq(SPEECHID),
                        eq(mockTTSBinding), eq(SpeechBehaviour.class))).thenReturn(stubTTSUnit);
    }

    @Before
    @Override
    public void setup() throws TTSException
    {
        MockitoAnnotations.initMocks(this);
        when(mockTiming.getBookmarks()).thenReturn(BOOKMARKS);
        when(mockTiming.getDuration()).thenReturn(SPEECH_DURATION);
        mockTTSUnitFactoryExpectations();
        TTSPlanner ttsPlanner = new TTSPlanner(mockFeedbackManager, mockTTSUnitFactory, mockTTSBinding, planManager);
        speechPlanner = ttsPlanner;
        ttsPlanner.addLipSyncher(mockLipSyncher);
        super.setup();
    }

    @Test
    public void testResolveUnknownStartKnownEnd() throws BehaviourPlanningException, IOException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg endPeg = new TimePeg(bbPeg);
        endPeg.setGlobalValue(5);
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0, false));
        TimePeg s1Peg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("s1", s1Peg, new Constraint(), 0, false));
        TimePeg startPeg = new OffsetPeg(new TimePeg(bbPeg), 0, bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, true));

        TimedTTSUnit pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(5 - SPEECH_DURATION, startPeg.getGlobalValue(), TIMING_PRECISION);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());
        assertEquals(5, endPeg.getGlobalValue(), TIMING_PRECISION);

        speechPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(5 - SPEECH_DURATION, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(5, pu.getEndTime(), TIMING_PRECISION);
        assertEquals(endPeg, startPeg.getLink());
        assertEquals(5, endPeg.getGlobalValue(), TIMING_PRECISION);
        assertEquals(5 - SPEECH_DURATION, startPeg.getGlobalValue(), TIMING_PRECISION);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());
    }

    @Test
    public void testAddBehaviour() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        stubTTSUnit.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, 0));
        speechPlanner.addBehaviour(bbPeg, beh, sacs, stubTTSUnit);
        verify(mockLipSyncher, atLeast(1)).addLipSyncMovement(eq(bbPeg), eq(beh), (TimedPlanUnit) any(), (TTSTiming) any());
    }

    @Test
    public void testVoiceExtension() throws IOException, BehaviourPlanningException
    {
        BMLTInfo.init();
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, "xmlns:bmlt=\"" + BMLTBehaviour.BMLTNAMESPACE
                + "\" bmlt:voice=\"testvoice\"", SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(bbPeg, 0), new Constraint(), 0, true));        
        speechPlanner.addBehaviour(bbPeg, beh, sacs, null);
        verify(mockTTSBinding).setVoice("testvoice");
    }
}
