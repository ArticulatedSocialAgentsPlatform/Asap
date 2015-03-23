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
import asap.realizer.interrupt.InterruptPlanner;
import asap.realizer.interrupt.TimedInterruptUnit;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * NO XML. Default engine.
 */
public class InterruptEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player iPlayer = null;
    private PlanManager<TimedInterruptUnit> iPlanManager = null;
    private InterruptPlanner iPlanner = null;
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
            throw new RuntimeException("SpeechEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        iPlanManager = new PlanManager<TimedInterruptUnit>();
        iPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedInterruptUnit>(are.getFeedbackManager(), iPlanManager));
        iPlanner = new InterruptPlanner(are.getFeedbackManager(), iPlanManager);
        iPlanner.setScheduler(are.getBmlScheduler());
        engine = new DefaultEngine<TimedInterruptUnit>(iPlanner, iPlayer, iPlanManager);
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

    public Player getParameterValueChangePlayer()
    {
        return iPlayer;
    }

    public PlanManager<TimedInterruptUnit> getPlanManager()
    {
        return iPlanManager;
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
