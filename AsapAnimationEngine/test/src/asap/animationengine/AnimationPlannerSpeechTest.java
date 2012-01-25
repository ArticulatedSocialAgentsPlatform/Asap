package asap.animationengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.DefaultEngine;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.DefaultTimedPlanUnitPlayer;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.mixed.MixedSystem;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.animationengine.restpose.SkeletonPoseRestPose;

/**
 * Tests if visemeunits are properly added and removed from the animation plan
 * @author welberge
 */
public class AnimationPlannerSpeechTest
{
    private MotionUnit mockUnit1 = mock(MotionUnit.class);
    private MotionUnit mockUnit2 = mock(MotionUnit.class);
    private MotionUnit mockUnit3 = mock(MotionUnit.class);
    private MotionUnit mockUnit4 = mock(MotionUnit.class);
    private GestureBinding mockBinding = mock(GestureBinding.class);
    private PhysicalHumanoid mockPh = mock(PhysicalHumanoid.class);
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);

    private AnimationPlanner animationPlanner;
    PlanManager<TimedMotionUnit> planManager = new PlanManager<TimedMotionUnit>();
    DefaultEngine<TimedMotionUnit> animationEngine;

    @Before
    public void setup()
    {
        final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
        ArrayList<MixedSystem> m = new ArrayList<MixedSystem>();
        MixedSystem ms = new MixedSystem(new float[] { 0, -9.8f, 0 }, mockPh);
        m.add(ms);
        AnimationPlayer ap = new AnimationPlayer(HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody(),
                m, 0.001f,
                new AnimationPlanPlayer(new SkeletonPoseRestPose(mockBmlFeedbackManager), mockBmlFeedbackManager, planManager,
                        new DefaultTimedPlanUnitPlayer()));

        animationPlanner = new AnimationPlanner(mockBmlFeedbackManager, ap, mockBinding, planManager);
        List<TimedMotionUnit> visemeMUs = new ArrayList<TimedMotionUnit>();

        animationEngine = new DefaultEngine<TimedMotionUnit>(animationPlanner, ap, planManager);

        TimedMotionUnit tmu = new TimedMotionUnit(mockBmlFeedbackManager, bbPeg, "bml1", "speech1", mockUnit1);
        visemeMUs.add(tmu);
        tmu = new TimedMotionUnit(mockBmlFeedbackManager, bbPeg, "bml1", "speech1", mockUnit2);
        tmu.setSubUnit(true);
        visemeMUs.add(tmu);

        tmu = new TimedMotionUnit(mockBmlFeedbackManager, bbPeg, "bml1", "speech1", mockUnit3);
        visemeMUs.add(tmu);
        tmu.setSubUnit(true);

        tmu = new TimedMotionUnit(mockBmlFeedbackManager, bbPeg, "bml1", "speech1", mockUnit4);
        visemeMUs.add(tmu);
        tmu.setSubUnit(true);

        for (TimedMotionUnit vis : visemeMUs)
        {
            vis.setSubUnit(true);
            planManager.addPlanUnit(vis);
        }
    }

    @Test
    public void testAddSpeech()
    {
        assertEquals(1, animationEngine.getBehaviours("bml1").size());
    }

    @Test
    public void testRemoveSpeech()
    {
        animationEngine.interruptBehaviour("bml1", "speech1", 0);
        assertEquals(0, animationEngine.getBehaviours("bml1").size());
        assertEquals(0, planManager.getBehaviours("bml1").size());
    }
}
