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
import hmi.elckerlyc.interrupt.InterruptPlanner;
import hmi.elckerlyc.interrupt.TimedInterruptUnit;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.SingleThreadedPlanPlayer;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.EngineLoader;
import asap.environment.Loader;
import asap.utils.Environment;

/**
 * NO XML. Default engine.
 */
public class InterruptEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player iPlayer = null;
    private PlanManager<TimedInterruptUnit> iPlanManager = null;
    private InterruptPlanner iPlanner = null;
    private String id = "";
    // some variables cached during loading
    private AsapVirtualHuman theVirtualHuman = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        id = newId;
        theVirtualHuman = avh;
        iPlanManager = new PlanManager<TimedInterruptUnit>();
        iPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedInterruptUnit>(theVirtualHuman.getElckerlycRealizer()
                .getFeedbackManager(), iPlanManager));
        iPlanner = new InterruptPlanner(theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), iPlanManager);
        iPlanner.setScheduler(theVirtualHuman.getBmlScheduler());
        engine = new DefaultEngine<TimedInterruptUnit>(iPlanner, iPlayer, iPlanManager);
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

    public Player getParameterValueChangePlayer()
    {
        return iPlayer;
    }

    public PlanManager<TimedInterruptUnit> getPlanManager()
    {
        return iPlanManager;
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
