/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockStatus;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit tests cases to verify feedback sent by the BMLBlocks/Blockmanager
 * @author Herwin
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLScheduler.class)
public class BMLBlockManagerFeedbackTest
{
    private BMLBlockManager bbm = new BMLBlockManager();
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private final PegBoard pegBoard = new PegBoard();

    @Before
    public void setup()
    {

    }

    @Test
    public void testFeedbackOnEmpty()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);
        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");

        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);

        BMLSyncPointProgressFeedback spp = new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 1, 1);
        bbm.syncProgress(spp);
        // previous end definition
        // verify(mockScheduler,times(1)).blockStopFeedback("bml1");
        verify(mockScheduler, times(0)).blockStopFeedback("bml1", BMLABlockStatus.DONE, 0);

        verify(mockScheduler, atLeastOnce()).getBehaviours("bml1");
    }

    @Test
    public void testFeedbackGiven1()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        // bml1:beh1:start = 1
        // bml1:beh1:end = unknown
        // bml1:beh2:stroke = 1
        // bml1:beh2:end = 2
        // => bml1:beh1:stroke = 2
        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(1));
        pegBoard.addTimePeg("bml1", "beh1", "end", TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN));
        pegBoard.addTimePeg("bml1", "beh2", "stroke", TimePegUtil.createTimePeg(1));
        TimePeg tp3 = TimePegUtil.createTimePeg(2);
        pegBoard.addTimePeg("bml1", "beh1", "stroke", tp3);
        pegBoard.addTimePeg("bml1", "beh2", "end", tp3);

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        behs.add("beh2");

        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);
        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(TimePeg.VALUE_UNKNOWN);
        when(mockScheduler.getEndTime("bml1", "beh2")).thenReturn(TimePeg.VALUE_UNKNOWN);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "stroke", 1.1, 1.1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", 2, 2));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "end", 2, 2));

        // previous end definition
        // verify(mockScheduler,times(1)).blockStopFeedback("bml1");
        verify(mockScheduler, times(0)).blockStopFeedback(eq("bml1"), any(BMLABlockStatus.class), eq(0));
        verify(mockScheduler, times(0)).blockStopFeedback(eq("bml2"), any(BMLABlockStatus.class), eq(0));
    }

    @Test
    public void testFeedbackNotGiven1()
    {
        // bml1:beh1:start = 1
        // bml1:beh1:stroke = 2
        // bml1:beh1:end = unknown
        // bml1:beh2:stroke = 1
        // bml1:beh2:end = 2
        // bml2:beh1:start = 6
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(1));

        TimePeg tp3 = TimePegUtil.createTimePeg(2);
        pegBoard.addTimePeg("bml1", "beh1", "stroke", tp3);
        pegBoard.addTimePeg("bml1", "beh2", "end", tp3);

        pegBoard.addTimePeg("bml1", "beh1", "end", TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN));
        pegBoard.addTimePeg("bml1", "beh2", "stroke", TimePegUtil.createTimePeg(1));
        pegBoard.addTimePeg("bml2", "beh1", "start", TimePegUtil.createTimePeg(6));

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        behs.add("beh2");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "stroke", 1.5, 1.5));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "end", 2, 2));

        verify(mockScheduler, never()).blockStopFeedback(eq("bml1"), any(BMLABlockStatus.class), eq(0));
    }

    @Test
    public void testNoFeedbackOnEnd()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(1));

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);
        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(4.0);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 4, 4));
        verify(mockScheduler, never()).blockStopFeedback(eq("bml1"), any(BMLABlockStatus.class), eq(0));
    }

    @Test
    public void testFeedbackOnEnd()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(1));

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);
        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(4.0);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "end", 4, 4));

        verify(mockScheduler, times(1)).blockStopFeedback("bml1", BMLABlockStatus.DONE, 4);
    }

    @Test
    public void testTwoBehaviours()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(1));
        pegBoard.addTimePeg("bml1", "beh2", "start", TimePegUtil.createTimePeg(2));

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        behs.add("beh2");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);
        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(4.0);
        when(mockScheduler.getEndTime("bml1", "beh2")).thenReturn(8.0);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "start", 3, 3));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "end", 4, 4));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "end", 8, 8));

        verify(mockScheduler, times(1)).blockStopFeedback("bml1", BMLABlockStatus.DONE, 8);
    }

    @Test
    public void testFeedbackOnException()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);
        when(mockScheduler.getBehaviours("bml1")).thenReturn(new HashSet<String>());

        bbm.warn(new BMLWarningFeedback("bml1", "TEST", ""), 0);
        verify(mockScheduler, times(1)).blockStopFeedback("bml1", BMLABlockStatus.DONE, 0);
    }

    @Test
    public void testTwoBehavioursReverse()
    {
        BMLBBlock bb = new BMLBBlock("bml1", mockScheduler, pegBoard);
        bbm.addBMLBlock(bb);
        bbm.startBlock("bml1", 0);

        pegBoard.addTimePeg("bml1", "beh2", "start", TimePegUtil.createTimePeg(1));
        pegBoard.addTimePeg("bml1", "beh1", "start", TimePegUtil.createTimePeg(2));

        final Set<String> behs = new HashSet<String>();
        behs.add("beh1");
        behs.add("beh2");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(behs);
        when(mockScheduler.getEndTime("bml1", "beh2")).thenReturn(4.0);
        when(mockScheduler.getEndTime("bml1", "beh1")).thenReturn(8.0);

        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "start", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 3, 3));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh2", "end", 4, 4));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "end", 8, 8));

        verify(mockScheduler, times(1)).blockStopFeedback("bml1", BMLABlockStatus.DONE, 8);
    }

    @Test
    public void testUpdate()
    {
        HashSet<String> appendAfter = new HashSet<String>();
        appendAfter.add("bml2");
        appendAfter.add("bml3");
        bbm.addBMLBlock(new BMLBBlock("bml1", mockScheduler, pegBoard, appendAfter, new ArrayList<String>(), new HashSet<String>()));
        bbm.addBMLBlock(new BMLBBlock("bml2", mockScheduler, pegBoard));
        bbm.addBMLBlock(new BMLBBlock("bml3", mockScheduler, pegBoard));

        bbm.activateBlock("bml1", 0);
        bbm.startBlock("bml2", 0);
        bbm.startBlock("bml3", 0);

        bbm.blockProgress(new BMLBlockProgressFeedback("bml2", "end", 1));
        bbm.blockProgress(new BMLBlockProgressFeedback("bml3", "end", 1));

        verify(mockScheduler, times(1)).startBlock("bml1", 1);
        verify(mockScheduler, never()).startBlock(eq("bml2"), anyDouble());
        verify(mockScheduler, never()).startBlock(eq("bml3"), anyDouble());
    }

    @Test
    public void testUpdateRemoved()
    {
        HashSet<String> appendAfter = new HashSet<String>();
        appendAfter.add("bml2");
        appendAfter.add("bml3");
        bbm.addBMLBlock(new BMLBBlock("bml1", mockScheduler, pegBoard, appendAfter, new ArrayList<String>(), new HashSet<String>()));
        bbm.addBMLBlock(new BMLBBlock("bml2", mockScheduler, pegBoard));
        bbm.addBMLBlock(new BMLBBlock("bml3", mockScheduler, pegBoard));

        bbm.activateBlock("bml1", 0);
        bbm.startBlock("bml2", 0);
        bbm.startBlock("bml3", 0);
        bbm.blockProgress(new BMLBlockProgressFeedback("bml2", "end", 1));
        bbm.removeBMLBlock("bml3", 0);

        verify(mockScheduler, times(1)).startBlock("bml1", 0);
        verify(mockScheduler, never()).startBlock("bml2", 0);
        verify(mockScheduler, never()).startBlock("bml3", 0);
    }

    @Test
    public void testNotUpdate()
    {
        HashSet<String> appendAfter = new HashSet<String>();
        appendAfter.add("bml2");
        appendAfter.add("bml3");
        BMLBBlock bml1 = new BMLBBlock("bml1", mockScheduler, pegBoard, appendAfter, new ArrayList<String>(), new HashSet<String>());
        bbm.addBMLBlock(bml1);
        bbm.addBMLBlock(new BMLBBlock("bml2", mockScheduler, pegBoard));
        bbm.addBMLBlock(new BMLBBlock("bml3", mockScheduler, pegBoard));

        bml1.setState(TimedPlanUnitState.PENDING);
        bbm.startBlock("bml2", 0);
        bbm.startBlock("bml3", 0);

        bbm.blockProgress(new BMLBlockProgressFeedback("bml2", "end", 1));

        verify(mockScheduler, never()).startBlock("bml1", 0);
        verify(mockScheduler, never()).startBlock("bml2", 0);
        verify(mockScheduler, never()).startBlock("bml3", 0);
    }
}
