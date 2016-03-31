/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.audioenvironment.SoundManager;

import java.io.InputStream;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

/**
 * Unit test cases for the TimedWavAudioUnit
 * @author welberge
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
public class TimedWavAudioUnitTest extends AbstractTimedPlanUnitTest
{
    InputStream mockInputStream = mock(InputStream.class);
    WavUnit mockWavUnit = mock(WavUnit.class);
    SoundManager mockSoundManager = mock(SoundManager.class);

    @Override //behavior does not subside
    public void testSubsiding()
    {
        
    }
    
    @Override
    // XXX no stroke on this behavior
    public void testSetStrokePeg()
    {

    }

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        TimedWavAudioUnit twa = new TimedWavAudioUnit(mockSoundManager, bfm, bbPeg, mockInputStream, bmlId, id);
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(startTime);
        twa.setTimePeg("start", start);

        // XXX a bit of a hack, simulates the setupCache
        twa.wavUnit = mockWavUnit;
        when(mockWavUnit.getDuration()).thenReturn(10d);
        twa.setPrefferedDuration(10d);

        return twa;
    }
}
