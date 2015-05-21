/*******************************************************************************
 *******************************************************************************/
package asap.srnao.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.srnao.PicturePlanner;
import asap.srnao.naobinding.NaoBinding;
import asap.srnao.planunit.TimedNaoUnit;

/**

*/
public class PictureEngineLoader implements EngineLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private StompROSNaoEmbodiment pe = null;

    private Engine engine = null;
    private PlanManager<TimedNaoUnit> planManager = null;
    private Player player = null;
    private String id = "";
    // some variables cached during loading
    private NaoBinding pictureBinding = null;
    private AsapRealizerEmbodiment are = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof StompROSNaoEmbodiment)
            {
                pe = (StompROSNaoEmbodiment) e.getEmbodiment();
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
            pictureBinding = new NaoBinding(pe.getPictureDisplay());
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
        planManager = new PlanManager<TimedNaoUnit>();
        PlanPlayer planPlayer = new SingleThreadedPlanPlayer<TimedNaoUnit>(are.getFeedbackManager(), planManager);
        player = new DefaultPlayer(planPlayer);
        PicturePlanner planner = new PicturePlanner(are.getFeedbackManager(), pictureBinding, planManager);
        engine = new DefaultEngine<TimedNaoUnit>(planner, player, planManager);
        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public PlanManager<TimedNaoUnit> getPlanManager()
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
