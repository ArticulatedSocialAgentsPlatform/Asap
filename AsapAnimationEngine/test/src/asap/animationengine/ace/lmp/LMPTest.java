/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit tests for LMP
 * @author hvanwelbergen
 * 
 */
public class LMPTest
{
    private FeedbackManager fbm = NullFeedbackManager.getInstance();
    private BMLBlockPeg bbPeg = new BMLBlockPeg("bml1", 0);
    private PegBoard pegBoard = new PegBoard();

    @Test
    public void testSetTimePeg()
    {
        LMP lmp = new StubLMP(fbm, bbPeg, "bml1", "beh1", pegBoard, new HashSet<String>(), new HashSet<String>(), 1, 1, 2);
        TimePeg tp = TimePegUtil.createTimePeg(2);
        lmp.setTimePeg("start", tp);
        assertEquals(tp, lmp.getTimePeg("start"));
        assertEquals(tp, lmp.getStartPeg());
    }

    @Test
    public void testSetAfterTimePeg()
    {
        LMP lmp = new StubLMP(fbm, bbPeg, "bml1", "beh1", pegBoard, new HashSet<String>(), new HashSet<String>(), 1, 1, 2);
        TimePeg tp = TimePegUtil.createTimePeg(2);
        AfterPeg tpAfter = new AfterPeg(tp, 0, bbPeg);
        lmp.setTimePeg("start", tp);
        lmp.setTimePeg("start", tpAfter);
        assertEquals(tpAfter, lmp.getTimePeg("start"));
        assertEquals(tpAfter, lmp.getStartPeg());
    }
}
