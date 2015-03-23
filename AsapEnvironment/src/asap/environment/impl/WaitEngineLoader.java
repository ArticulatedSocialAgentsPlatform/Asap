/*******************************************************************************
 *******************************************************************************/
package asap.environment.impl;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.wait.TimedWaitUnit;
import asap.realizer.wait.WaitPlanner;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * NO XML. Default engine.
 */
public class WaitEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player wPlayer = null;
    private PlanManager<TimedWaitUnit> wPlanManager = null;
    private WaitPlanner wPlanner = null;
    private String id = "";

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        AsapRealizerEmbodiment are = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        if (are == null)
        {
            throw new RuntimeException("WaitEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        wPlanManager = new PlanManager<TimedWaitUnit>();
        wPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedWaitUnit>(are.getFeedbackManager(), wPlanManager));
        wPlanner = new WaitPlanner(are.getFeedbackManager(), wPlanManager);
        engine = new DefaultEngine<TimedWaitUnit>(wPlanner, wPlayer, wPlanManager);
        engine.setId(id);
        are.addEngine(engine);

    }

    @Override
    public void unload()
    {
        // engine.shutdown();already done in scheduler...
    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public Player getWaitPlayer()
    {
        return wPlayer;
    }

    public PlanManager<TimedWaitUnit> getPlanManager()
    {
        return wPlanManager;
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
