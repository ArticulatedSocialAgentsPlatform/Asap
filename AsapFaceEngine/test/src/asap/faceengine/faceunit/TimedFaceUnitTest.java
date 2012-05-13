package asap.faceengine.faceunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.ListFeedbackListener;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.KeyPositionMocker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.motionunit.MUPlayException;

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
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private static final double TIMING_PRECISION = 0.0001;
    
    public TimedFaceUnit createTimedFaceUnit(String behId, String bmlId, FaceUnit fu)
    {
        return new TimedFaceUnit(fbManager,BMLBlockPeg.GLOBALPEG, bmlId, behId, fu);
    }
    
    @Test
    public void testPrepState() throws TimedPlanUnitPlayException, MUPlayException
    {
        //state is IN_PREP, play shouldn't do anything
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>(); 
        TimedFaceUnit tfu = createTimedFaceUnit("behaviour1", "bml1", fuMock);
        fbManager.addFeedbackListener(new ListFeedbackListener(fbList));
        
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);
                
        KeyPositionMocker.stubKeyPositions(fuMock, new KeyPosition("start",0,1), new KeyPosition("end",1,1));
        
        tfu.setTimePeg("start", tp);
        tfu.play(1);
        assertTrue(fbList.isEmpty());
        verify(fuMock,never()).play(anyDouble());
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
        
        KeyPositionMocker.stubKeyPositions(fuMock, new KeyPosition("start",0,1), new KeyPosition("end",1,1));        
        
        tfu.setTimePeg("start", tpStart);
        tfu.setTimePeg("end", tpEnd);
        tfu.setState(TimedPlanUnitState.LURKING);
        tfu.start(0.5);
        tfu.play(0.5);        
        assertTrue(fbList.size()==1);
        assertEquals("behaviour1",fbList.get(0).behaviorId);
        assertEquals("bml1",fbList.get(0).bmlId);
        assertEquals("start",fbList.get(0).syncId);
        assertEquals(0.5,fbList.get(0).timeStamp,TIMING_PRECISION);
        assertEquals(0.5,fbList.get(0).bmlBlockTime,TIMING_PRECISION);       
        verify(fuMock,times(1)).play(0.5);
    }
    
    @Override //no stroke peg
    public void testSetStrokePeg()
    {
        
    }
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedFaceUnit tfu = new TimedFaceUnit(bfm,bbPeg, bmlId, id, fuMock);
        KeyPositionMocker.stubKeyPositions(fuMock,new KeyPosition("start",0,1),
                                                  new KeyPosition("attackPeak",0,1),
                                                  new KeyPosition("relax",1,1),
                                                  new KeyPosition("end",1,1));
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        tfu.setTimePeg("start", start);
        return tfu;
    }
}
