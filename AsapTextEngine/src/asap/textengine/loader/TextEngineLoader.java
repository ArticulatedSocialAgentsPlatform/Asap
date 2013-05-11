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
package asap.textengine.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.textembodiments.TextEmbodiment;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Planner;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.textengine.EmbodimentTextOutput;
import asap.textengine.TextOutput;
import asap.textengine.TextPlanner;
import asap.textengine.TimedSpeechTextUnit;

/**
 * Handles the construction of a TextEngine through its XML specification.
 */
public class TextEngineLoader implements EngineLoader
{
    private TextEmbodiment te = null;

    private Engine engine = null;
    private PlanManager<TimedSpeechTextUnit> textPlanManager = null;
    private Player textPlayer = null;
    private PlanPlayer textPlanPlayer = null;
    private Planner<TimedSpeechTextUnit> textPlanner = null;
    private TextOutput textOutput = null;

    String id = "";

    private AsapRealizerEmbodiment are = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof TextEmbodiment)
            {
                te = (TextEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) e.getEmbodiment();
            }
        }
        if (are == null)
        {
            throw new RuntimeException("TextEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        if (te == null)
        {
            throw new RuntimeException("TextSpeechEngineLoader requires an Embodiment of type TextEmbodiment");
        }

        textPlanManager = new PlanManager<TimedSpeechTextUnit>();
        textPlanPlayer = new SingleThreadedPlanPlayer<TimedSpeechTextUnit>(are.getFeedbackManager(), textPlanManager);
        textPlayer = new DefaultPlayer(textPlanPlayer);
        textOutput = new EmbodimentTextOutput(te);
        textPlanner = new TextPlanner(are.getFeedbackManager(), textOutput, textPlanManager);

        engine = new DefaultEngine<TimedSpeechTextUnit>(textPlanner, textPlayer, textPlanManager);

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

    public Player getPlayer()
    {
        return textPlayer;
    }

    public Planner<TimedSpeechTextUnit> getSpeechPlanner()
    {
        return textPlanner;
    }

    public PlanManager<TimedSpeechTextUnit> getPlanManager()
    {
        return textPlanManager;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newId)
    {
        id = newId;
    }

    public TextOutput getTextOutput()
    {
        return textOutput;
    }
}
