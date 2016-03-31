/*******************************************************************************
 *******************************************************************************/
package asap.faceengine;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.util.KeyPositionMocker;
import asap.testutil.bml.feedback.FeedbackAsserts;

/**
 * Unit test cases for the FacePlanPlayer
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class FacePlanPlayerTest
{
    private FaceUnit fuMock1 = mock(FaceUnit.class);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();
    private final PegBoard pegBoard = new PegBoard();
    @Test
    public void testPlayTfu() throws MUPlayException 
    {
        //Playing one face unit at t=0, checking for state transition, feedback calls, no warnings
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        SingleThreadedPlanPlayer<TimedFaceUnit>fpp = new SingleThreadedPlanPlayer<TimedFaceUnit>(mockFeedbackManager, planManager);
        TimedFaceUnit tfu1 = new TimedFaceUnit(fbManager,BMLBlockPeg.GLOBALPEG, "bml1", "behaviour1", fuMock1, pegBoard);        
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        planManager.addPlanUnit(tfu1);
        KeyPositionMocker.stubKeyPositions(fuMock1, new KeyPosition("start",0,1), new KeyPosition("end",1,1));
                
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tfu1.setTimePeg("start", tpStart);
        tfu1.setState(TimedPlanUnitState.LURKING);        
        fpp.play(0);
        assertEquals(TimedPlanUnitState.IN_EXEC,tfu1.getState());
        assertEquals(1, fbList.size());
        FeedbackAsserts.assertEqualSyncPointProgress(new BMLSyncPointProgressFeedback("bml1","behaviour1","start",0,0), fbList.get(0));
        verify(fuMock1,times(1)).play(0);        
    }
}
