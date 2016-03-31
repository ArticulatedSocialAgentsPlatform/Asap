/*******************************************************************************
 *******************************************************************************/
package asap.marytts5binding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.PhonemeToVisemeMappingInfo;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.marytts5binding.MaryTTSBinding;
import asap.speechengine.ttsbinding.TTSBindingLoader;

/**
 * XML loader for the MaryTTSBinding
 * @author hvanwelbergen
 * 
 */
public class MaryTTSBindingLoader implements TTSBindingLoader
{
    private String id;
    private MaryTTSBinding binding;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        PhonemeToVisemeMappingInfo phoneToVisMapping = new PhonemeToVisemeMappingInfo();

        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case PhonemeToVisemeMappingInfo.XMLTAG:
                phoneToVisMapping = new PhonemeToVisemeMappingInfo();
                phoneToVisMapping.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }
        binding = new MaryTTSBinding(phoneToVisMapping.getMapping());
    }

    @Override
    public void unload()
    {
        if (binding != null)
        {
            binding.cleanup();
        }
    }

    @Override
    public MaryTTSBinding getTTSBinding()
    {
        return binding;
    }

}
