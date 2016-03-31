/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.BMLGestureSync;
import saiba.bml.parser.Constraint;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnitTest;
import asap.animationengine.restpose.RestPose;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.util.KeyPositionMocker;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit test cases for ProcAnimationGestureTMUs
 * @author hvanwelbergen
 * 
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class ProcAnimationGestureTMUTest extends TimedAnimationMotionUnitTest
{
    private ProcAnimationMU mockProcAnimation = mock(ProcAnimationMU.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    private static final double STROKE_DURATION = 1;
    private static final double TIME_PRECISION = 0.0001;
    private static final double RETRACTION_DURATION = 1;

    @SuppressWarnings("unchecked")
    protected ProcAnimationGestureTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId)
            throws MUSetupException
    {
        if (pegBoard.getBMLBlockPeg(bmlId) == null)
        {
            pegBoard.addBMLBlockPeg(new BMLBlockPeg(bmlId, 0));
        }
        VJoint vCurr = HanimBody.getLOA1HanimBody();
        VJoint vNext = HanimBody.getLOA1HanimBody();
        ProcAnimationGestureMU mu = new ProcAnimationGestureMU();
        mu.setGestureUnit(mockProcAnimation);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockAnimationPlayer.getVCurrPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        when(mockAnimationPlayer.getVNextPartBySid(anyString())).thenReturn(HanimBody.getLOA1HanimBody().getPartBySid(Hanim.l_shoulder));
        
        when(mockProcAnimation.copy((VJoint) any())).thenReturn(mockProcAnimation);
        when(mockProcAnimation.getPreferedDuration()).thenReturn(STROKE_DURATION);
        when(mockProcAnimation.getPrefDuration()).thenReturn(STROKE_DURATION);

        KeyPositionMocker.stubKeyPositions(mockProcAnimation, new KeyPosition("start", 0), new KeyPosition("ready", 0.4), new KeyPosition(
                "strokeStart", 0.4), new KeyPosition("stroke", 0.5), new KeyPosition("strokeEnd", 0.8), new KeyPosition("relax", 0.8),
                new KeyPosition("end", 1));

        RestPose mockRestPose = mock(RestPose.class);
        when(mockRestPose.getTransitionToRestDuration((VJoint) any(), (Set<String>) any())).thenReturn(RETRACTION_DURATION);

        AnimationUnit mockRelaxMU = mock(AnimationUnit.class);
        when(mockAnimationPlayer.getRestPose()).thenReturn(mockRestPose);
        when(mockRestPose.createTransitionToRest((Set<String>) any())).thenReturn(mockRelaxMU);

        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_START.getId(), 0.2, 1.0));
        mu.addKeyPosition(new KeyPosition(BMLGestureSync.STROKE_END.getId(), 0.8, 1.0));
        mu.setAnimationPlayer(mockAnimationPlayer);

        ProcAnimationGestureTMU tmu = new ProcAnimationGestureTMU(bfm, bbPeg, bmlId, id, mu, pegBoard, mockAnimationPlayer);
        tmu.resolveGestureKeyPositions();
        return tmu;
    }

    @Override
    protected ProcAnimationGestureTMU setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)

    {
        ProcAnimationGestureTMU tmu;
        try
        {
            tmu = setupPlanUnit(bfm, bbPeg, id, bmlId);
        }
        catch (MUSetupException e)
        {
            throw new RuntimeException(e);
        }
        TimePeg tp = TimePegUtil.createTimePeg(bbPeg, startTime);
        tmu.setTimePeg("start", tp);
        pegBoard.addTimePeg("bml1", "id1", "start", tp);
        return tmu;
    }

    @Test
    public void resolveEmptySynchsTest() throws BehaviourPlanningException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
    }

    @Test
    public void resolveEndFromStartTest() throws BehaviourPlanningException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0.1);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0.1, tpStart.getGlobalValue(), TIME_PRECISION);
        assertEquals(0.1 + tpu.getPreferedDuration(), tpEnd.getGlobalValue(), TIME_PRECISION);
    }

    @Test
    public void resolveStartFromEndTest() throws BehaviourPlanningException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpEnd.setGlobalValue(10);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(10, tpEnd.getGlobalValue(), TIME_PRECISION);
        assertEquals(10 - tpu.getPreferedDuration(), tpStart.getGlobalValue(), TIME_PRECISION);
    }

    @Test
    public void resolveStartSynchTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tp.getGlobalValue(), TIME_PRECISION);
    }

    @Test
    public void resolveStrokeFromStartAndEndTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpStroke = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tpEnd.setGlobalValue(3);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("stroke", tpStroke, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tpStart.getGlobalValue(), TIME_PRECISION);
        assertEquals(3, tpEnd.getGlobalValue(), TIME_PRECISION);
        assertThat(tpStroke.getGlobalValue(), greaterThan(0d));
        assertThat(tpStroke.getGlobalValue(), lessThan(3d));
    }

    @Test
    public void resolveEndFromStartAndRelaxTest() throws BehaviourPlanningException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpEnd = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tpRelax = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        tpRelax.setGlobalValue(2);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("relax", tpRelax, new Constraint(), 0));
        tpu.resolveSynchs(BMLBlockPeg.GLOBALPEG, null, sac);
        assertEquals(0, tpStart.getGlobalValue(), TIME_PRECISION);
        assertEquals(2, tpRelax.getGlobalValue(), TIME_PRECISION);
        assertThat(tpEnd.getGlobalValue(), greaterThan(2d));
    }

    @Test
    public void testUpdateTimingNonSetTTPUAtT0() throws TMUPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        tpu.updateTiming(0);
        assertEquals(0, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingNonSetTTPUAtT1() throws TMUPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        tpu.updateTiming(1);
        assertEquals(1, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingSetStart() throws TMUPlayException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 1);
        assertEquals(1, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        tpu.updateTiming(0);
        assertEquals(0, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingFixedStart() throws TMUPlayException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 1);
        TimePeg tp = pegBoard.getTimePeg("bml1", "id1", "start");
        pegBoard.addTimePeg("bml1", "id2", "start", tp);
        tpu.updateTiming(0);
        assertEquals(1, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingPassedStart() throws TMUPlayException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1", 0);
        tpu.updateTiming(1);
        assertEquals(0, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
    }

    @Test
    public void testUpdateTimingStrokeSet() throws TMUPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");

        TimePeg tp = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 1);
        tpu.setTimePeg("stroke", tp);
        pegBoard.addTimePeg("bml1", "id1", "stroke", tp);
        pegBoard.addTimePeg("bml1", "id2", "start", tp);

        tpu.updateTiming(0);
        assertEquals(1, tpu.getTimePeg("stroke").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
        assertThat(tpu.getTimePeg("start").getLocalValue(), greaterThanOrEqualTo(0d));
        assertThat(tpu.getTimePeg("start").getGlobalValue(), greaterThanOrEqualTo(0d));
    }

    @Test
    public void testUpdateTimingStartRelaxSet() throws TMUPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        TimePeg tpStart = TimePegUtil.createAbsoluteTimePeg(0);
        TimePeg tpRelax = TimePegUtil.createAbsoluteTimePeg(4);
        tpu.setTimePeg("start", tpStart);
        tpu.setTimePeg("relax", tpRelax);

        tpu.updateTiming(0);
        assertEquals(0, tpu.getTimePeg("start").getGlobalValue(), TIME_PRECISION);
        assertEquals(4, tpu.getTimePeg("relax").getGlobalValue(), TIME_PRECISION);
        assertEquals(STROKE_DURATION, tpu.getTime("strokeEnd") - tpu.getTime("strokeStart"), TIME_PRECISION);
        assertEquals(RETRACTION_DURATION, tpu.getTime("end") - tpu.getTime("relax"), TIME_PRECISION);
    }

    @Test
    public void testInterrupt() throws TimedPlanUnitPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        TimePeg tpStroke = TimePegUtil.createAbsoluteTimePeg(4);
        TimePeg tpStart = TimePegUtil.createAbsoluteTimePeg(0);
        tpu.setTimePeg("start", tpStart);
        tpu.setTimePeg("stroke", tpStroke);
        tpu.setState(TimedPlanUnitState.LURKING);
        pegBoard.addTimePeg("bml1", "id1", "start", tpStart);
        pegBoard.addTimePeg("bml1", "id1", "stroke", tpStroke);
        tpu.start(0);
        tpu.interrupt(2);
        assertEquals(1.99, tpu.getTime("stroke"), TIME_PRECISION);
    }

    @Test
    public void testInterruptUnlinkStroke() throws TimedPlanUnitPlayException, MUSetupException
    {
        ProcAnimationGestureTMU tpu = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "id1", "bml1");
        TimePeg tpStroke = TimePegUtil.createAbsoluteTimePeg(4);
        TimePeg tpStart = TimePegUtil.createAbsoluteTimePeg(0);
        tpu.setTimePeg("start", tpStart);
        tpu.setTimePeg("stroke", tpStroke);
        tpu.setState(TimedPlanUnitState.LURKING);
        pegBoard.addTimePeg("bml1", "id1", "start", tpStart);
        pegBoard.addTimePeg("bml1", "id1", "stroke", tpStroke);
        pegBoard.addTimePeg("bml1", "speechX", "sync", tpStroke);
        tpu.start(0);
        tpu.interrupt(2);
        assertEquals(1.99, tpu.getTime("stroke"), TIME_PRECISION);
        assertEquals(4, tpStroke.getGlobalValue(), TIME_PRECISION);
        assertEquals(4, pegBoard.getPegTime("bml1", "speechX", "sync"), TIME_PRECISION);
        assertEquals(1.99, pegBoard.getPegTime("bml1", "id1", "stroke"), TIME_PRECISION);
    }
}
