/*******************************************************************************
 *******************************************************************************/
package asap.bmlflowvisualizer.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.loader.JComponentEmbodimentLoader;
import hmi.util.ArrayUtils;
import hmi.util.CollectionUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import saiba.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Loader for the BMLFlowVisualizaterSwingUI, loads the BMLFlowVisualizerPort in a desired JComponentEmbodiment (e.g. JPanel).
 * @author hvanwelbergen
 */
public class BMLFlowVisualizaterSwingUILoader implements Loader
{
    @Getter
    private String id;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        JComponentEmbodimentLoader jcc = ArrayUtils.getFirstClassOfType(requiredLoaders, JComponentEmbodimentLoader.class);
        if (jcc == null)
        {
            throw new XMLScanException("BMLFlowVisualizaterSwingUILoader requires an JComponentEmbodimentLoader");
        }
        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        AsapBMLFlowVisualizerPortLoader visPortLoader = CollectionUtils.getFirstClassOfType(are.getPipeLoaders(),
                AsapBMLFlowVisualizerPortLoader.class);
        BMLFlowVisualizerPort visPort = visPortLoader.getAdaptedRealizerPort();
        jcc.getEmbodiment().addJComponent(visPort.getVisualization());
    }

    @Override
    public void unload()
    {

    }
}
