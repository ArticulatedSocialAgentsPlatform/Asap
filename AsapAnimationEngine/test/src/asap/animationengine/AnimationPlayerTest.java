/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.mixed.MixedSystem;
import hmi.testutil.animation.HanimBody;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLWarningFeedback;
import asap.animationengine.gaze.ForwardRestGaze;
import asap.animationengine.gaze.GazeInfluence;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;

/**
 * Test cases for the AnimationPlayer 
 * @author welberge
 *
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({TimedAnimationMotionUnit.class,BMLBlockManager.class})
public class AnimationPlayerTest
{
    private AnimationPlayer animationPlayer;
    private List<BMLWarningFeedback> beList;

    private PhysicalHumanoid mockPhysicalHumanoid = mock(PhysicalHumanoid.class);
    private TimedAnimationMotionUnit mockTimedMotionUnit = mock(TimedAnimationMotionUnit.class);
    private BMLBlockManager mockBMLBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBMLBlockManager,"character1");
    private PegBoard pegBoard = new PegBoard();
    
    private PlanManager<TimedAnimationUnit> planManager = new PlanManager<>();
    
    @Before
    public void setup()
    {
        ArrayList<MixedSystem> m = new ArrayList<MixedSystem>();
        float g[] = { 0, 0, 0 };
        MixedSystem mix = new MixedSystem(g, mockPhysicalHumanoid);
        m.add(mix);
        
        
        animationPlayer = new AnimationPlayer(HanimBody.getLOA1HanimBody(),
                HanimBody.getLOA1HanimBody(), HanimBody.getLOA1HanimBody(),
                m, 0.01f,
                new AnimationPlanPlayer(new SkeletonPoseRestPose(), new ForwardRestGaze(GazeInfluence.WAIST), fbManager, planManager,
                        new DefaultTimedPlanUnitPlayer(),pegBoard)                
                );
        beList = new ArrayList<BMLWarningFeedback>();
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(beList).build());
    }    

    @Test
    public void testPlanUnitException() throws TimedPlanUnitPlayException
    {
        
        when(mockTimedMotionUnit.getId()).thenReturn("id1");
        when(mockTimedMotionUnit.getBMLId()).thenReturn("id1");
        when(mockTimedMotionUnit.getStartTime()).thenReturn(0d);
        when(mockTimedMotionUnit.getEndTime()).thenReturn(1d);
        when(mockTimedMotionUnit.getState()).thenReturn(TimedPlanUnitState.IN_EXEC);
        when(mockTimedMotionUnit.isPlaying()).thenReturn(true);    
        doThrow(new TimedPlanUnitPlayException("", mockTimedMotionUnit)).when(mockTimedMotionUnit).play(anyDouble());       
        
        planManager.addPlanUnit(mockTimedMotionUnit);                    
        animationPlayer.playStep(0);
        verify(mockTimedMotionUnit,times(1)).play(anyDouble());
        assertEquals(1, beList.size());        
    }
}
