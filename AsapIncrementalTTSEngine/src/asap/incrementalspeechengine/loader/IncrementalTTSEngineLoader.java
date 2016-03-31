/*******************************************************************************
 *******************************************************************************/
package asap.incrementalspeechengine.loader;

import hmi.environmentbase.ConfigDirLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.PhonemeToVisemeMappingInfo;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import inpro.apps.SimpleMonitor;
import inpro.apps.util.MonitorCommandLineParser;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.synthesis.MaryAdapter;
import inpro.synthesis.MaryAdapter5internal;
import inpro.synthesis.hts.IUBasedFullPStream;
import inpro.synthesis.hts.VocodingAudioStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import marytts.util.data.audio.DDSAudioInputStream;
import asap.incrementalspeechengine.IncrementalTTSPlanner;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.incrementalspeechengine.PhraseIUManager;
import asap.incrementalspeechengine.SingleThreadedPlanPlayeriSS;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.planunit.PlanManager;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.realizerembodiments.IncrementalLipSynchProviderLoader;

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
    private PhonemeToVisemeMapping visemeMapping = new NullPhonemeToVisemeMapping();
    private Collection<IncrementalLipSynchProvider> lipSynchers = new ArrayList<IncrementalLipSynchProvider>();
    private String voicename = null;
    
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

    private static class DispatcherInfo extends XMLStructureAdapter
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

        for (IncrementalLipSynchProviderLoader el : ArrayUtils.getClassesOfType(requiredLoaders, IncrementalLipSynchProviderLoader.class))
        {
            lipSynchers.add(el.getLipSyncProvider());
        }

        DispatcherInfo di = null;
        ConfigDirLoader maryTTS = new ConfigDirLoader("MARYTTSIncremental", "MaryTTSIncremental")
        {
            public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
            {
                String language = this.getOptionalAttribute("language", attrMap);
                voicename = this.getOptionalAttribute("voicename", attrMap);
                if (language != null)
                {
                    System.setProperty("inpro.tts.language", language);
                }
                if (voicename != null)
                {
                    System.setProperty("inpro.tts.voice", voicename);
                }
                super.decodeAttributes(attrMap, tokenizer);
            }
        };

        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case PhonemeToVisemeMappingInfo.XMLTAG:
                PhonemeToVisemeMappingInfo info = new PhonemeToVisemeMappingInfo();
                info.readXML(tokenizer);
                visemeMapping = info.getMapping();
                break;
            case DispatcherInfo.XMLTAG:
                di = new DispatcherInfo();
                di.readXML(tokenizer);
                break;
            case "MaryTTSIncremental":
                maryTTS.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }
        if (di == null)
        {
            throw new RuntimeException("IncrementalTTSEngineLoader requires an Dispatcher specification");
        }
        System.setProperty("mary.base", maryTTS.getConfigDir());

        //heating up the tts so that subsequent speech is processed faster
        DispatchStream dummydispatcher = SimpleMonitor.setupDispatcher(new MonitorCommandLineParser(new String[] { "-D", "-c",
                "" + new Resources(di.getResource()).getURL(di.getFilename())}));
        List<IU> wordIUs = MaryAdapter.getInstance().text2IUs("Heating up.");
        //(source, new AudioFormat(16000.0F, 16, 1, true, false))
        dummydispatcher.playStream(new DDSAudioInputStream(new VocodingAudioStream(new IUBasedFullPStream(wordIUs.get(0)),
                MaryAdapter5internal.getDefaultHMMData(), true), new AudioFormat(16000.0F, 16, 1, true, false)), true);
        dummydispatcher.waitUntilDone();
        dummydispatcher.close();
        
        dispatcher = SimpleMonitor.setupDispatcher(new Resources(di.getResource()).getURL(di.getFilename()));
        PlanManager<IncrementalTTSUnit> planManager = new PlanManager<IncrementalTTSUnit>();
        IncrementalTTSPlanner planner = new IncrementalTTSPlanner(realizerEmbodiment.getFeedbackManager(), planManager,
                new PhraseIUManager(dispatcher, voicename, realizerEmbodiment.getBmlScheduler()), visemeMapping, lipSynchers);
        engine = new DefaultEngine<IncrementalTTSUnit>(planner, new DefaultPlayer(new SingleThreadedPlanPlayeriSS<IncrementalTTSUnit>(
                realizerEmbodiment.getFeedbackManager(), planManager)), planManager);
        engine.setId(id);
        MaryAdapter.getInstance();
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
