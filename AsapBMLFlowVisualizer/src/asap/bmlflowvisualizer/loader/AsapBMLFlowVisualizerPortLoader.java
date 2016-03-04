/*******************************************************************************
 *******************************************************************************/
package asap.bmlflowvisualizer.loader;

import hmi.util.Clock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import saiba.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.realizerembodiments.PipeLoader;
import asap.realizerport.RealizerPort;

/**
 * Loads the AsapBMLFlowVisualizer in a new JFrame
 * @author hvanwelbergen
 */
public class AsapBMLFlowVisualizerPortLoader implements PipeLoader
{
    private BMLFlowVisualizerPort vis;
    
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws IOException
    {
        vis = new BMLFlowVisualizerPort(realizerPort);        
    }

    @Override
    public BMLFlowVisualizerPort getAdaptedRealizerPort()
    {
        return vis;
    }

    @Override
    public void shutdown()
    {
                
    }

}
