/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.picture.display.PictureDisplay;
import asap.picture.loader.PictureEmbodiment;


/** Take care of its own loading from XML. */
public class JFramePictureEmbodiment implements EmbodimentLoader, PictureEmbodiment
{
    private PictureDisplay display;

    private String id = "";

    private int width = 400;
    private int height = 400;

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException
    {
        setId(loaderId);
        HashMap<String, String> attrMap = null;
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        if (tokenizer.atSTag("Size"))
        {
            attrMap = tokenizer.getAttributes();
        	width = adapter.getRequiredIntAttribute("width", attrMap, tokenizer);
        	height = adapter.getRequiredIntAttribute("height", attrMap, tokenizer);
            tokenizer.takeEmptyElement("Size");            
        } 

        // initialize the picturedisplay
        display = new PictureJFrame(width,height);
        return;
    }

    @Override
    public void unload()
    {
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    public PictureDisplay getPictureDisplay()
    {
        return display;
    }
}
