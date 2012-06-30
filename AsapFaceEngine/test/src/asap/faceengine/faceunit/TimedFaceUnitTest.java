package asap.faceengine.faceunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.bml.feedback.ListFeedbackListener;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.KeyPositionMocker;
import asap.testutil.bml.feedback.FeedbackAsserts;

/**
 * Test cases for the TimedFaceUnit
 * @author welberge
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedFaceUnitTest extends AbstractTimedPlanUnitTest
{
    private FaceUnit fuMock = mock(FaceUnit.class);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");

    public TimedFaceUnit createTimedFaceUnit(String behId, String bmlId, FaceUnit fu)
    {
        return new TimedFaceUnit(fbManager, BMLBlockPeg.GLOBALPEG, bmlId, behId, fu);
    }

    @Test
    public void testPrepState() throws TimedPlanUnitPlayException, MUPlayException
    {
        // state is IN_PREP, play shouldn't do anything
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        TimedFaceUnit tfu = createTimedFaceUnit("behaviour1", "bml1", fuMock);
        fbManager.addFeedbackListener(new ListFeedbackListener(fbList));

        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        KeyPositionMocker.stubKeyPositions(fuMock, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        tfu.setTimePeg("start", tp);
        tfu.play(1);
        assertTrue(fbList.isEmpty());
        verify(fuMock, never()).play(anyDouble());
    }

    @Test
    public void testPlay() throws TimedPlanUnitPlayException, MUPlayException
    {
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        TimedFaceUnit tfu = createTimedFaceUnit("behaviour1", "bml1", fuMock);
        fbManager.addFeedbackListener(new ListFeedbackListener(fbList));

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);

        KeyPositionMocker.stubKeyPositions(fuMock, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));

        tfu.setTimePeg("start", tpStart);
        tfu.setTimePeg("end", tpEnd);
        tfu.setState(TimedPlanUnitState.LURKING);
        tfu.start(0.5);
        tfu.play(0.5);
        assertEquals(1, fbList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 0.5, 0.5),
                fbList.get(0));
        verify(fuMock, times(1)).play(0.5);
    }

    @Override
    // no stroke peg
    public void testSetStrokePeg()
    {

    }

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedFaceUnit tfu = new TimedFaceUnit(bfm, bbPeg, bmlId, id, fuMock);
        KeyPositionMocker.stubKeyPositions(fuMock, new KeyPosition("start", 0, 1), new KeyPosition("attackPeak", 0, 1), new KeyPosition(
                "relax", 1, 1), new KeyPosition("end", 1, 1));
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tfu.setTimePeg("start", start);
        return tfu;
    }
}
