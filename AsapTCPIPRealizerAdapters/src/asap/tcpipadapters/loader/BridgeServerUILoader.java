/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.loader.JComponentEmbodimentLoader;
import hmi.util.ArrayUtils;
import hmi.util.CollectionUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.tcpipadapters.ui.BridgeServerUI;

/**
 * XML Loader for the BridgeServerUI
 * @author Herwin
 * 
 */
public class BridgeServerUILoader implements Loader
{
    private String id;

    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
        JComponentEmbodimentLoader jcc = ArrayUtils.getFirstClassOfType(requiredLoaders, JComponentEmbodimentLoader.class);
        if (jcc == null)
        {
            throw new XMLScanException("BridgeServerUILoader requires an JComponentEmbodimentLoader");
        }
        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        TCPIPToBMLRealizerAdapterLoader tcpipAdapterLoader = CollectionUtils.getFirstClassOfType(are.getPipeLoaders(),
                TCPIPToBMLRealizerAdapterLoader.class);
        jcc.getEmbodiment().addJComponent(
                new BridgeServerUI(tcpipAdapterLoader.getAdaptedRealizerPort(), tcpipAdapterLoader.getTcpIpAdapter()).getUI());
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void unload()
    {

    }
}
