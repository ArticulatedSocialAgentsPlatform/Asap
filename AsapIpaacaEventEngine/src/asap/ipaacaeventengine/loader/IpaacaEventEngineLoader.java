package asap.ipaacaeventengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import asap.ipaacaeventengine.IpaacaEventPlanner;
import asap.ipaacaeventengine.MessageManager;
import asap.ipaacaeventengine.TimedIpaacaMessageUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * Loads the IpaacaEventEngine
 * @author hvanwelbergen
 *
 */
public class IpaacaEventEngineLoader implements EngineLoader
{
    @Getter
    private String id;
    private Engine engine = null;
    private MessageManager messageManager;
    static
    {
        ipaaca.Initializer.initializeIpaacaRsb();
    }
    
    public IpaacaEventEngineLoader()
    {

    }
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
        messageManager = new MessageManager(id);
        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if (are == null)
        {
            throw new RuntimeException("IpaacaEventEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        
        PlanManager<TimedIpaacaMessageUnit> planManager = new PlanManager<>();
        SingleThreadedPlanPlayer<TimedIpaacaMessageUnit> pp = new SingleThreadedPlanPlayer<>(are.getFeedbackManager(), planManager);
        IpaacaEventPlanner planner = new IpaacaEventPlanner(are.getFeedbackManager(), planManager, messageManager);
        engine = new DefaultEngine<TimedIpaacaMessageUnit>(planner,new DefaultPlayer(pp),new PlanManager<TimedIpaacaMessageUnit>());
        engine.setId(id);
        are.addEngine(engine);
    }
    
    @Override
    public Engine getEngine()
    {
        return engine;
    }    

    @Override
    public void unload()
    {
        messageManager.close();
    }
}
