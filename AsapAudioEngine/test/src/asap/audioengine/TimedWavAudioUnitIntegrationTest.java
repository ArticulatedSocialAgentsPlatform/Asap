/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import static org.mockito.Mockito.mock;
import hmi.audioenvironment.ClipSoundManager;
import hmi.audioenvironment.LWJGLJoalSoundManager;
import hmi.audioenvironment.SoundManager;
import hmi.util.Resources;
import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;

/**
 * Integration test for the TimedWavAudioUnit (using an actual .wav file)
 * @author welberge
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLBlockManager.class)
@Slf4j
public class TimedWavAudioUnitIntegrationTest
{
    private FeedbackManager mockFeedBackManager = mock(FeedbackManager.class);
    private SoundManager soundManager;
    private BMLBlockPeg bbPeg = new BMLBlockPeg("peg1", 0);
    private Resources res = new Resources("");
    
    
    @Before
    public void setup()
    {
        soundManager = new LWJGLJoalSoundManager();
        try{
            soundManager.init();    
        }
        catch (Exception e)
        {
            log.error("Cannot create LWJGLJoalSoundManager, falling back on ClipSoundManager");
            soundManager = new ClipSoundManager();
            soundManager.init();
        }
    }
    
    @After
    public void tearDown()
    {
        soundManager.shutdown();
    }
    
    @Test(expected=AudioUnitPlanningException.class)
    public void testInvalidFile() throws AudioUnitPlanningException
    {
        TimedWavAudioUnit twau = new TimedWavAudioUnit(soundManager,mockFeedBackManager,bbPeg, 
                res.getInputStream("audio/invalid.wav"), "bml1", "audio1");
        twau.setup();
    }
    
    @Test
    public void test() throws TimedPlanUnitPlayException, AudioUnitPlanningException, InterruptedException
    {
        TimedWavAudioUnit twau = new TimedWavAudioUnit(soundManager,mockFeedBackManager,bbPeg, 
                res.getInputStream("audio/audience.wav"), "bml1", "audio1");
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(0);
        twau.setStart(start);
        twau.setup();
        twau.setState(TimedPlanUnitState.LURKING);
        twau.start(0);
        twau.play(0);
        Thread.sleep(5000);
        twau.stop(10);
        Thread.sleep(1000);
    }
    
    @Test
    public void testMono() throws TimedPlanUnitPlayException, AudioUnitPlanningException, InterruptedException
    {
        TimedWavAudioUnit twau = new TimedWavAudioUnit(soundManager,mockFeedBackManager,bbPeg, 
                res.getInputStream("audio/audience_mono.wav"), "bml1", "audio1");
        TimePeg start = new TimePeg(bbPeg);
        start.setGlobalValue(0);
        twau.setStart(start);
        twau.setup();
        twau.setState(TimedPlanUnitState.LURKING);
        twau.start(0);
        twau.play(0);
        Thread.sleep(5000);
        twau.stop(10);
        Thread.sleep(1000);
    }
    

    @Test
    public void testSimultaneousAudio() throws TimedPlanUnitPlayException, AudioUnitPlanningException, InterruptedException
    {
        final int NUMCHANNELS = 20;
        TimedWavAudioUnit twau[] = new TimedWavAudioUnit[NUMCHANNELS];  
        for(int i=0;i<NUMCHANNELS;i++)
        {
            twau[i] = new TimedWavAudioUnit(soundManager,mockFeedBackManager,bbPeg, 
                    res.getInputStream("audio/audience_mono.wav"), "bml1", "audio1");
            TimePeg start = new TimePeg(bbPeg);
            start.setGlobalValue(0);
            twau[i].setStart(start);
            twau[i].setup();
            twau[i].setState(TimedPlanUnitState.LURKING);
            twau[i].start(0);
            twau[i].play(0);
        }        
        Thread.sleep(5000);
        
        for(int i=0;i<NUMCHANNELS;i++)
        {
            twau[i].stop(10);
        }
        Thread.sleep(1000);
    }
}
