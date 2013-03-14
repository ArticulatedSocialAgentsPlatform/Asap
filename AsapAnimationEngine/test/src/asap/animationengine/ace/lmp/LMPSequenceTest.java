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

    private StubLMP createStub(String bmlId, String id, double prepDur, double retrDur, double strokeDur)
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
        
        assertEquals(1, tmu1.getStartTime(), TIME_PRECISION);
        assertEquals(2, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(4, tmu1.getTime("strokeEnd"), TIME_PRECISION);        
    }
    
    @Test
    public void testResolveTwoWithSkewing()
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
        
        assertEquals(2, tmu1.getStartTime(), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(3+6d/7d, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(3+6d/7d, tmu2.getStartTime(), TIME_PRECISION);
        assertEquals(3+10d/7d, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2.getTime("strokeEnd"), TIME_PRECISION);
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
    
    @Test
    public void testPlaybackTwo() throws TimedPlanUnitPlayException
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.resolveTimePegs(0);
        
        seq.setState(TimedPlanUnitState.LURKING);
        seq.start(2);
        seq.play(3);
        assertEquals(TimedPlanUnitState.IN_EXEC, tmu1.getState());
        assertEquals(TimedPlanUnitState.LURKING, tmu2.getState());
        
        seq.play(7);
        assertEquals(TimedPlanUnitState.IN_EXEC, tmu1.getState());
        assertEquals(TimedPlanUnitState.IN_EXEC, tmu2.getState());
    }
    
    @Test
    public void testDurationsWithParallelInSequence()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a).add(tmu2b).build());
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, par).build());
        
        assertEquals(8,seq.getStrokeDuration(), TIME_PRECISION);
        assertEquals(1,seq.getPreparationDuration(), TIME_PRECISION);
        assertEquals(2,seq.getRetractionDuration(), TIME_PRECISION);        
    }
    
    
    @Test
    public void testResolveWithParallelInSequence()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a).add(tmu2b).build());
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, par).build());
        
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.resolveTimePegs(0);
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(13, seq.getTime("end"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, par.getTime("start"), TIME_PRECISION);
        assertEquals(8, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, par.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(6, tmu2a.getStartTime(), TIME_PRECISION);
        assertEquals(8, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(7, tmu2b.getStartTime(), TIME_PRECISION);
        assertEquals(8, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2b.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testCountInternalSyncs()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(5));
        seq.resolveTimePegs(0);
        
        assertEquals(2, seq.countInternalSyncs(pegBoard.getPegKeys(pegBoard.getTimePeg(seq.getBMLId(),seq.getId(),"start")),0));
    }
    
    @Test
    public void testUpdateTiming() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.resolveTimePegs(0);
        seq.setState(TimedPlanUnitState.LURKING);
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        seq.updateTiming(0);
        
        assertEquals(1, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(23, seq.getTime("end"), TIME_PRECISION);
        
        assertEquals(1, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(9, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(11, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(14, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testUpdateTimingWhileRunningInPrep() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.resolveTimePegs(0);
        seq.setState(TimedPlanUnitState.IN_EXEC);
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        seq.updateTiming(2);
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(23, seq.getTime("end"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);
        assertEquals(9, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(11, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(11, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(14, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(16, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
    
    @Test
    public void testUpdateTimingWhileRunningInStroke() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        StubLMP tmu3 = createStub("bml1", "beh1-3", 2, 7, 2);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard,
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2, tmu3).build());
        seq.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        seq.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3+seq.getStrokeDuration()));
        seq.setState(TimedPlanUnitState.LURKING);
        seq.resolveTimePegs(0);
        seq.start(2);
        seq.play(2);
        
        
        tmu1.setPrepDuration(2);
        tmu2.setPrepDuration(3);
        tmu3.setPrepDuration(3);
        seq.play(6.9);
        seq.updateTiming(7);
        
        
        assertEquals(2, seq.getStartTime(), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(15, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(22, seq.getTime("end"), TIME_PRECISION);
        
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(6, tmu2.getTime("start"), TIME_PRECISION);        
        assertEquals(8, tmu2.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(10, tmu2.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(10, tmu3.getTime("start"), TIME_PRECISION);
        assertEquals(13, tmu3.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(15, tmu3.getTime("strokeEnd"), TIME_PRECISION);
    }
}
