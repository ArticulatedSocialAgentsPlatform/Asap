/*******************************************************************************
 *******************************************************************************/
package asap.srnao.swing;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.srnao.display.PictureDisplay;
import asap.srnao.loader.StompROSNaoEmbodiment;


/** Take care of its own loading from XML. */
public class JFramePictureEmbodiment implements EmbodimentLoader, StompROSNaoEmbodiment
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
