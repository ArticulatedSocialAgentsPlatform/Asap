/*******************************************************************************
 *******************************************************************************/
package asap.picture.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.picture.PicturePlanner;
import asap.picture.picturebinding.PictureBinding;
import asap.picture.planunit.TimedPictureUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**

*/
public class PictureEngineLoader implements EngineLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private PictureEmbodiment pe = null;

    private Engine engine = null;
    private PlanManager<TimedPictureUnit> planManager = null;
    private Player player = null;
    private String id = "";
    // some variables cached during loading
    private PictureBinding pictureBinding = null;
    private AsapRealizerEmbodiment are = null;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof PictureEmbodiment)
            {
                pe = (PictureEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) e.getEmbodiment();
            }
        }
        if (pe == null)
        {
            throw new RuntimeException("PictureEngineLoader requires an EmbodimentLoader containing a PictureEmbodiment");
        }
        if (are == null)
        {
            throw new RuntimeException("PictureEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        constructEngine(tokenizer);
    }

    @Override
    public void unload()
    {
        // engine.shutdown(); already done by realizer
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("PictureBinding"))
        {
            attrMap = tokenizer.getAttributes();
            pictureBinding = new PictureBinding(pe.getPictureDisplay());
            try
            {
                pictureBinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException("Cannnot load PictureBinding: " + e);
            }
            tokenizer.takeEmptyElement("PictureBinding");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    private void constructEngine(XMLTokenizer tokenizer)
    {
        if (pictureBinding == null) throw tokenizer.getXMLScanException("picturebinding is null, cannot build pictureplanner ");
        planManager = new PlanManager<TimedPictureUnit>();
        PlanPlayer planPlayer = new SingleThreadedPlanPlayer<TimedPictureUnit>(are.getFeedbackManager(), planManager);
        player = new DefaultPlayer(planPlayer);
        PicturePlanner planner = new PicturePlanner(are.getFeedbackManager(), pictureBinding, planManager);
        engine = new DefaultEngine<TimedPictureUnit>(planner, player, planManager);
        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public PlanManager<TimedPictureUnit> getPlanManager()
    {
        return planManager;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newId)
    {
        id = newId;
    }
}
