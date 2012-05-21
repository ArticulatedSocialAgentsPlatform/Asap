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

import asap.bml.bridge.RealizerPort;
import asap.bml.bridge.TCPIPToBMLRealizerAdapter;
import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTBMLBehaviorAttributes;
import saiba.bml.parser.BMLParser;
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

import asap.environment.impl.ActivateEngineLoader;
import asap.environment.impl.InterruptEngineLoader;
import asap.environment.impl.ParameterValueChangeEngineLoader;
import asap.environment.impl.WaitEngineLoader;
import asap.realizer.AsapRealizer;
import asap.realizer.Engine;
import asap.realizer.bridge.LogPipe;
import asap.realizer.bridge.MultiThreadedElckerlycRealizerBridge;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.BMLTSchedulingHandler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;
import asap.utils.Environment;
import asap.utils.SchedulingClock;

import com.google.common.collect.ImmutableSet;

/**
 * Loads and unloads an AsapVirtualHuman and provides access to its elements (realizer, embodiments, engines, etc)
 */
public class AsapVirtualHuman
{
    private Logger logger = LoggerFactory.getLogger(AsapVirtualHuman.class.getName());

    /**
     * The elckerlycRealizer is exposed to facilitate access to some advanced capabilities.
     * Generally you should not use this variable, there is a big chance that acess to it
     * will be removed in the future
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private AsapRealizer elckerlycRealizer = null;

    /** Use the RealizerPort to send BML to the Realizer */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private RealizerPort realizerPort = null;

    /**
     * This is the server providing access to the realizer through TCPIP.
     * Acces this variable to start/stop the server, and to create a GUI for it.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private TCPIPToBMLRealizerAdapter tcpipToBMLRealizerAdapter = null;

    /** Some engines (interrupt engine, paramvalchange) need access to the scheduler and other internals */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private BMLScheduler bmlScheduler = null;

    /** "human readable name" */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String name = "<no name>";

    /* Unique ID for this virtual human */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String id = "";

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

    /** All other loaders (embodiments and engines) that have been loaded (and should be unloaded again) */
    @Getter
    private HashMap<String, Loader> loaders = new HashMap<String, Loader>();

    /** Needed to add any loaded engines to the environment */
    private AsapEnvironment ae = null;

    /** Needed during the loading process in order to offer all other loaders access to the full list of available environments */
    private Environment[] allEnvironments = new Environment[0];

    /**
     * Needed during the loading process in order to offer all other loaders access to the scheduling clock during load
     * (Some engines, such as interrupt engine and
     * paramvalchange, need access to such internals)
     */
    private SchedulingClock theSchedulingClock = null;

    @Getter
    private final PegBoard pegBoard = new PegBoard();

    /** Unload: remove the virtual human from the AsapEnvironment; stop scheduler etc; onload all other loaders */
    public void unload()
    {
        ae.removeVirtualHuman(this);

        if (tcpipToBMLRealizerAdapter != null)
        {
            tcpipToBMLRealizerAdapter.shutdown();
            logger.debug("Attempting to shutdown server...");
        }

        elckerlycRealizer.shutdown(); // can you do this before all engines and emitters have been shut down?

        for (Entry<String, Loader> loader : loaders.entrySet())
        {
            loader.getValue().unload();
        }
    }

    public void load(String resources, String filename, String name, Environment[] environments, SchedulingClock sc) throws IOException
    {
        load(XMLTokenizer.forResource(resources, filename), name, environments, sc);
    }

    public void load(XMLTokenizer tokenizer, String name, Environment[] environments, SchedulingClock sc) throws IOException
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

            // ================= REALIZER =====================

            loadBMLRealizerSection();

            // ================= LOADERS (embodiment&engine) =====================
            Loader loader = null;
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
                loader.readXML(theTokenizer, id, this, allEnvironments, requiredLoaders.toArray(new Loader[0]));
                theTokenizer.takeETag("Loader");
                loaders.put(id, loader);
                if (loader instanceof EngineLoader)
                {
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
            loader.readXML(null, id, this, allEnvironments, new Loader[0]);
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "parametervaluechangeengine";
            loader = new ParameterValueChangeEngineLoader();
            loader.readXML(null, id, this, allEnvironments, new Loader[0]);
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "activateengine";
            loader = new ActivateEngineLoader();
            loader.readXML(null, id, this, allEnvironments, new Loader[0]);
            loaders.put(id, loader);
            engines.add(((EngineLoader) loader).getEngine());

            id = "interruptengine";
            loader = new InterruptEngineLoader();
            loader.readXML(null, id, this, allEnvironments, new Loader[0]);
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

    /** Construct a reaslizer setup from the XML specification within the <BMLRealizer> tag */
    public void loadBMLRealizerSection() throws IOException
    {
        attrMap = theTokenizer.getAttributes();

        theTokenizer.takeSTag("BMLRealizer");

        // ====== load BML parsing and scheduling stuff

        BMLParser parser = readParserSection();

        // FIXME assumes that the XML element starts with the scheduler information
        BMLBlockManager bmlBlockManager = new BMLBlockManager();
        FeedbackManager feedbackManager = new FeedbackManagerImpl(bmlBlockManager, name);
        bmlScheduler = readSchedulerSection(bmlBlockManager, parser, feedbackManager);

        // ========= construct realizer and realizerport

        elckerlycRealizer = new AsapRealizer(parser, feedbackManager, theSchedulingClock, bmlScheduler);

        //we can't ensure that the port will only be called singlethreaded
        realizerPort = new MultiThreadedElckerlycRealizerBridge(elckerlycRealizer); 
        

        // ====== subsequently, read in XML all requests for pipes, ports and adapters, and create / insert / attach them.

        while (!theTokenizer.atETag("BMLRealizer"))
        {
            readBMLRealizerSubsection();
        }

        theTokenizer.takeETag("BMLRealizer");

    }

    protected BMLParser readParserSection() throws IOException
    {
        BMLParser parser = new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().add(
                BMLTBMLBehaviorAttributes.class).build());
        if (theTokenizer.atSTag("BMLParser"))
        {
            BMLParserAssembler assembler = new BMLParserAssembler();
            assembler.readXML(theTokenizer);
            parser = assembler.getBMLParser();
        }
        return parser;
    }

    protected BMLScheduler readSchedulerSection(BMLBlockManager bmlBlockManager, BMLParser parser, FeedbackManager feedbackManager)
            throws IOException
    {

        BMLScheduler scheduler;

        if (theTokenizer.atSTag("BMLScheduler"))
        {
            BMLSchedulerAssembler assembler = new BMLSchedulerAssembler(name, parser, feedbackManager, bmlBlockManager, theSchedulingClock,
                    pegBoard);
            assembler.readXML(theTokenizer);
            scheduler = assembler.getBMLScheduler();
        }
        else
        {
            scheduler = new BMLScheduler(name, parser, feedbackManager, theSchedulingClock, new BMLTSchedulingHandler(
                    new SortedSmartBodySchedulingStrategy(pegBoard)), bmlBlockManager, pegBoard);
        }
        return scheduler;
    }

    protected void readBMLRealizerSubsection() throws IOException
    {
        if (theTokenizer.atSTag("ServerAdapter"))
        {
            attrMap = theTokenizer.getAttributes();
            String requestPort = adapter.getRequiredAttribute("requestport", attrMap, theTokenizer);
            String feedbackPort = adapter.getRequiredAttribute("feedbackport", attrMap, theTokenizer);
            tcpipToBMLRealizerAdapter = new TCPIPToBMLRealizerAdapter(realizerPort, Integer.valueOf(requestPort),
                    Integer.valueOf(feedbackPort));
            theTokenizer.takeSTag("ServerAdapter");
            theTokenizer.takeETag("ServerAdapter");
        }
        else if (theTokenizer.atSTag("Scheduler"))
        {
            // TODO
            logger.error("Encountered Scheduler section that was not at beginning of BMLRealizer section");
        }
        else if (theTokenizer.atSTag("LogPipe"))
        {
            attrMap = theTokenizer.getAttributes();
            String requestLog = adapter.getOptionalAttribute("requestlog", attrMap);
            String feedbackLog = adapter.getOptionalAttribute("feedbacklog", attrMap);
            Logger rl = null;
            Logger fl = null;
            if (requestLog != null)
            {
                rl = LoggerFactory.getLogger(requestLog);
            }
            if (feedbackLog != null)
            {
                fl = LoggerFactory.getLogger(feedbackLog);
            }
            realizerPort = new LogPipe(rl, fl, realizerPort, theSchedulingClock);
            theTokenizer.takeSTag("LogPipe");
            theTokenizer.takeETag("LogPipe");
        }
        else
        {
            throw theTokenizer.getXMLScanException("Unknown tag in BMLRealizer content");
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
                        elckerlycRealizer.addEngine(behaviorClass, engine);
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

}
