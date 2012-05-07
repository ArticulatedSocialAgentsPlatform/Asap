package asap.animationengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.bml.core.Behaviour;
import hmi.bml.core.HeadBehaviour;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.PlannerTests;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;


/**
 * Test cases for the AnimationPlanner
 * @author welberge
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class AnimationPlannerTest
{
    private AnimationPlayer mockPlayer = mock(AnimationPlayer.class);
    private GestureBinding mockBinding = mock(GestureBinding.class);    

    private AnimationUnit mockUnit = mock(AnimationUnit.class);
    private PegBoard pegBoard = new PegBoard();

    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    protected FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private AnimationPlanner animationPlanner;
    private PlannerTests<TimedAnimationUnit> plannerTests;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private final PlanManager<TimedAnimationUnit> planManager = new PlanManager<TimedAnimationUnit>();
    private static final double TIMING_PRECISION = 0.0001;
    @Before
    public void setup()
    {
        animationPlanner = new AnimationPlanner(fbManager, mockPlayer, mockBinding, planManager,pegBoard);
        plannerTests = new PlannerTests<TimedAnimationUnit>(animationPlanner, bbPeg);

        TimedAnimationUnit tmu = new TimedAnimationUnit(fbManager, bbPeg, BMLID, "nod1", mockUnit,pegBoard);
        KeyPositionMocker.stubKeyPositions(mockUnit, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        final List<TimedAnimationUnit> tmus = new ArrayList<TimedAnimationUnit>();
        tmus.add(tmu);
        when(mockBinding.getMotionUnit((BMLBlockPeg) any(), (Behaviour) any(), eq(mockPlayer), eq(pegBoard))).thenReturn(tmus);
        when(mockUnit.getPreferedDuration()).thenReturn(3.0);        
    }

    public HeadBehaviour createHeadBehaviour() throws IOException
    {
        return new HeadBehaviour(BMLID, new XMLTokenizer("<head id=\"nod1\" lexeme=\"NOD\"/>"));
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createHeadBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createHeadBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createHeadBehaviour());
    }

    @Test
    public void testAddedToPlanManager() throws IOException, BehaviourPlanningException
    {
        HeadBehaviour beh = new HeadBehaviour(BMLID, new XMLTokenizer("<head id=\"nod1\" lexeme=\"NOD\"/>"));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));
        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertThat(planManager.getBehaviours(BMLID), IsIterableContainingInOrder.contains("nod1"));        
    }

    @Test
    public void testUnknownStart() throws BehaviourPlanningException, IOException
    {
        HeadBehaviour beh = new HeadBehaviour(BMLID, new XMLTokenizer("<head id=\"nod1\" lexeme=\"NOD\"/>"));

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedAnimationUnit pu = animationPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(0.3, sp.getGlobalValue(), TIMING_PRECISION);
        animationPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(0.3, pu.getStartTime(), TIMING_PRECISION);
        assertEquals(3.3, pu.getEndTime(), TIMING_PRECISION);
    }

    @Test
    public void testInterrupt() // throws BehaviourPlanningException
    {
        /*
         * TODO: break up the animationplayer for better testability
         * AnimationPlanner ap = new AnimationPlanner(new AnimationPlayer(),mockBinding);
         * final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1",0.3);
         * HeadBehaviour beh = new HeadBehaviour();
         * beh.readXML("<head id=\"nod1\" action=\"ROTATION\" rotation=\"NOD\"/>");
         * beh.bmlId = "bml1";
         * final HeadBehaviour behF = beh;
         * TimedMotionUnit tmu = new TimedMotionUnit(bbPeg, "nod1", "bml1", mockUnit);
         * 
         * ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
         * TimePeg sp = new TimePeg(bbPeg);
         * sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));
         * 
         * final KeyPosition start = new KeyPosition("start",0,1);
         * final KeyPosition end = new KeyPosition("end",1,1);
         * final List<KeyPosition> keyPos = new ArrayList<KeyPosition>();
         * keyPos.add(start);
         * keyPos.add(end);
         * final List<TimedMotionUnit>tmus = new ArrayList<TimedMotionUnit>();
         * tmus.add(tmu);
         * new NonStrictExpectations()
         * {
         * {
         * mockBinding.getMotionUnit(bbPeg, behF, mockPlayer);returns(tmus);
         * mockUnit.getPreferedDuration();returns(3.0);
         * mockUnit.getKeyPositions();returns(keyPos);
         * mockUnit.getKeyPosition("start");returns(start);
         * mockUnit.getKeyPosition("end");returns(end);
         * }
         * };
         * 
         * PlanUnit pu = ap.resolveSynchs(bbPeg, beh, sacs);
         * assertEquals(0.3,sp.getGlobalValue(),0.0001);
         * ap.addBehaviour(bbPeg, beh, sacs, pu);
         * 
         * ap.interruptBehaviourBlock("nod1", 2);
         * assertTrue(0,getNumberOfTimedMotionUnits());
         */
    }
}
