/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import hmi.tts.TTSException;
import hmi.tts.TimingInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.testutil.bml.feedback.FeedbackAsserts;

/**
 * Unit testcases for the DirectTTSUnit
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, TimingInfo.class })
public class DirectTTSUnitTest extends AbstractTimedPlanUnitTest
{
    protected TimedTTSUnit ttsUnit;
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private TTSBinding mockTTSBinding = mock(TTSBinding.class);
    private TimingInfo mockTimingInfo = mock(TimingInfo.class);

    protected TimedTTSUnit getTTSUnit(BMLBlockPeg bbPeg, String text, String id, String bmlId)
    {
        return new TimedDirectTTSUnit(fbManager, bbPeg, text, bmlId, id, mockTTSBinding, SpeechBehaviour.class);
    }

    @Override
    // XXX no stroke in this behavior
    public void testSetStrokePeg()
    {

    }

    @Override
    protected void assertSubsiding(TimedPlanUnit tpu)
    {
        assertEquals(TimedPlanUnitState.DONE, tpu.getState());
    }
    
    @Override //behavior does not subside
    public void testSubsiding()
    {
        
    }

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedDirectTTSUnit ttsUnit = new TimedDirectTTSUnit(bfm, bbPeg, "Hello world", bmlId, id, mockTTSBinding, SpeechBehaviour.class);
        try
        {
            when(mockTTSBinding.getTiming(SpeechBehaviour.class, "Hello world")).thenReturn(mockTimingInfo);
        }
        catch (TTSException e1)
        {
            throw new RuntimeException(e1);
        }
        when(mockTimingInfo.getDuration()).thenReturn(3d);
        try
        {
            ttsUnit.setup();
        }
        catch (SpeechUnitPlanningException e)
        {
            throw new RuntimeException(e);
        }

        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        ttsUnit.setTimePeg("start", start);
        return ttsUnit;
    }

    @Test
    public void testTTSUnit() throws TimedPlanUnitPlayException, SpeechUnitPlanningException, InterruptedException, TTSException
    {
        BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
        TimePeg tp = new TimePeg(bbPeg);
        tp.setGlobalValue(2);
        List<BMLSyncPointProgressFeedback> feedbackList = new ArrayList<BMLSyncPointProgressFeedback>();

        ttsUnit = getTTSUnit(bbPeg, "Hello world", "speech1", "bml1");
        when(mockTTSBinding.getTiming(SpeechBehaviour.class, "Hello world")).thenReturn(mockTimingInfo);
        when(mockTimingInfo.getDuration()).thenReturn(3d);

        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(feedbackList).build());
        ttsUnit.setStart(tp);
        ttsUnit.setup();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(2);
        assertTrue(TimedPlanUnitState.IN_EXEC == ttsUnit.getState());
        ttsUnit.play(2);
        Thread.sleep(100);

        assertEquals(1, feedbackList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","start",1.7,2),feedbackList.get(0));
        
        ttsUnit.play(6);
        Thread.sleep(100);        
        assertEquals(2, feedbackList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","speech1","end",5.7,6),feedbackList.get(1));        
    }
}
