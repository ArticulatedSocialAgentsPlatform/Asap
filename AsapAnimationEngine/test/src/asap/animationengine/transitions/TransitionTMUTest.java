package asap.animationengine.transitions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.transitions.TransitionMU;
import asap.animationengine.transitions.TransitionTMU;

import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;
/**
 * Unit test cases for transition timed motion units
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class,BMLBlockManager.class})

public class TransitionTMUTest extends AbstractTimedPlanUnitTest
{
    TransitionMU mockTransitionMU = mock(TransitionMU.class);
    FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private PegBoard pegBoard = new PegBoard();
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TransitionTMU tmu = new TransitionTMU(bfm, bbPeg, bmlId, id, mockTransitionMU, pegBoard);
        KeyPositionMocker.stubKeyPositions(mockTransitionMU,new KeyPosition("start",0,1),
                new KeyPosition("ready",0,1),
                new KeyPosition("strokeStart",0,1),
                new KeyPosition("stroke",0.5,1),
                new KeyPosition("strokeEnd",1,1),
                new KeyPosition("relax",1,1),
                new KeyPosition("end",1,1));
        tmu.resolveDefaultBMLKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;       
    }    
    
    @Test
    public void testExecStates() throws TimedPlanUnitPlayException
    {
        TransitionTMU tmu = new TransitionTMU(mockBmlFeedbackManager,BMLBlockPeg.GLOBALPEG, "bml1", "behaviour1", mockTransitionMU, pegBoard);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(1);
        
        final KeyPosition start = new KeyPosition("start",0,1);
        final KeyPosition end = new KeyPosition("end",1,1);
        when(mockTransitionMU.getKeyPosition("start")).thenReturn(start);
        when(mockTransitionMU.getKeyPosition("end")).thenReturn(end);
        
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(0.5);
        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.setState(TimedPlanUnitState.IN_EXEC);
        tmu.play(0.5);
        
        verify(mockTransitionMU,atLeastOnce()).getKeyPosition("start");
        verify(mockTransitionMU,atLeastOnce()).getKeyPosition("end");
        verify(mockTransitionMU,atLeastOnce()).setStartPose();
        verify(mockTransitionMU,times(1)).play(eq(0.5,0.01));
    }
}
