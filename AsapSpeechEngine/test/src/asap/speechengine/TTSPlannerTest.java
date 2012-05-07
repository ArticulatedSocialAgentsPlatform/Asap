package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import hmi.bml.core.SpeechBehaviour;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.lipsync.LipSynchProvider;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.Visime;
import hmi.tts.WordDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.speechengine.TTSPlanner;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.TimedTTSUnitFactory;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.util.TTSUnitStub;

import com.google.common.collect.ImmutableList;

/**
 * Unit test cases for SpeechBehaviour planning using a TTSPlanner 
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class,PlanManager.class})
public class TTSPlannerTest extends AbstractSpeechPlannerTest<TimedTTSUnit>
{
    protected TTSBinding mockTTSBinding = mock(TTSBinding.class);
    protected TimedTTSUnitFactory mockTTSUnitFactory = mock(TimedTTSUnitFactory.class);
    protected LipSynchProvider mockLipSyncher = mock(LipSynchProvider.class);
    
    public static final double SPEECH_DURATION = 3.0;
    public static final double SYNC1_OFFSET = 1.0;
    private final ImmutableList<Bookmark> BOOKMARKS = new ImmutableList.Builder<Bookmark>()
            .add(new Bookmark("s1", new WordDescription("world", new ArrayList<Phoneme>(), new ArrayList<Visime>()),
                    (int) (SYNC1_OFFSET * 1000))).build();

    final TTSUnitStub stubTTSUnit = new TTSUnitStub(mockFeedbackManager, bbPeg, SPEECHTEXT, SPEECHID, BMLID, mockTTSBinding,
            SpeechBehaviour.class, SPEECH_DURATION, BOOKMARKS);
    
    protected void mockTTSUnitFactoryExpectations()
    {
        when(mockTTSUnitFactory.createTimedTTSUnit(                  
                (BMLBlockPeg)any(), 
                anyString(),
                anyString(),
                eq(BMLID), 
                eq(SPEECHID),
                eq(mockTTSBinding), 
                eq(SpeechBehaviour.class))).thenReturn(stubTTSUnit);        
    }

    @Before
    @Override
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
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
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0,
                false));
        TimePeg s1Peg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("s1", s1Peg, new Constraint(), 0, false));
        TimePeg startPeg = new OffsetPeg(new TimePeg(bbPeg), 0, bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, true));

        TimedTTSUnit pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(5 - SPEECH_DURATION, startPeg.getGlobalValue(), 0.0001);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());
        assertEquals(5, endPeg.getGlobalValue(), 0.0001);

        speechPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(5 - SPEECH_DURATION, pu.getStartTime(), 0.0001);
        assertEquals(5, pu.getEndTime(), 0.0001);
        assertEquals(endPeg, startPeg.getLink());
        assertEquals(5, endPeg.getGlobalValue(), 0.0001);
        assertEquals(5 - SPEECH_DURATION, startPeg.getGlobalValue(), 0.0001);
        TestUtil.assertInRangeExclusive(s1Peg.getGlobalValue(), startPeg.getGlobalValue(), endPeg.getGlobalValue());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAddBehaviour() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        stubTTSUnit.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg,0));
        speechPlanner.addBehaviour(bbPeg, beh, sacs, stubTTSUnit);
        verify(mockLipSyncher,atLeast(1)).addLipSyncMovement(eq(bbPeg), eq(beh), (TimedPlanUnit)any(), (List<Visime>)any());        
    }
}
