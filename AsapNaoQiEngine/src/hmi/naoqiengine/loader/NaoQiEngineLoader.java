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
package hmi.shaderengine.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import hmi.shaderengine.*;
import hmi.shaderengine.planunit.*;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

import hmi.renderenvironment.HmiRenderBodyEmbodiment;
/**

*/
public class ShaderEngineLoader implements EngineLoader
{
    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    private Engine engine = null;
    private PlanManager<TimedShaderUnit> planManager = null;
    private Player player = null;
    private String id = "";
    
    
    private AsapRealizerEmbodiment are = null;
    private HmiRenderBodyEmbodiment hrbe = null;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof HmiRenderBodyEmbodiment) hrbe = (HmiRenderBodyEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
        }
        if (are == null)
        {
            throw new RuntimeException("EmitterEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        if (hrbe == null)
        {
            throw new RuntimeException("ShaderEngineLoader requires an EmbodimentLoader containing a HmiRenderBodyEmbodiment");
        }		
        constructEngine(tokenizer);
    }

    @Override
    public void unload()
    {
        engine.shutdown();
    }


    /** tokenizer used for throwing scanexceptions */
    private void constructEngine(XMLTokenizer tokenizer)
    {
        planManager = new PlanManager<TimedShaderUnit>();
        PlanPlayer planPlayer = new SingleThreadedPlanPlayer<TimedShaderUnit>(are.getFeedbackManager(),
                planManager);
        player = new DefaultPlayer(planPlayer);
        ShaderPlanner planner = new ShaderPlanner(are.getFeedbackManager(), planManager, hrbe);
        engine = new DefaultEngine<TimedShaderUnit>(planner, player, planManager);
        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public PlanManager<TimedShaderUnit> getPlanManager()
    {
        return planManager;
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
