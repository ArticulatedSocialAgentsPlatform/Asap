/*******************************************************************************
 *******************************************************************************/
package asap.ipaacattsbinding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.PhonemeToVisemeMappingInfo;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import asap.ipaacattsbinding.IpaacaTTSBinding;
import asap.speechengine.ttsbinding.TTSBindingLoader;
import asap.tts.ipaaca.NullProsodyAnalyzer;
import asap.tts.ipaaca.OpenSmileProsodyAnalyzer;
import asap.tts.ipaaca.VisualProsodyAnalyzer;

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

    private static class VisualProsodyAnalyzerLoader extends XMLStructureAdapter
    {
        @Getter
        private VisualProsodyAnalyzer visualProsodyAnalyzer = new NullProsodyAnalyzer();

        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            String type = getRequiredAttribute("type", attrMap, tokenizer);
            if (type.equals("OPENSMILE"))
            {
                visualProsodyAnalyzer = new OpenSmileProsodyAnalyzer();
            }
        }

        public String getXMLTag()
        {
            return XMLTAG;
        }

        public static final String XMLTAG = "VisualProsodyAnalyzer";
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        PhonemeToVisemeMappingInfo phoneToVisMapping = new PhonemeToVisemeMappingInfo();
        VisualProsodyAnalyzerLoader vpLoader = new VisualProsodyAnalyzerLoader();
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case PhonemeToVisemeMappingInfo.XMLTAG:
                phoneToVisMapping.readXML(tokenizer);
                break;
            case VisualProsodyAnalyzerLoader.XMLTAG:
                vpLoader.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }
        binding = new IpaacaTTSBinding(phoneToVisMapping.getMapping(), vpLoader.getVisualProsodyAnalyzer());
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
