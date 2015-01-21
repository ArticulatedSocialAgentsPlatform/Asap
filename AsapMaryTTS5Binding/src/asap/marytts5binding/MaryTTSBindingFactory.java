/*******************************************************************************
 *******************************************************************************/
package asap.marytts5binding;

import hmi.tts.util.PhonemeToVisemeMapping;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.ttsbinding.TTSBindingFactory;

/**
 * Produces a MaryTTSBinding; only used for test cases
 * @author hvanwelbergen
 *
 */
public class MaryTTSBindingFactory implements TTSBindingFactory
{
    private final MaryTTSBinding binding;
    public MaryTTSBindingFactory(PhonemeToVisemeMapping mapping)
    {
        binding = new MaryTTSBinding(mapping);
    }
    
    @Override
    public TTSBinding createBinding()
    {
        return binding;
    }
}