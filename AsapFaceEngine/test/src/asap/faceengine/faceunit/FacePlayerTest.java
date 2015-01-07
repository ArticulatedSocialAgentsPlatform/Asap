/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.DefaultPlayer;
import asap.realizer.Player;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.util.KeyPositionMocker;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit Test cases for the FacePlayer
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class,TimedFaceUnit.class})
public class FacePlayerTest
{
    private List<BMLWarningFeedback> beList;

    private Player facePlayer;

    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private final PegBoard pegBoard = new PegBoard();

    private FaceUnit mockFaceUnit = mock(FaceUnit.class);

    private PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();

    @Before
    public void setup()
    {
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(fbManager, planManager));
        beList = new ArrayList<BMLWarningFeedback>();
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(beList).build());
    }

    @Test
    public void testPlanUnitException() throws TimedPlanUnitPlayException
    {
        TimedFaceUnit tfu = new TimedFaceUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "id1", mockFaceUnit, pegBoard);
        KeyPositionMocker.stubKeyPositions(mockFaceUnit, new KeyPosition("start",0,1),new KeyPosition("end",1,1));
        tfu.setTimePeg("start", TimePegUtil.createTimePeg(0));
        tfu.setTimePeg("end", TimePegUtil.createTimePeg(1));
        tfu.setState(TimedPlanUnitState.LURKING);
        TimedFaceUnit spyTfu = PowerMockito.spy(tfu);
        doThrow(new TimedPlanUnitPlayException("", spyTfu)).when(spyTfu).play(anyDouble());        
        planManager.addPlanUnit(spyTfu);        
        
        assertEquals(1, planManager.getBehaviours("bml1").size());
        facePlayer.play(0);
        assertEquals(1, beList.size());
        assertEquals(0, planManager.getBehaviours("bml1").size());        
    }
}
