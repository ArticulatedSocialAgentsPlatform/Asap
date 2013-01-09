package asap.maryttsbinding.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.ttsbinding.TTSBindingLoader;

public class MaryTTSBindingLoader implements TTSBindingLoader
{

    @Override
    public String getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void readXML(XMLTokenizer arg0, String arg1, String arg2, String arg3, Environment[] arg4, Loader... arg5) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unload()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TTSBinding getTTSBinding()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
