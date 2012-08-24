package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.animationengine.ace.OrientConstraint;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Testcases for the WristRot LMP
 * @author hvanwelbergen
 *
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class LMPWristRotTest extends AbstractTimedPlanUnitTest
{
    private PegBoard pegBoard = new PegBoard();
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        List<OrientConstraint> ocList = new ArrayList<>();
        ocList.add(new OrientConstraint("stroke_start"));
        ocList.add(new OrientConstraint("stroke_end"));
        LMPWristRot lmp = new LMPWristRot("r_wrist", ocList, bfm, bbPeg, bmlId, id, pegBoard);
        lmp.setTimePeg("start",  TimePegUtil.createTimePeg(bbPeg, startTime));
        return lmp;
    }

}
