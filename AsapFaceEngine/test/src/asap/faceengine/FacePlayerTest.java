package asap.faceengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.elckerlyc.Player;
import hmi.elckerlyc.DefaultPlayer;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.bml.feedback.ListBMLExceptionListener;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;

/**
 * Unit Test cases for the FacePlayer
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class FacePlayerTest
{
    private List<BMLExceptionFeedback> beList;

    private Player facePlayer;

    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");


    private FaceUnit mockFaceUnit = mock(FaceUnit.class);

    private PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();

    @Before
    public void setup()
    {
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(fbManager, planManager));
        beList = new ArrayList<BMLExceptionFeedback>();
        fbManager.addExceptionListener(new ListBMLExceptionListener(beList));
    }

    @Test
    public void testPlanUnitException() throws TimedPlanUnitPlayException
    {
        TimedFaceUnit tfu = new TimedFaceUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "id1", mockFaceUnit);
        KeyPositionMocker.stubKeyPositions(mockFaceUnit, new KeyPosition("start",0,1),new KeyPosition("end",1,1));
        tfu.setTimePeg("start", TimePegUtil.createTimePeg(0));
        tfu.setTimePeg("end", TimePegUtil.createTimePeg(1));
        tfu.setState(TimedPlanUnitState.LURKING);
        TimedFaceUnit spyTfu = spy(tfu);
        doThrow(new TimedPlanUnitPlayException("", spyTfu)).when(spyTfu).playUnit(anyDouble());        
        planManager.addPlanUnit(spyTfu);        
        
        assertEquals(1, planManager.getBehaviours("bml1").size());
        facePlayer.play(0);
        assertEquals(1, beList.size());
        assertEquals(0, planManager.getBehaviours("bml1").size());        
    }
}
