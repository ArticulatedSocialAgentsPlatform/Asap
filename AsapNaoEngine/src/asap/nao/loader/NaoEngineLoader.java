/*******************************************************************************
 *******************************************************************************/
package asap.nao.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.nao.NaoPlanner;
import asap.nao.naobinding.NaoBinding;
import asap.nao.planunit.TimedNaoUnit;
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
public class NaoEngineLoader implements EngineLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private NaoEmbodiment ne = null;

    private Engine engine = null;
    private NaoPlanner planner = null;
    private PlanManager<TimedNaoUnit> planManager = null;
    private Player player = null;
    private String id = "";
    // some variables cached during loading
    private NaoBinding naobinding = null;

    private AsapRealizerEmbodiment are = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof NaoEmbodiment)
            {
                ne = (NaoEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
        }
        if (ne == null)
        {
            throw new RuntimeException("NaoEngineLoader requires an EmbodimentLoader containing a NaoEmbodiment");
        }
        if (are == null)
        {
            throw new RuntimeException("NaoEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
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
        engine.shutdown();
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("NaoBinding"))
        {
            attrMap = tokenizer.getAttributes();
            naobinding = new NaoBinding();
            try
            {
                naobinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Cannnot load NaoBinding: " + e);
            }
            tokenizer.takeEmptyElement("NaoBinding");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** tokenizer used for throwing scanexceptions */
    private void constructEngine(XMLTokenizer tokenizer)
    {
        if (naobinding == null) throw tokenizer.getXMLScanException("naobinding is null, cannot build naoplanner ");
        planManager = new PlanManager<TimedNaoUnit>();
        PlanPlayer planPlayer = new SingleThreadedPlanPlayer<TimedNaoUnit>(are.getFeedbackManager(), planManager);
        player = new DefaultPlayer(planPlayer);
        planner = new NaoPlanner(are.getFeedbackManager(), ne.getNao(), naobinding, planManager);
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
