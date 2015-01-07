/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.picture.display.PictureDisplay;
import asap.picture.loader.PictureEmbodiment;


/** Take care of its own loading from XML. */
public class JFramePictureEmbodiment implements EmbodimentLoader, PictureEmbodiment
{
    private PictureDisplay display;

    private String id = "";

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

        // initialize the picturedisplay
        display = new PictureJFrame();
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
