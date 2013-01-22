package asap.incrementalspeechengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import asap.incrementalspeechengine.IncrementalTTSPlanner;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * Loads the IncrementalTTSEngine from XML
 * @author hvanwelbergen
 *
 */
@Slf4j
public class IncrementalTTSEngineLoader implements EngineLoader
{
    private Engine engine;
    private String id;
    private DispatchStream dispatcher;
    @Override
    public Engine getEngine()
    {
        return engine;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        AsapRealizerEmbodiment realizerEmbodiment = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if(realizerEmbodiment == null)
        {
            throw new RuntimeException("MixedAnimationEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");            
        }
        dispatcher = SimpleMonitor.setupDispatcher();
        PlanManager<IncrementalTTSUnit> planManager = new PlanManager<IncrementalTTSUnit>();
        IncrementalTTSPlanner planner = new IncrementalTTSPlanner(realizerEmbodiment.getFeedbackManager(),planManager, dispatcher);
        engine = new DefaultEngine<IncrementalTTSUnit>(planner,new DefaultPlayer(new SingleThreadedPlanPlayer<IncrementalTTSUnit>(planManager)),planManager);        
        engine.setId(id);
        realizerEmbodiment.addEngine(engine);
    }

    @Override
    public void unload()
    {
        try
        {
            dispatcher.close();
        }
        catch (IOException e)
        {
            log.warn("Error unloading: ",e);
        }
    }
}
