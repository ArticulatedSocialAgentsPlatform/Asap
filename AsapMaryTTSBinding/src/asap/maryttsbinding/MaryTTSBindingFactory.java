/*******************************************************************************
 *******************************************************************************/
package asap.maryttsbinding;

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