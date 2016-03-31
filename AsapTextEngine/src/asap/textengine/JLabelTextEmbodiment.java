/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.textembodiments.TextEmbodiment;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Take care of its own loading from XML. */
public class JLabelTextEmbodiment implements TextEmbodiment, EmbodimentLoader
{

    private JComponentEmbodiment jce = null;
    private JLabel textLabel;
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

    /** No loading necessary, actually! Empty content expected. No required embodiments */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof JComponentEmbodiment)
            {
                jce = (JComponentEmbodiment) e.getEmbodiment();
            }
        }
        if (jce == null)
        {
            throw new RuntimeException("JLabelTextEmbodiment requires an Embodiment of type JComponentEmbodiment");
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                textLabel = new JLabel();
                jce.addJComponent(textLabel);
            }
        });        
    }

    @Override
    public void unload()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                jce.removeJComponent(textLabel);
            }
        });
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    /** print to stdout */
    @Override
    public void setText(String text)
    {
        textLabel.setText(text);
    }
}
