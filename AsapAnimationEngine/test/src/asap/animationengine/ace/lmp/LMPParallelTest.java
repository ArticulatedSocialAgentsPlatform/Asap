/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for LMPParallel
 * @author hvanwelbergen
 * 
 */
public class LMPParallelTest
{

    private BMLBlockManager bbm = new BMLBlockManager();
    private FeedbackManager fbm = new FeedbackManagerImpl(bbm, "char1");
    private PegBoard pegBoard = new PegBoard();
    private static final double TIME_PRECISION = 0.001;

    private StubLMP createStub(String bmlId, String id, double prepDur, double retrDur, double strokeDur)
    {
        return new StubLMP(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, bmlId, id, pegBoard, 
                new HashSet<String>(), new HashSet<String>(), prepDur, retrDur, strokeDur);
    }

    @Before
    public void setup()
    {
        pegBoard.addBMLBlockPeg(new BMLBlockPeg("bml1", 0));
    }

    @Test
    public void testEmpty() throws TimedPlanUnitPlayException
    {
        List<TimedAnimationUnit> lmps = new ArrayList<>();
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, lmps);
        par.setState(TimedPlanUnitState.LURKING);
        par.start(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, par.getState());
    }

    @Test
    public void testResolveOne()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).build());
        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(2));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(4));
        par.resolveTimePegs(0);
        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(2, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(4, par.getTime("strokeEnd"), TIME_PRECISION);
    }

    @Test
    public void testResolveTwo()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(6));
        par.resolveTimePegs(0);
        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, par.getTime("strokeEnd"), TIME_PRECISION);        

        assertEquals(2, tmu1.getStartTime(), TIME_PRECISION);
        assertEquals(1, tmu2.getStartTime(), TIME_PRECISION);
        assertEquals(3, tmu1.getStrokeStartTime(), TIME_PRECISION);
        assertEquals(3, tmu2.getStrokeStartTime(), TIME_PRECISION);        
        assertEquals(6, tmu1.getStrokeEndTime(), TIME_PRECISION);
        assertEquals(5, tmu2.getStrokeEndTime(), TIME_PRECISION);
        assertEquals(6, tmu1.getRelaxTime(), TIME_PRECISION);
        assertEquals(6, tmu2.getRelaxTime(), TIME_PRECISION);
    }

    @Test
    public void testDurationsTwo()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        assertEquals(3, par.getStrokeDuration(), TIME_PRECISION);
        assertEquals(2, par.getPreparationDuration(), TIME_PRECISION);
        assertEquals(2, par.getRetractionDuration(), TIME_PRECISION);
    }

    @Test
    public void testDurationsWithSequenceInParallel()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        assertEquals(6, par.getStrokeDuration(), TIME_PRECISION);
        assertEquals(2, par.getPreparationDuration(), TIME_PRECISION);
        assertEquals(2, par.getRetractionDuration(), TIME_PRECISION);
    }

    @Test
    public void testResolveWithSequenceInParallel()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3 + par.getStrokeDuration()));
        par.resolveTimePegs(0);

        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, par.getTime("strokeEnd"), TIME_PRECISION);        

        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(9, tmu1.getRelaxTime(), TIME_PRECISION);
        
        assertEquals(1, seq.getTime("start"), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, seq.getTime("strokeEnd"), TIME_PRECISION);        
        assertEquals(9, seq.getRelaxTime(), TIME_PRECISION);

        assertEquals(1, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(6, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, tmu2b.getTime("strokeEnd"), TIME_PRECISION);        
    }

    @Test
    public void testResolveWithSequenceInParallelAndHandmove()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 3, 3);//,true
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3 + par.getStrokeDuration()));
        par.resolveTimePegs(0);

        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, par.getTime("strokeEnd"), TIME_PRECISION);        
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(9, tmu1.getRelaxTime(), TIME_PRECISION);
        
        assertEquals(1, seq.getTime("start"), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, seq.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(9, seq.getRelaxTime(), TIME_PRECISION);
        
        assertEquals(1, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(6, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, tmu2b.getTime("strokeEnd"), TIME_PRECISION);        
    }
    
    
    @Test
    public void testResolveWithSequenceInParallelAndHandmoveAfterStart() throws TimedPlanUnitPlayException
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 3, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3 + par.getStrokeDuration()));
        par.resolveTimePegs(0);
        par.setState(TimedPlanUnitState.LURKING);
        par.start(0);
        par.play(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, par.getState());

        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, par.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(9, tmu1.getRelaxTime(), TIME_PRECISION);
        
        assertEquals(1, seq.getTime("start"), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, seq.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(1, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(6, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, tmu2b.getTime("strokeEnd"), TIME_PRECISION);        
    }

    @Test
    public void testDurationsWithSequenceInParallelAndHandmove()
    {
        LMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        LMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        LMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        assertEquals(6, par.getStrokeDuration(), TIME_PRECISION);        
    }

    @Test
    public void testUpdateTiming() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(5));
        par.resolveTimePegs(0);
        par.setState(TimedPlanUnitState.LURKING);

        tmu2.setPrepDuration(0.5);
        par.updateTiming(0);
        assertEquals(2, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, par.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getStartTime(), TIME_PRECISION);
        assertEquals(2.5, tmu2.getStartTime(), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingWhileRunning() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2 = createStub("bml1", "beh1-2", 2, 1, 2);
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1, tmu2).build());
        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(5));
        par.resolveTimePegs(0);
        par.setState(TimedPlanUnitState.LURKING);

        par.start(1);
        par.play(1);
        tmu1.setPrepDuration(0.5);
        tmu2.setPrepDuration(0.5);
        par.updateTiming(1);

        assertEquals(1, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, par.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2.5, tmu1.getStartTime(), TIME_PRECISION);
        assertEquals(1, tmu2.getStartTime(), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingWithSequenceInParallel() throws TimedPlanUnitPlayException
    {
        StubLMP tmu1 = createStub("bml1", "beh1-1", 1, 2, 3);
        StubLMP tmu2a = createStub("bml1", "beh1-2a", 2, 1, 2);
        StubLMP tmu2b = createStub("bml1", "beh1-2b", 1, 2, 3);
        LMPSequence seq = new LMPSequence(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu2a, tmu2b).build());
        LMPParallel par = new LMPParallel(fbm, BMLBlockPeg.GLOBALPEG, "bml1", "beh1-par", pegBoard, 
                new ImmutableList.Builder<TimedAnimationUnit>().add(tmu1).add(seq).build());

        par.setTimePeg("strokeStart", TimePegUtil.createTimePeg(3));
        par.setTimePeg("strokeEnd", TimePegUtil.createTimePeg(3 + par.getStrokeDuration()));
        par.resolveTimePegs(0);
        par.setState(TimedPlanUnitState.LURKING);
        tmu2a.setPrepDuration(1.5);
        par.updateTiming(0);

        assertEquals(1.5, par.getStartTime(), TIME_PRECISION);
        assertEquals(3, par.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, par.getTime("strokeEnd"), TIME_PRECISION);
        
        assertEquals(2, tmu1.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu1.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(6, tmu1.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(9, tmu1.getRelaxTime(), TIME_PRECISION);
        
        assertEquals(1.5, seq.getTime("start"), TIME_PRECISION);
        assertEquals(3, seq.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, seq.getTime("strokeEnd"), TIME_PRECISION);
        

        assertEquals(1.5, tmu2a.getTime("start"), TIME_PRECISION);
        assertEquals(3, tmu2a.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(5, tmu2a.getTime("strokeEnd"), TIME_PRECISION);
        assertEquals(5, tmu2b.getTime("start"), TIME_PRECISION);
        assertEquals(6, tmu2b.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(9, tmu2b.getTime("strokeEnd"), TIME_PRECISION);        
    }
}
