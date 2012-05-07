package asap.speechengine.ttsbinding;

import hmi.tts.util.PhonemeToVisemeMapping;

/**
 * Produces a MaryTTSBinding
 * @author hvanwelbergen
 *
 */
public class MaryTTSBindingFactory implements TTSBindingFactory
{
    private final MaryTTSBinding binding;
    public MaryTTSBindingFactory(String maryDir, PhonemeToVisemeMapping mapping)
    {
        binding = new MaryTTSBinding(maryDir, mapping);
    }
    
    @Override
    public TTSBinding createBinding()
    {
        return binding;
    }
}