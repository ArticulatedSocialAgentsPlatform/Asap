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

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.EngineLoader;
import asap.environment.Loader;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.parametervaluechange.ParameterValueChangePlanner;
import asap.realizer.parametervaluechange.TimedParameterValueChangeUnit;
import asap.realizer.parametervaluechange.TrajectoryBinding;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.utils.Environment;

/**
 * NO XML. Default engine.
 */
public class ParameterValueChangeEngineLoader implements EngineLoader
{

    private Engine engine = null;
    private Player pvcPlayer = null;
    private PlanManager<TimedParameterValueChangeUnit> pvcPlanManager = null;
    private ParameterValueChangePlanner pvcPlanner = null;
    private String id = "";
    // some variables cached during loading
    private AsapVirtualHuman theVirtualHuman = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        id = newId;
        theVirtualHuman = avh;
        pvcPlanManager = new PlanManager<TimedParameterValueChangeUnit>();
        pvcPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedParameterValueChangeUnit>(theVirtualHuman.getElckerlycRealizer()
                .getFeedbackManager(), pvcPlanManager));
        pvcPlanner = new ParameterValueChangePlanner(theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), new TrajectoryBinding(),
                pvcPlanManager);
        pvcPlanner.setScheduler(theVirtualHuman.getBmlScheduler());
        engine = new DefaultEngine<TimedParameterValueChangeUnit>(pvcPlanner, pvcPlayer, pvcPlanManager);
        engine.setId(id);
        theVirtualHuman.getElckerlycRealizer().addEngine(engine);

    }

    @Override
    public void unload()
    {
        // engine.shutdown(); already done in scheduler
    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public Player getParameterValueChangePlayer()
    {
        return pvcPlayer;
    }

    public PlanManager<TimedParameterValueChangeUnit> getPlanManager()
    {
        return pvcPlanManager;
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
