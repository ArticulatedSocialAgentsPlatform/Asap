/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import saiba.bml.core.Behaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.speechengine.ttsbinding.TTSBinding;

/**
 * Factory to generate DirectSpeechUnits
 * 
 * @author welberge
 */
public final class DirectTTSUnitFactory implements TimedTTSUnitFactory
{
    private final FeedbackManager fbManager;
    
    public DirectTTSUnitFactory(FeedbackManager fbm)
    {
        fbManager = fbm;
    }

    @Override
    public TimedTTSUnit createTimedTTSUnit(BMLBlockPeg bbPeg, String text,String voiceId,
            String bmlId, String id, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass)
    {
        return new TimedDirectTTSUnit(fbManager,bbPeg, text, bmlId, id, ttsBin, behClass);
    }

}
