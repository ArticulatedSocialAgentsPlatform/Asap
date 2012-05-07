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
package asap.environment.impl;

import hmi.elckerlyc.DefaultEngine;
import hmi.elckerlyc.DefaultPlayer;
import hmi.elckerlyc.Engine;
import hmi.elckerlyc.Player;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.elckerlyc.wait.TimedWaitUnit;
import hmi.elckerlyc.wait.WaitPlanner;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.EngineLoader;
import asap.environment.Loader;
import asap.utils.Environment;

/**
 * NO XML. Default engine.
 */
public class WaitEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player wPlayer = null;
    private PlanManager<TimedWaitUnit> wPlanManager = null;
    private WaitPlanner wPlanner = null;
    private String id = "";
    // some variables cached during loading
    private AsapVirtualHuman theVirtualHuman = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        id = newId;
        theVirtualHuman = avh;
        wPlanManager = new PlanManager<TimedWaitUnit>();
        wPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedWaitUnit>(
                theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), wPlanManager));
        wPlanner = new WaitPlanner(theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), wPlanManager);
        engine = new DefaultEngine<TimedWaitUnit>(wPlanner, wPlayer, wPlanManager);
        engine.setId(id);
        theVirtualHuman.getElckerlycRealizer().addEngine(engine);

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

    public Player getWaitPlayer()
    {
        return wPlayer;
    }

    public PlanManager<TimedWaitUnit> getPlanManager()
    {
        return wPlanManager;
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
