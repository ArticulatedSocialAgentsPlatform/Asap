/*******************************************************************************
 *******************************************************************************/
package asap.sapittsbinding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.sapittsbinding.SAPITTSBinding;
import asap.speechengine.ttsbinding.TTSBindingLoader;

/**
 * XML loader for the SapiTTSBinding
 * @author hvanwelbergen
 * 
 */
public class SapiTTSBindingLoader implements TTSBindingLoader
{
    private String id;
    private SAPITTSBinding binding;

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
        binding = new SAPITTSBinding();        
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
    public SAPITTSBinding getTTSBinding()
    {
        return binding;
    }

}
