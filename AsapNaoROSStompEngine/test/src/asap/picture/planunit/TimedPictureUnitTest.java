package asap.picture.planunit;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.KeyPositionMocker;
import asap.realizertestutil.util.TimePegUtil;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.TimedNaoUnit;

/**
 * Unit tests for the TimedPictureUnit
 * @author Herwin
 *
 */
@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class TimedPictureUnitTest extends AbstractTimedPlanUnitTest
{
    private NaoUnit mockPictureUnit = mock(NaoUnit.class);
    
    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        KeyPositionMocker.stubKeyPositions(mockPictureUnit, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        TimedNaoUnit tpu = new TimedNaoUnit(bfm, bbPeg, bmlId, id, mockPictureUnit);
        tpu.setTimePeg("start", TimePegUtil.createTimePeg(bbPeg, startTime));
        return tpu;
    }
    
    @Override
    @Test
    public void testSubsiding()
    {
        
    }
    
    @Override
    @Test
    public void testSetStrokePeg()
    {
        
    }
}
