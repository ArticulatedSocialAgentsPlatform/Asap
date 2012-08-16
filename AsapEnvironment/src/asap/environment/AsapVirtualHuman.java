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
package asap.environment;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.Clock;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import asap.bml.bridge.RealizerPort;
import asap.environment.impl.ActivateEngineLoader;
import asap.environment.impl.InterruptEngineLoader;
import asap.environment.impl.ParameterValueChangeEngineLoader;
import asap.environment.impl.WaitEngineLoader;
import asap.realizer.Engine;
import asap.realizer.pegboard.PegBoard;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;
import asap.realizerembodiments.SchedulingClockEmbodiment;

/**
 * Loads and unloads an AsapVirtualHuman and provides access to its elements (realizer, embodiments, engines, etc)
 */
public class AsapVirtualHuman 
{
    private Logger logger = LoggerFactory.getLogger(AsapVirtualHuman.class.getName());

    /** "human readable name" */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String name = "<no name>";

    /* Unique ID for this virtual human */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String vhId = "";

    /** All engines for this virtualhuman -- these need to be "played" on the main AsapEnvironment loop */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private ArrayList<Engine> engines = new ArrayList<Engine>();

    /** used for loading the virtual human from an XML specification file */
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    /** used for loading the virtual human from an XML specification file */
    private HashMap<String, String> attrMap = null;
    /** used for loading the virtual human from an XML specification file */
    private XMLTokenizer theTokenizer = null;

    /** All loaders (embodiments and engines) that have been loaded (and should be unloaded again) */
    @Getter
    private HashMap<String, Loader> loaders = new HashMap<String, Loader>();

    /** Needed to add any loaded engines to the environment */
    private AsapEnvironment ae = null;

    private AsapRealizerEmbodiment are = null;

    /** Needed during the loading process in order to offer all other loaders access to the full list of available environments */
    private Environment[] allEnvironments = new Environment[0];

    /**
     * Needed during the loading process in order to offer all other loaders access to the scheduling clock during load
     * (Some engines, such as interrupt engine and
     * paramvalchange, need access to such internals)
     */
    private Clock theSchedulingClock = null;

    /** Unload: remove the virtual human from the AsapEnvironment; stop scheduler etc; onload all other loaders */
    public void unload()
    {
        ae.removeVirtualHuman(this);

        for (Entry<String, Loader> loader : loaders.entrySet())
        {
            loader.getValue().unload();
        }
    }

    public PegBoard getPegBoard()
    {
        return are.getPegBoard();
    }
    
    public void load(String resources, String filename, String name, Environment[] environments, Clock sc) throws IOException
    {
        load(XMLTokenizer.forResource(resources, filename), name, environments, sc);
    }

    public void load(XMLTokenizer tokenizer, String name, Environment[] environments, Clock sc) throws IOException
    {
        for (Environment e : environments)
        {
            if (e instanceof AsapEnvironment) ae = (AsapEnvironment) e;
        }
        if (ae == null)
        {
            throw new RuntimeException("AsapVirtualHuman requires an AsapEnvironment when loading");
        }

        theTokenizer = tokenizer;
        allEnvironments = environments;
        theSchedulingClock = sc;
        this.name = name;

        try
        {
            theTokenizer.takeSTag("AsapVirtualHuman");

            Loader loader = null;
            
            loader = new EmbodimentLoader()
            {
            	public String getId(){return "schedulingclockembodimentloader";}
            	public Embodiment getEmbodiment()
            	{
            		return new SchedulingClockEmbodiment()
            		{
            			public String getId(){return "schedulingclockembodiment";}
                        public Clock getSchedulingClock() {
                    		return theSchedulingClock;
                    	}
            		};
            	}
				public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader... requiredLoaders) throws IOException {}
				public void unload() {}
            };
            loaders.put("schedulingclockembodimentloader", loader);
            Loader sel = loader;
            // ================= REALIZER (ALWAYS FIRST) =====================

        	//is at loader? type must be realizerloader!
            if (theTokenizer.atSTag("Loader"))
            {
                attrMap = theTokenizer.getAttributes();
                String id = adapter.getRequiredAttribute("id", attrMap, theTokenizer);
                String loaderClass = adapter.getRequiredAttribute("loader", attrMap, theTokenizer);
                String requiredLoaderIds = adapter.getOptionalAttribute("requiredloaders", attrMap, "");
                try
                {
                    loader = (Loader) Class.forName(loaderClass).newInstance();
                }
                catch (InstantiationException e)
                {
                    throw theTokenizer.getXMLScanException("InstantiationException while starting Loader " + loaderClass);
                }
                catch (IllegalAccessException e)
                {
                    throw theTokenizer.getXMLScanException("IllegalAccessException while starting Loader " + loaderClass);
                }
                catch (ClassNotFoundException e)
                {
                    throw theTokenizer.getXMLScanException("ClassNotFoundException while starting Loader " + loaderClass);
                }
                catch (ClassCastException e)
                {
                    throw theTokenizer.getXMLScanException("ClassCastException while starting Loader " + loaderClass);
                }

                if (!(loader instanceof AsapRealizerEmbodiment))
                {
                	logger.error("AsapVirtualHuman: The first loader *must* be a AsapRealizerEmbodiment");
                	throw new RuntimeException("AsapVirtualHuman: The first loader *must* be a AsapRealizerEmbodiment");
                }
                
                if (!requiredLoaderIds.equals(""))
                {
                	logger.error("AsapVirtualHuman: no required loaders allowed for AsapRealizerEmbodiment");
                	throw new RuntimeException("AsapVirtualHuman: no required loaders allowed for AsapRealizerEmbodiment");
                }
                are = (AsapRealizerEmbodiment)loader;
                theTokenizer.takeSTag("Loader");
                logger.debug("Parsing Loader: {}", id);
                loader.readXML(theTokenizer, id, vhId, name, allEnvironments, new Loader[]{sel});
                theTokenizer.takeETag("Loader");
                loaders.put(id, loader);
            }
	
            // ================= LOADERS (embodiment&engine) =====================
            while (theTokenizer.atSTag("Loader"))
            {
                attrMap = theTokenizer.getAttributes();
                String id = adapter.getRequiredAttribute("id", attrMap, theTokenizer);
                String loaderClass = adapter.getRequiredAttribute("loader", attrMap, theTokenizer);
                String requiredLoaderIds = adapter.getOptionalAttribute("requiredloaders", attrMap, "");
                ArrayList<Loader> requiredLoaders = new ArrayList<Loader>();
                if (!requiredLoaderIds.equals(""))
                {
                    for (String reqId : requiredLoaderIds.split(","))
                    {
                        if (!reqId.equals("") && loaders.get(reqId) == null) throw theTokenizer
                                .getXMLScanException("Required loader not present: " + reqId);
                        requiredLoaders.add(loaders.get(reqId));
                    }
                }
                //always add "are" -- just in case someone forgets
                requiredLoaders.add(are);
                try
                {
                    loader = (Loader) Class.forName(loaderClass).newInstance();
                }
                catch (InstantiationException e)
                {
                    throw theTokenizer.getXMLScanException("InstantiationException while starting Loader " + loaderClass);
                }
                catch (IllegalAccessException e)
                {
                    throw theTokenizer.getXMLScanException("IllegalAccessException while starting Loader " + loaderClass);
                }
                catch (ClassNotFoundException e)
                {
                    throw theTokenizer.getXMLScanException("ClassNotFoundException while starting Loader " + loaderClass);
                }
                catch (ClassCastException e)
                {
                    throw theTokenizer.getXMLScanException("ClassCastException while starting Loader " + loaderClass);
                }
                theTokenizer.takeSTag("Loader");
                logger.debug("Parsing Loader: {}", id);
                loader.readXML(theTokenizer, id, vhId, name, allEnvironments, requiredLoaders.toArray(new Loader[0]));
                theTokenizer.takeETag("Loader");
                loaders.put(id, loader);
                if (loader instanceof EngineLoader)
                {
                    logger.info("Adding engine {}",loader.getId());
                    engines.add(((EngineLoader) loader).getEngine());
                }
            }

            // ================= BML BEHAVIOUR ROUTING =====================

            if (theTokenizer.atSTag("BMLRouting"))
            {
                loadBMLRoutingSection();
            }

            theTokenizer.takeETag("AsapVirtualHuman");

            // ================= DEFAULT ENGINES =====================
            String id = "waitengine";
            loader = new WaitEngineLoader();
            loader.readXML(null, id, vhId, name, allEnvironments, new Loader[]{are});
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "parametervaluechangeengine";
            loader = new ParameterValueChangeEngineLoader();
            loader.readXML(null, id, vhId, name, allEnvironments, new Loader[]{are});
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "activateengine";
            loader = new ActivateEngineLoader();
            loader.readXML(null, id, vhId, name, allEnvironments, new Loader[]{are});
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "interruptengine";
            loader = new InterruptEngineLoader();
            loader.readXML(null, id, vhId, name, allEnvironments, new Loader[]{are});
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            // ================= ADD TO ENVIRONMENT =====================

            ae.addVirtualHuman(this);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

    }



    private void loadBMLRoutingSection() throws IOException
    {
        attrMap = theTokenizer.getAttributes();

        theTokenizer.takeSTag("BMLRouting");
        while (theTokenizer.atSTag("Route"))
        {
            // read route spec
            attrMap = theTokenizer.getAttributes();
            String behaviorClassName = adapter.getRequiredAttribute("behaviourclass", attrMap, theTokenizer);
            String engineId = adapter.getRequiredAttribute("engineid", attrMap, theTokenizer);
            // find engine
            Engine engine = null;
            for (Engine e : engines)
            {
                if (e.getId().equals(engineId))
                {
                    engine = e;
                    break;
                }
            }
            if (engine == null)
            {
                logger.error("Cannot find engine with id \"{}\"", engineId);
            }
            else
            {
                // find bml behavior class
                try
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends Behaviour> behaviorClass = (Class<? extends Behaviour>) Class.forName(behaviorClassName);
                    if (Behaviour.class.isAssignableFrom(behaviorClass))
                    {
                        are.addEngine(behaviorClass, engine);
                    }
                    else
                    {
                        logger.error("The class \"{}\" is not a bml Behaviour class", behaviorClassName);
                    }
                }
                catch (Exception e)
                {
                    logger.error("Cannot find behaviorclass \"{}\"", behaviorClassName);
                    logger.debug("Exception: ", e);
                }
            }
            theTokenizer.takeSTag("Route");
            theTokenizer.takeETag("Route");

        }
        theTokenizer.takeETag("BMLRouting");

    }

    public RealizerPort getRealizerPort()
    {
    	return are.getRealizerPort();
    }
}
