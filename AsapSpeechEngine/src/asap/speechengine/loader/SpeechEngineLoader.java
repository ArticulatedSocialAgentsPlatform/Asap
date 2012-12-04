/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.speechengine.loader;

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.util.XMLPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.SpeechBehaviour;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Planner;
import asap.realizer.Player;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.realizerembodiments.JComponentEmbodiment;
import asap.realizerembodiments.LipSynchProviderLoader;
import asap.speechengine.DirectTTSUnitFactory;
import asap.speechengine.TTSPlanner;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.TimedTTSUnitFactory;
import asap.speechengine.WavTTSUnitFactory;
import asap.speechengine.ttsbinding.MaryTTSBinding;
import asap.speechengine.ttsbinding.SAPITTSBinding;
import asap.speechengine.ttsbinding.TTSBinding;

/**
 * Loads the a SpeechEngine
 * @author reidsma
 */
public class SpeechEngineLoader implements EngineLoader
{
    private static Logger logger = LoggerFactory.getLogger(SpeechEngineLoader.class.getName());

    private enum Voicetype
    {
        NOVOICE, SAPI5, MARY
    };

    private enum Factory
    {
        DIRECT_TTS, WAV_TTS
    };

    public SpeechEngineLoader()
    {
    }

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private JComponentEmbodiment jce = null;
    // private MixedAnimationEngineLoader ael = null;
    private AudioEnvironment aue = null;
    // private FaceEngineLoader fel = null;

    private JComboBox<String> voiceList = null;
    private JPanel speechUIPanel = null;
    private boolean initUI = false;

    private Engine engine = null;
    private PlanManager<TimedTTSUnit> speechPlanManager = null;
    private Player speechPlayer = null;
    private PlanPlayer speechPlanPlayer = null;
    private Planner<TimedTTSUnit> speechPlanner = null;
    // private MorphVisemeBinding visemebinding = null;
    private Voicetype voicetype = null;
    private String voicename = "";
    private Factory factory = null;
    String marydir = "MARYTTS";

    String id = "";

    private AsapRealizerEmbodiment are = null;

    private List<LipSynchProvider> lipSyncProviders = new ArrayList<LipSynchProvider>();

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof LipSynchProviderLoader) lipSyncProviders.add(((LipSynchProviderLoader) e).getLipSyncProvider());
            // if (e instanceof FaceEngineLoader) fel = (FaceEngineLoader) e;
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof JComponentEmbodiment) jce = (JComponentEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        for (Environment e : environments)
        {
            if (e instanceof AudioEnvironment) aue = (AudioEnvironment) e;
        }
        if (aue == null)
        {
            throw tokenizer.getXMLScanException("Speechengineloader requires audioenvironment.");
        }
        if (are == null)
        {
            throw new RuntimeException("SpeechEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        logger.debug("Reading SpeechEngine");
        while (!tokenizer.atETag("Loader"))
        {
            logger.debug("Reading Section");

            readSection(tokenizer);
        }
        constructEngine(tokenizer);

    }

    @Override
    public void unload()
    {
        // engine.shutdown();already done in scheduler...
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("Voice"))
        {
            logger.debug("Reading Voice");
            attrMap = tokenizer.getAttributes();
            voicename = "";
            String type = adapter.getRequiredAttribute("voicetype", attrMap, tokenizer);
            voicetype = Voicetype.NOVOICE;
            factory = Factory.DIRECT_TTS;
            if (type.equals("SAPI5"))
            {
                voicetype = Voicetype.SAPI5;
                voicename = adapter.getOptionalAttribute("voicename", attrMap, "");
                String factoryString = adapter.getOptionalAttribute("factory", attrMap, "WAV_TTS");
                if (factoryString.equals("DIRECT_TTS"))
                {
                    factory = Factory.DIRECT_TTS;
                }
                else if (factoryString.equals("WAV_TTS"))
                {
                    factory = Factory.WAV_TTS;
                }

            }
            else if (type.equals("MARY"))
            {
                voicetype = Voicetype.MARY;
                voicename = adapter.getOptionalAttribute("voicename", attrMap, "");
                String factoryString = adapter.getOptionalAttribute("factory", attrMap, "WAV_TTS");
                if (factoryString.equals("DIRECT_TTS"))
                {
                    factory = Factory.DIRECT_TTS;
                }
                else if (factoryString.equals("WAV_TTS"))
                {
                    factory = Factory.WAV_TTS;
                }
                String localMaryDir = adapter.getOptionalAttribute("localmarydir", attrMap);
                marydir = adapter.getOptionalAttribute("marydir", attrMap);
                if (marydir == null)
                {
                    if (localMaryDir == null)
                    {
                        throw tokenizer.getXMLScanException("neither marydir nor localmarydir specified.");
                    }
                    String spr = System.getProperty("shared.project.root");
                    if (spr == null)
                    {
                        throw tokenizer.getXMLScanException("the use of the localmarydir setting "
                                + "requires a shared.project.root system variable (often: -Dshared.project.root=\"../..\" "
                                + "but this may depend on your system setup).");
                    }
                    marydir = System.getProperty("shared.project.root") + "/" + localMaryDir;
                }
            }
            tokenizer.takeEmptyElement("Voice");
        }
        else if (tokenizer.atSTag("SpeechUI"))
        {
            if (jce == null) throw tokenizer.getXMLScanException("Cannot add SpeechUI when no JComponentEmbodiment is set");
            initUI = true;
            tokenizer.takeSTag("SpeechUI");
            tokenizer.takeETag("SpeechUI");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** tokenizer used for throwing scanexceptions */
    private void constructEngine(XMLTokenizer tokenizer)
    {

        speechPlanManager = new PlanManager<TimedTTSUnit>();
        speechPlanPlayer = new MultiThreadedPlanPlayer<TimedTTSUnit>(are.getFeedbackManager(), speechPlanManager);
        speechPlayer = new DefaultPlayer(speechPlanPlayer);
        speechPlanner = null;

        switch (voicetype)
        {
        case MARY:
        {
            TTSBinding ttsBin;
            try
            {
                XMLPhonemeToVisemeMapping vm = new XMLPhonemeToVisemeMapping();
                vm.readXML(new Resources("Humanoids/shared/phoneme2viseme/").getReader("sampade2ikp.xml"));
                ttsBin = new MaryTTSBinding(marydir, vm);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            if (voicename != null)
            {
                ttsBin.setVoice(voicename);
            }
            // ttsBin.speak(SpeechBehaviour.class, ""); // HACK HACK: speak a sentence to counter delays
            speechPlanner = getTTSPlanner(ttsBin);
            break;
        }
        case SAPI5:
        {
            TTSBinding ttsBin = new SAPITTSBinding();
            if (voicename != null)
            {
                ttsBin.setVoice(voicename);
            }
            else
            {
                ttsBin.setVoice(ttsBin.getVoices()[0]);
            }
            ttsBin.speak(SpeechBehaviour.class, ""); // HACK HACK: speak a sentence to counter delays
            speechPlanner = getTTSPlanner(ttsBin);
            break;
        }
        default:
        {
            logger.warn("cannot initialize this voice, wrong type.");
            return;
        }
        }
        engine = new DefaultEngine<TimedTTSUnit>(speechPlanner, speechPlayer, speechPlanManager);

        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

        // init ui?
        if (initUI)
        {
            if (voicetype != Voicetype.NOVOICE)
            {
                final TTSPlanner ttsp = (TTSPlanner) speechPlanner;
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        voiceList = new JComboBox<String>(ttsp.getVoices());
                        voiceList.setEditable(false);
                        voiceList.setSelectedItem(voicename);
                        voiceList.addActionListener(new VoiceSelectionListener());
                        speechUIPanel = new JPanel();
                        speechUIPanel.add(new JLabel("Voice:"));
                        speechUIPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                        speechUIPanel.add(voiceList);
                        jce.addJComponent(speechUIPanel);
                    }
                });
            }
        }

    }

    private TTSPlanner getTTSPlanner(TTSBinding ttsBin)
    {
        TimedTTSUnitFactory ttsFactory = null;
        switch (factory)
        {
        case DIRECT_TTS:
            ttsFactory = new DirectTTSUnitFactory(are.getFeedbackManager());
            break;
        case WAV_TTS:
            ttsFactory = new WavTTSUnitFactory(are.getFeedbackManager(), aue.getSoundManager());
            break;
        default:
            System.err.println("cannot initialize this factory, wrong type.");
            return null;
        }
        TTSPlanner ttsPlanner = new TTSPlanner(are.getFeedbackManager(), ttsFactory, ttsBin, speechPlanManager);
        for (LipSynchProvider lsp : lipSyncProviders)
        {
            ttsPlanner.addLipSyncher(lsp);
        }
        return ttsPlanner;
    }

    class VoiceSelectionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (voicetype)
            {
            case NOVOICE:
                return;
            case MARY:
            case SAPI5:
                if (speechPlanner instanceof TTSPlanner)
                {
                    ((TTSPlanner) speechPlanner).setSpeaker((String) voiceList.getSelectedItem());
                }
                break;
            default:
                System.err.println("cannot initialize this voice, wrong type");
                return;
            }
        }
    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public Player getPlayer()
    {
        return speechPlayer;
    }

    public Planner<TimedTTSUnit> getSpeechPlanner()
    {
        return speechPlanner;
    }

    public PlanManager<TimedTTSUnit> getPlanManager()
    {
        return speechPlanManager;
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
