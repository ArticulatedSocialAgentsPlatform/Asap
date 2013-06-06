package asap.bmlflowvisualizer.loader;

import hmi.environmentbase.Environment;
import hmi.util.Clock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.realizerembodiments.PipeLoader;
import asap.realizerport.RealizerPort;

public class AsapBMLFlowVisualizerLoader implements PipeLoader
{

    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Environment envs[], Clock theSchedulingClock)
            throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void shutdown()
    {
                
    }

}
