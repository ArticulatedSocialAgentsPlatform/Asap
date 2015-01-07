/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.jcomponentenvironment.loader.JComponentEmbodimentLoader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JPanel;

import lombok.Getter;
import lombok.Setter;
import asap.picture.display.PictureDisplay;
import asap.picture.loader.PictureEmbodiment;

/**
 * Loads the PictureEmbodiment into an external JComponentEmbodiment
 * @author Herwin
 * 
 */
public class JComponentPictureEmbodiment implements EmbodimentLoader, PictureEmbodiment
{
    @Getter
    @Setter
    private String id = "";

    private PictureDisplay display;
    private JComponentEmbodiment jce;
    private JPanel jPanel;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        JComponentEmbodimentLoader jcl = ArrayUtils.getFirstClassOfType(requiredLoaders, JComponentEmbodimentLoader.class);
        if (jcl == null)
        {
            throw new XMLScanException("JComponentPictureEmbodiment requires an JComponentEmbodimentLoader.");
        }
        jce = jcl.getEmbodiment();
        if (jce == null)
        {
            throw new XMLScanException("JComponentPictureEmbodiment: null embodiment in JComponentEmbodimentLoader.");
        }
        
        
        jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1,1));
        display = new PictureJComponent(jPanel);
        jce.addJComponent(jPanel);
    }

    @Override
    public void unload()
    {
        jce.removeJComponent(jPanel);
    }

    @Override
    public PictureDisplay getPictureDisplay()
    {
        return display;
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }
}
