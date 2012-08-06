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
package asap.audioengine.loader;

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.audioengine.AudioPlanner;
import asap.audioengine.TimedAbstractAudioUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**

*/
public class AudioEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player audioPlayer = null;
    private PlanManager<TimedAbstractAudioUnit> audioPlanManager = null;
    private String id = "";
    // some variables cached during loading
    private AudioEnvironment aue = null;

    private AsapRealizerEmbodiment are = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (Loader e : requiredLoaders)
        {
          if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                  instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
        }
        if (are == null)
        {
            throw new RuntimeException("AudioEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }        
        for (Environment e : environments)
        {
            if (e instanceof AudioEnvironment) aue = (AudioEnvironment) e;
        }
        if (aue == null)
        {
            throw new RuntimeException("AudioEngineLoader requires an Environment of type AudioEnvironment");
        }
        audioPlanManager = new PlanManager<TimedAbstractAudioUnit>();
        audioPlayer = new DefaultPlayer(new MultiThreadedPlanPlayer<TimedAbstractAudioUnit>(are.getFeedbackManager(), audioPlanManager));
        AudioPlanner audioPlanner = new AudioPlanner(are.getFeedbackManager(), new Resources(""),
                audioPlanManager, aue.getSoundManager());
        engine = new DefaultEngine<TimedAbstractAudioUnit>(audioPlanner, audioPlayer, audioPlanManager);
        engine.setId(id);

        // add engine to realizer;
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

    public Player getAudioPlayer()
    {
        return audioPlayer;
    }

    public PlanManager<TimedAbstractAudioUnit> getPlanManager()
    {
        return audioPlanManager;
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
