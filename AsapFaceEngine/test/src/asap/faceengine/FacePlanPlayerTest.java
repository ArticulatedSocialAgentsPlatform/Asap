package asap.faceengine;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.ListFeedbackListener;
import asap.realizertestutil.util.KeyPositionMocker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;

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
    private static final double TIMING_PRECISION = 0.0001;
    @Test
    public void testPlayTfu() throws MUPlayException 
    {
        //Playing one face unit at t=0, checking for state transition, feedback calls, no warnings
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        SingleThreadedPlanPlayer<TimedFaceUnit>fpp = new SingleThreadedPlanPlayer<TimedFaceUnit>(mockFeedbackManager, planManager);
        TimedFaceUnit tfu1 = new TimedFaceUnit(fbManager,BMLBlockPeg.GLOBALPEG, "bml1", "behaviour1", fuMock1);        
        fbManager.addFeedbackListener(new ListFeedbackListener(fbList));
        planManager.addPlanUnit(tfu1);
        KeyPositionMocker.stubKeyPositions(fuMock1, new KeyPosition("start",0,1), new KeyPosition("end",1,1));
                
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tfu1.setTimePeg("start", tpStart);
        tfu1.setState(TimedPlanUnitState.LURKING);        
        fpp.play(0);
        assertTrue(tfu1.getState()==TimedPlanUnitState.IN_EXEC);
        assertTrue(fbList.size()==1);
        assertEquals("behaviour1",fbList.get(0).behaviorId);
        assertEquals("bml1",fbList.get(0).bmlId);
        assertEquals("start",fbList.get(0).syncId);
        assertEquals(0, fbList.get(0).timeStamp,TIMING_PRECISION);
        
        verify(fuMock1,times(1)).play(0);        
    }
}
