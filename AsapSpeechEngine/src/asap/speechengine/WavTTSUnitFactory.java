package asap.speechengine;

import asap.speechengine.ttsbinding.TTSBinding;
import hmi.audioenvironment.SoundManager;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;

/**
 * Factory to generate WavSpeechUnit
 * 
 * @author welberge
 */
public final class WavTTSUnitFactory implements TimedTTSUnitFactory
{
    private final FeedbackManager fbManager;
    private final SoundManager soundManager;
    
    public WavTTSUnitFactory(FeedbackManager fbm, SoundManager soundManager)
    {
        fbManager = fbm;
        this.soundManager = soundManager; 
    }

    @Override
    public TimedTTSUnit createTimedTTSUnit(BMLBlockPeg bbPeg, String text,
            String voiceId, String bmlId, String id, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass)
    {
        return new TimedWavTTSUnit(fbManager, soundManager, bbPeg, text, voiceId, bmlId, id, ttsBin, behClass);
    }
}
