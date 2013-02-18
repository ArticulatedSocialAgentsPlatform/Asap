package asap.animationengine.ace.lmp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit test for LMPSequence
 * @author hvanwelbergen
 */
public class LMPSequenceTest
{
    private BMLBlockManager bbm = new BMLBlockManager();
    private FeedbackManager fbm = new FeedbackManagerImpl(bbm, "char1");
    private PegBoard pegBoard = new PegBoard();
    private final double TIME_PRECISION = 0.001;

    private LMP createStub(String bmlId, String id, double prepDur, double retrDur, double strokeDur)
    {
        return new StubLMP(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, bmlId, id, pegBoard, new HashSet<String>(),
                new HashSet<String>(), prepDur, retrDur, strokeDur);
    }

    @Test
    public void testEmpty() throws TimedPlanUnitPlayException
    {
        List<TimedAnimationUnit> lmps = new ArrayList<>();
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, lmps);
        seq.setState(TimedPlanUnitState.LURKING);
        seq.start(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, seq.getState());
    }
    
    @Test
    public void testResolveOne()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(2));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(4));
        seq.resolveTimePegs(0);        
        assertEquals(1, seq.getStartTime(), TIME_PRECISION);
        assertEquals(2, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(4, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, seq.getTime("end"), TIME_PRECISION);
    }
    
    @Test
    public void testResolveTwo()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(5));
        seq.resolveTimePegs(0);        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, seq.getTime("end"), TIME_PRECISION);
    }
    
    @Test
    public void testResolveThree()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.resolveTimePegs(0);
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(14, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(21, seq.getTime("end"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(8, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(10, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(10, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(12, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(14, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testDurationsTwo()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        assertEquals(7,seq.getStrokeDuration(), TIME_PRECISION);
        assertEquals(1,seq.getPreparationDuration(), TIME_PRECISION);
        assertEquals(1,seq.getRetractionDuration(), TIME_PRECISION);
    }
}
