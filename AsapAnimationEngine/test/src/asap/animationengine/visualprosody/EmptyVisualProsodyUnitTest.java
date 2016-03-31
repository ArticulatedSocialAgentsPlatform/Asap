package asap.animationengine.visualprosody;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.AnimationPlayer;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitSetupException;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;
import asap.visualprosody.VisualProsody;

/**
 * Unit test cases for transition timed motion units
 * @author Herwin
 * 
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
        "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLScheduler.class, BMLBlockManager.class })
public class EmptyVisualProsodyUnitTest extends AbstractTimedPlanUnitTest
{
    private TimedPlanUnit mockSpeechUnit = mock(TimedPlanUnit.class);
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private VisualProsody mockVisualProsody = mock(VisualProsody.class);

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
            throws TimedPlanUnitSetupException
    {
        when(mockVisualProsody.firstHeadMotion((double[]) any(), anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(
                new double[3]);
        when(mockVisualProsody.getOffset()).thenReturn(new float[3]);
        when(mockVisualProsody.nextHeadMotion((double[]) any(), (double[]) any(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new double[3]);

        VisualProsodyUnit vp = new VisualProsodyUnit(bfm, bbPeg, bmlId, id, mockSpeechUnit, mockVisualProsody, mockAnimationPlayer,
                new double[0], new double[0], 0.01, TimePegUtil.createTimePeg(bbPeg, startTime), TimePegUtil.createTimePeg(bbPeg,
                        startTime + 2));
        return vp;
    }

    @Override
    @Test
    public void testSetStrokePeg() throws TimedPlanUnitSetupException
    {

    }
}
