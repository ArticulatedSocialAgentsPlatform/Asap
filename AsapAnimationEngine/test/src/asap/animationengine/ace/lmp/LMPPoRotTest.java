package asap.animationengine.ace.lmp;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit tests for LMPPoRot
 * @author Herwin
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class LMPPoRotTest extends AbstractTimedPlanUnitTest
{
    //private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    private PegBoard pegBoard = new PegBoard();
    @Override
    protected LMPPoRot setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        LMPPoRot lmp = new LMPPoRot("right_arm", bfm, bbPeg, bmlId, id, pegBoard);
        lmp.setTimePeg("start",  TimePegUtil.createTimePeg(bbPeg, startTime));
        return lmp;
    }

}
