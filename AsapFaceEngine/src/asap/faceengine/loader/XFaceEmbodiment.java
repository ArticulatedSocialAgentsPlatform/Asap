/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.loader;

import hmi.environmentbase.CopyEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.util.XFaceController;
import hmi.faceanimation.util.XfaceInterface;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Loader for an XFaceEmbodiment
 * @author hvanwelbergen
 * 
 */
public class XFaceEmbodiment implements FaceEmbodiment, CopyEmbodiment, EmbodimentLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    private FaceController faceController = null;
    private XfaceInterface xfi = null;

    String id = "";

    @Override
    public void unload()
    {
        xfi.disconnect();
    }

    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;

        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("XFaceHost"))
        {
            attrMap = tokenizer.getAttributes();
            tokenizer.takeSTag("XFaceHost");
            int port = adapter.getOptionalIntAttribute("port", attrMap, 50011);
            xfi = new XfaceInterface(port);
            faceController = new XFaceController(xfi);
            xfi.connect();
            tokenizer.takeETag("XFaceHost");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Embodiment content");
        }
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    @Override
    public FaceController getFaceController()
    {
        return faceController;
    }

    public void copy()
    {
        faceController.copy();
    }
}
