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
import asap.realizer.activate.ActivatePlanner;
import asap.realizer.activate.TimedActivateUnit;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * NO XML. Default engine.
 */
public class ActivateEngineLoader implements EngineLoader
{
    private String id = "";
    private Engine engine = null;

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
            throw new RuntimeException("SpeechEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        PlanManager<TimedActivateUnit> planManager = new PlanManager<TimedActivateUnit>();
        DefaultPlayer player = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedActivateUnit>(are.getFeedbackManager(), planManager));
        ActivatePlanner planner = new ActivatePlanner(are.getFeedbackManager(), planManager);
        planner.setScheduler(are.getBmlScheduler());
        engine = new DefaultEngine<TimedActivateUnit>(planner, player, planManager);
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

    public String getId()
    {
        return id;
    }

    public void setId(String newId)
    {
        id = newId;
    }
}
