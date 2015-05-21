/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

/**
 * Unit test cases for the TextSpeechUnit
 * @author welberge
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class SpeechTextUnitTest extends AbstractTimedPlanUnitTest
{
    private TextOutput mockOutput = mock(TextOutput.class);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");

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
        TimedSpeechTextUnit textUnit = new TimedSpeechTextUnit(bfm, bbPeg, "Hello world", bmlId, id, mockOutput);
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        textUnit.setStart(start);
        return textUnit;
    }

    @Test
    public void testSpeak() throws TimedPlanUnitPlayException
    {
        TimedSpeechTextUnit textUnit = new TimedSpeechTextUnit(fbManager, BMLBlockPeg.GLOBALPEG, "Hello world", "bml1", "speech1",
                mockOutput);
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        ListBMLFeedbackListener fbl = new ListBMLFeedbackListener.Builder().feedBackList(fbList).build();
        fbManager.addFeedbackListener(fbl);

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        textUnit.setStart(tpStart);
        assertEquals(textUnit.getEndTime(), textUnit.getStartTime() + textUnit.getPreferedDuration(), 0.0001);
        assertTrue(textUnit.getPreferedDuration() > 0);
        textUnit.setState(TimedPlanUnitState.LURKING);

        textUnit.start(0);
        assertTrue(textUnit.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 1);
        textUnit.play(10);
        assertTrue(textUnit.getState() == TimedPlanUnitState.DONE);
        assertTrue(fbList.size() == 2);
        verify(mockOutput, times(1)).setText(anyString());
    }

    @Test
    public void testSpeakWithSync() throws TimedPlanUnitPlayException
    {
        TimedSpeechTextUnit textUnit = new TimedSpeechTextUnit(fbManager, BMLBlockPeg.GLOBALPEG, "Hello<sync id=\"s1\"/> world", "bml1",
                "speech1", mockOutput);
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        ListBMLFeedbackListener fbl = new ListBMLFeedbackListener.Builder().feedBackList(fbList).build();
        fbManager.addFeedbackListener(fbl);

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        textUnit.setStart(tpStart);
        assertEquals(textUnit.getEndTime(), textUnit.getStartTime() + textUnit.getPreferedDuration(), 0.0001);
        assertTrue(textUnit.getPreferedDuration() > 0);
        textUnit.setState(TimedPlanUnitState.LURKING);

        textUnit.start(0);
        assertTrue(textUnit.getState() == TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size() == 1);
        textUnit.play(10);
        assertTrue(textUnit.getState() == TimedPlanUnitState.DONE);
        assertTrue(fbList.size() == 3);
        verify(mockOutput, times(1)).setText(anyString());
    }

    @Test
    @Override
    public void testSetStrokePeg()
    {
        // XXX: remove from super, use some exception when setting unsupported timepegs?
    }
}
