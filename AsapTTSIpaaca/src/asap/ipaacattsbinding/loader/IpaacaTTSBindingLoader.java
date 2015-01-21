/*******************************************************************************
 *******************************************************************************/
package asap.ipaacattsbinding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.PhonemeToVisemeMappingInfo;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.ipaacattsbinding.IpaacaTTSBinding;
import asap.speechengine.ttsbinding.TTSBindingLoader;

/**
 * XML loader for the IpaacaTTSBinding
 * @author hvanwelbergen
 * 
 */
public class IpaacaTTSBindingLoader implements TTSBindingLoader
{
    private String id;
    private IpaacaTTSBinding binding;

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
        binding = new IpaacaTTSBinding(phoneToVisMapping.getMapping());
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
    public IpaacaTTSBinding getTTSBinding()
    {
        return binding;
    }

}
