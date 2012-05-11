package asap.animationengine.motionunit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.doubleThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hmi.bml.BMLGestureSync;
import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.bml.feedback.ListFeedbackListener;
import hmi.elckerlyc.SyncPointNotFoundException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.AbstractTimedPlanUnitTest;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.TimePegUtil;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.motionunit.MUPlayException;

/**
 * Testcases for the TimedMotionUnit
 * @author welberge
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class TimedMotionUnitTest extends AbstractTimedPlanUnitTest
{
    private AnimationUnit muMock;
    
    private List<BMLSyncPointProgressFeedback> fbList;
    private ListFeedbackListener fbl;
    private TimedAnimationUnit tmu;
    private PegBoard pegBoard = new PegBoard();
    private static final double TIMING_PRECISION = 0.01;
    
    @Before
    public void setup()
    {
        fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        fbl = new ListFeedbackListener(fbList);
        muMock = spy(new StubMotionUnit());
        tmu = new TimedAnimationUnit(fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "behaviour1", muMock, pegBoard);
        fbManager.addFeedbackListener(fbl);
    }

    private void assertEqualBMLSyncPointProgressFeedback(BMLSyncPointProgressFeedback expected, BMLSyncPointProgressFeedback actual)
    {
        assertEquals(expected.behaviorId, actual.behaviorId);
        assertEquals(expected.bmlId, actual.bmlId);
        assertEquals(expected.syncId, actual.syncId);
        assertEquals(expected.bmlBlockTime, actual.bmlBlockTime, TIMING_PRECISION);
        assertEquals(expected.timeStamp, actual.timeStamp, TIMING_PRECISION);
    }

    @Test
    public void testGetAvailableSyncs()
    {
        tmu.resolveGestureKeyPositions();
        assertThat(tmu.getAvailableSyncs(), contains("start", "ready", "strokeStart", "stroke", "strokeEnd", "relax", "end"));        
    }

    @Test
    public void getRelativeTime() throws SyncPointNotFoundException
    {
        muMock.addKeyPosition(new KeyPosition("stroke", 0.4, 1));
        assertEquals(0.4, tmu.getRelativeTime("stroke"), TIMING_PRECISION);
    }

    @Test
    public void getRelativeTimeResolved() throws SyncPointNotFoundException
    {
        muMock.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE.getId(), 0.4, 1));
        tmu.resolveGestureKeyPositions();
        assertEquals(0.0, tmu.getRelativeTime(BMLGestureSync.START.getId()), TIMING_PRECISION);
        assertEquals(0.0, tmu.getRelativeTime(BMLGestureSync.READY.getId()), TIMING_PRECISION);
        assertEquals(0.0, tmu.getRelativeTime(BMLGestureSync.STROKE_START.getId()), TIMING_PRECISION);
        assertEquals(0.4, tmu.getRelativeTime(BMLGestureSync.STROKE.getId()), TIMING_PRECISION);
        assertEquals(1, tmu.getRelativeTime(BMLGestureSync.STROKE_END.getId()), TIMING_PRECISION);
        assertEquals(1, tmu.getRelativeTime(BMLGestureSync.RELAX.getId()), TIMING_PRECISION);
        assertEquals(1, tmu.getRelativeTime(BMLGestureSync.END.getId()), TIMING_PRECISION);
        assertEquals(1, tmu.getRelativeTime("end"), TIMING_PRECISION);
    }

    @Test
    public void testPrepState() throws TimedPlanUnitPlayException, MUPlayException
    {
        // state is IN_PREP, play shouldn't do anything
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));

        tmu.setTimePeg("start", tp);
        tmu.play(1);
        assertTrue(fbList.isEmpty());
        verify(muMock,times(0)).play(anyDouble());
    }

    @Test
    public void testStop() throws MUPlayException, TimedPlanUnitPlayException
    {
        tmu.setState(TimedPlanUnitState.LURKING);

        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);

        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(10);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));

        tmu.start(1);
        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("end", tpEnd);
        tmu.play(1);
        verify(muMock,times(1)).play(anyDouble());
        
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 1, 1), fbList.get(0));
        assertEquals(1, fbList.size());

        tmu.stop(5);
        assertEquals(TimedPlanUnitState.DONE, tmu.getState());
        assertEquals(1, fbList.size());
    }

    @Test
    public void testExecStatesPersistant1() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
       
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.IN_EXEC);
        tmu.play(1);
        
        verify(muMock,times(1)).play(0d);        
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 1, 1), fbList.get(0));
        assertEquals(1, fbList.size());
    }

    @Test
    public void testExecStates() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp2.setGlobalValue(1);
        tmu.setTimePeg("stroke", tp2);
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.SUBSIDING);
        tmu.play(0.5);

        verify(muMock,times(1)).play(doubleThat(closeTo(0.5, 0.4)));
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 0.5, 0.5),
                fbList.get(0));
        assertEquals(1, fbList.size());
    }

    @Test
    public void testExecStatesPersistant2() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp2.setGlobalValue(1);
        tmu.setTimePeg("stroke", tp2);
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.SUBSIDING);
        tmu.play(1);
        verify(muMock,times(1)).play(doubleThat(closeTo(0.5,TIMING_PRECISION)));
    }

    @Test
    public void testExecStatesPersistant3() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp2.setGlobalValue(1);
        tmu.setTimePeg("stroke", tp2);
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.SUBSIDING);
        tmu.play(3);

        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 3, 3), fbList.get(0));
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "stroke", 3, 3), fbList.get(1));
        assertEquals(2, fbList.size());
        verify(muMock,times(1)).play(0.5d);
    }

    @Test
    public void testExecStatesEndTimed() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp2.setGlobalValue(1);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(2);
        tmu.setTimePeg("end", tpEnd);
        tmu.setTimePeg("stroke", tp2);
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.LURKING);
        tmu.start(1.99);
        tmu.play(1.99);

        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 1.99, 1.99),
                fbList.get(0));
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "stroke", 1.99, 1.99),
                fbList.get(1));
        assertEquals(2, fbList.size());
        verify(muMock,times(1)).play(doubleThat(closeTo(1,0.1)));
    }

    @Test
    public void testExecStatesPastEndTime() throws TimedPlanUnitPlayException, MUPlayException
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(0);

        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp2.setGlobalValue(1);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(2);
        tmu.setTimePeg("end", tpEnd);
        tmu.setTimePeg("stroke", tp2);
        tmu.setTimePeg("start", tp);
        tmu.setState(TimedPlanUnitState.SUBSIDING);
        tmu.play(3);
        assertTrue(tmu.isDone());

        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "start", 3, 3), fbList.get(0));
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "stroke", 3, 3), fbList.get(1));
        assertEqualBMLSyncPointProgressFeedback(new BMLSyncPointProgressFeedback("bml1", "behaviour1", "end", 3, 3), fbList.get(2));
        assertEquals(3, fbList.size());
        verify(muMock,times(0)).play(anyDouble());
    }

    @Test
    public void testHasValidTiming() throws MUPlayException
    {
        muMock.addKeyPosition(new KeyPosition("start", 0, 1));
        muMock.addKeyPosition(new KeyPosition("stroke", 0.5, 1));
        muMock.addKeyPosition(new KeyPosition("end", 1, 1));
        
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(1);
        TimePeg tpStroke = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStroke.setGlobalValue(2);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(3);
        tmu.setTimePeg("start", tpStart);
        tmu.setTimePeg("stroke", tpStroke);
        tmu.setTimePeg("end", tpEnd);
        assertTrue(tmu.hasValidTiming());

        tpStroke.setGlobalValue(2);
        assertTrue(tmu.hasValidTiming());

        tpStroke.setGlobalValue(1);
        assertTrue(tmu.hasValidTiming());

        tpStroke.setGlobalValue(3.1);
        assertTrue(!tmu.hasValidTiming());

        tpStroke.setGlobalValue(2);
        tpEnd.setGlobalValue(1.9);
        assertTrue(!tmu.hasValidTiming());
        verify(muMock,times(0)).play(anyDouble());
    }
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        tmu = new TimedAnimationUnit(bfm, bbPeg, bmlId, id, new StubMotionUnit(),pegBoard);
        tmu.resolveGestureKeyPositions();
        tmu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tmu;
    }
}
