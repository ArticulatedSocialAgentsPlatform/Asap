package asap.incrementalspeechengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
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

    private class DispatcherInfo extends XMLStructureAdapter
    {
        @Getter
        private String filename;

        @Getter
        private String resource;

        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            filename = getRequiredAttribute("filename", attrMap, tokenizer);
            resource = getRequiredAttribute("resources", attrMap, tokenizer);
        }

        public String getXMLTag()
        {
            return XMLTAG;
        }

        private static final String XMLTAG = "Dispatcher";
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        AsapRealizerEmbodiment realizerEmbodiment = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if (realizerEmbodiment == null)
        {
            throw new RuntimeException("IncrementalTTSEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }

        DispatcherInfo di = null;
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case DispatcherInfo.XMLTAG:
                di = new DispatcherInfo();
                di.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }
        if (di == null)
        {
            throw new RuntimeException("IncrementalTTSEngineLoader requires an Dispatcher specification");
        }

        dispatcher = SimpleMonitor.setupDispatcher(new Resources(di.getResource()).getURL(di.getFilename()));
        PlanManager<IncrementalTTSUnit> planManager = new PlanManager<IncrementalTTSUnit>();
        IncrementalTTSPlanner planner = new IncrementalTTSPlanner(realizerEmbodiment.getFeedbackManager(), planManager, dispatcher);
        engine = new DefaultEngine<IncrementalTTSUnit>(planner, new DefaultPlayer(new SingleThreadedPlanPlayer<IncrementalTTSUnit>(
                planManager)), planManager);
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
            log.warn("Error unloading: ", e);
        }
    }
}
