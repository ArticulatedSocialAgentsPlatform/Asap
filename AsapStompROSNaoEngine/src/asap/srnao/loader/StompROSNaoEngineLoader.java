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
package asap.srnao.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.srnao.NaoPlanner;
import asap.srnao.naobinding.NaoBinding;
import asap.srnao.planunit.TimedNaoUnit;

/**

*/
public class StompROSNaoEngineLoader implements EngineLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private StompROSNaoEmbodiment ne = null;

    private Engine engine = null;
    private PlanManager<TimedNaoUnit> planManager = null;
    private Player player = null;
    private String id = "";
    // some variables cached during loading
    private NaoBinding naoBinding = null;
    private AsapRealizerEmbodiment are = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof StompROSNaoEmbodiment)
            {
                ne = (StompROSNaoEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) e.getEmbodiment();
            }
        }
        if (ne == null)
        {
            throw new RuntimeException("StompROSNaoEngineLoader requires an EmbodimentLoader containing a StompROSNaoEmbodiment");
        }
        if (are == null)
        {
            throw new RuntimeException("StompROSNaoEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        constructEngine(tokenizer);
    }

    @Override
    public void unload()
    {
        // engine.shutdown(); already done by realizer
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("StompROSNaoBinding"))
        {
            attrMap = tokenizer.getAttributes();
            naoBinding = new NaoBinding(ne.getEmbodiment());
            try
            {
                naoBinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException("Cannnot load StompROSNaoBinding: " + e);
            }
            tokenizer.takeEmptyElement("StompROSNaoBinding");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    private void constructEngine(XMLTokenizer tokenizer)
    {
        if (naoBinding == null) throw tokenizer.getXMLScanException("naobinding is null, cannot build naoplanner ");
        planManager = new PlanManager<TimedNaoUnit>();
        PlanPlayer planPlayer = new SingleThreadedPlanPlayer<TimedNaoUnit>(are.getFeedbackManager(), planManager);
        player = new DefaultPlayer(planPlayer);
        NaoPlanner planner = new NaoPlanner(are.getFeedbackManager(), naoBinding, planManager);
        engine = new DefaultEngine<TimedNaoUnit>(planner, player, planManager);
        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public PlanManager<TimedNaoUnit> getPlanManager()
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
