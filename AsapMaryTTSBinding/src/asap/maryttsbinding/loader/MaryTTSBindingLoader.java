package asap.maryttsbinding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.XMLPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import asap.maryttsbinding.MaryTTSBinding;
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

    private class MaryTTSInfo extends XMLStructureAdapter
    {
        @Getter
        private String marydir = System.getProperty("user.dir") + "/lib/MARYTTS";
        
        public void decodeAttributes(HashMap<String,String> attrMap, XMLTokenizer tokenizer)
        {
            String localMaryDir = getOptionalAttribute("localmarydir", attrMap);
            marydir = getOptionalAttribute("marydir", attrMap);
            if (marydir == null)
            {
                if (localMaryDir != null)
                {
                    String spr = System.getProperty("shared.project.root");
                    if (spr == null)
                    {
                        throw tokenizer.getXMLScanException("the use of the localmarydir setting "
                                + "requires a shared.project.root system variable (often: -Dshared.project.root=\"../..\" "
                                + "but this may depend on your system setup).");
                    }
                    marydir = System.getProperty("shared.project.root") + "/" + localMaryDir;
                }
            }
        }
        
        public String getXMLTag()
        {
            return XMLTAG;
        }
        private static final String XMLTAG = "MaryTTS";
    }

    private class PhonemeToVisemeMappingInfo extends XMLStructureAdapter
    {
        @Getter
        private PhonemeToVisemeMapping mapping;
        
        PhonemeToVisemeMappingInfo()
        {
            mapping = new NullPhonemeToVisemeMapping(); 
        }
        
        public void decodeAttributes(HashMap<String,String> attrMap, XMLTokenizer tokenizer)
        {
            String resources = getRequiredAttribute("resources",attrMap, tokenizer);
            String filename = getRequiredAttribute("filename",attrMap, tokenizer);
            XMLPhonemeToVisemeMapping xmlmapping = new XMLPhonemeToVisemeMapping();
            try
            {
                xmlmapping.readXML(new Resources(resources).getReader(filename));
            }
            catch (IOException e)
            {
                XMLScanException ex = new XMLScanException(e.getMessage());
                ex.initCause(e);
                throw ex;
            }
            mapping = xmlmapping ;
        }
        
        public String getXMLTag()
        {
            return XMLTAG;
        }
        private static final String XMLTAG = "PhonemeToVisemeMapping";
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        MaryTTSInfo maryTTS = new MaryTTSInfo();
        PhonemeToVisemeMappingInfo phoneToVisMapping = new PhonemeToVisemeMappingInfo();
        
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case MaryTTSInfo.XMLTAG:
                maryTTS = new MaryTTSInfo();
                maryTTS.readXML(tokenizer);
                break;
            case PhonemeToVisemeMappingInfo.XMLTAG:
                phoneToVisMapping = new PhonemeToVisemeMappingInfo();
                phoneToVisMapping.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }        
        binding = new MaryTTSBinding(maryTTS.getMarydir(), phoneToVisMapping.getMapping());        
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
