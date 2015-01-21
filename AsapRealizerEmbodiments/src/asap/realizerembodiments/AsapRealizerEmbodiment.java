/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.Clock;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.Behaviour;
import saiba.bml.parser.BMLParser;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.realizer.AsapRealizer;
import asap.realizer.Engine;
import asap.realizer.bridge.MultiThreadedElckerlycRealizerBridge;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLASchedulingHandler;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;
import asap.realizerembodiments.impl.BMLParserAssembler;
import asap.realizerembodiments.impl.BMLSchedulerAssembler;
import asap.realizerport.RealizerPort;

import com.google.common.collect.ImmutableSet;

/**
 * Loads and unloads an AsapRealizerEmbodiment and provides access to its elements (realizer, embodiments, engines, etc)
 */
@Slf4j
public class AsapRealizerEmbodiment implements EmbodimentLoader, Embodiment
{
    /**
     * The elckerlycRealizer is exposed to facilitate access to some advanced capabilities.
     * Generally you should not use this variable, there is a big chance that access to it
     * will be removed in the future
     */
    private AsapRealizer elckerlycRealizer = null;

    private Map<String, PipeLoader> pipeLoaders = new HashMap<>();

    /** Use the RealizerPort to send BML to the Realizer */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private RealizerPort realizerPort = null;

    /** Some engines (interrupt engine, paramvalchange) need access to the scheduler */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private BMLScheduler bmlScheduler = null;

    @Getter
    BMLBlockManager bmlBlockManager = null;
    @Getter
    FeedbackManager feedbackManager = null;

    /** "human readable name" */
    private String name = "<no name>";

    private String loaderId = "";

    private String vhId = "";

    /** used for loading the virtual human from an XML specification file */
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    /** used for loading the virtual human from an XML specification file */
    private HashMap<String, String> attrMap = null;
    /** used for loading the virtual human from an XML specification file */
    private XMLTokenizer theTokenizer = null;

    /**
     * Needed during the loading process in order to offer all other loaders access to the scheduling clock during load
     * (Some engines, such as interrupt engine and
     * paramvalchange, need access to such internals)
     */
    private Clock theSchedulingClock = null;

    @Getter
    private final PegBoard pegBoard = new PegBoard();

    /** Unload: remove the virtual human from the AsapEnvironment; stop scheduler etc; onload all other loaders */
    public void unload()
    {
        for (PipeLoader pipeLoader : pipeLoaders.values())
        {
            pipeLoader.shutdown();
        }
        elckerlycRealizer.shutdown(); // can you do this before all engines and emitters have been shut down?
    }

    @Override
    public String getId()
    {
        return loaderId;
    }

    @Override
    public AsapRealizerEmbodiment getEmbodiment()
    {
        return this;
    }

    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.loaderId = loaderId;
        this.name = vhName;
        this.vhId = vhId;

        for (Loader l : requiredLoaders)
        {
            if ((l instanceof EmbodimentLoader) && (((EmbodimentLoader) l).getEmbodiment() instanceof SchedulingClockEmbodiment)) theSchedulingClock = ((SchedulingClockEmbodiment) ((EmbodimentLoader) l)
                    .getEmbodiment()).getSchedulingClock();
        }
        if (theSchedulingClock == null)
        {
            throw new RuntimeException("AsapRealizerEmbodiment requires an SchedulingClockEmbodiment when loading");
        }

        theTokenizer = tokenizer;

        // ====== load BML parsing and scheduling stuff

        BMLParser parser = readParserSection();

        // FIXME assumes that the XML element starts with the scheduler information
        bmlBlockManager = new BMLBlockManager();
        feedbackManager = new FeedbackManagerImpl(bmlBlockManager, vhId);
        bmlScheduler = readSchedulerSection(bmlBlockManager, parser, feedbackManager);

        // ========= construct realizer and realizerport

        elckerlycRealizer = new AsapRealizer(parser, feedbackManager, theSchedulingClock, bmlScheduler);

        // we can't ensure that the port will only be called singlethreaded, so we put a multithread adapter on top of it
        realizerPort = new MultiThreadedElckerlycRealizerBridge(elckerlycRealizer);

        // ====== subsequently, read in XML all requests for pipes, ports and adapters, and create / insert / attach them.

        while (!theTokenizer.atETag("Loader"))
        {
            readBMLRealizerSubsection();
        }

    }

    public void addEngine(Engine e)
    {
        elckerlycRealizer.addEngine(e);
    }

    public void addEngine(Class<? extends Behaviour> behaviorClass, Engine e)
    {
        elckerlycRealizer.addEngine(behaviorClass, e);
    }

    protected BMLParser readParserSection() throws IOException
    {
        BMLParser parser = new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().add(
                BMLABMLBehaviorAttributes.class).build());
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
            BMLSchedulerAssembler assembler = new BMLSchedulerAssembler(vhId, parser, feedbackManager, bmlBlockManager, theSchedulingClock,
                    pegBoard);
            assembler.readXML(theTokenizer);
            scheduler = assembler.getBMLScheduler();
        }
        else
        {
            scheduler = new BMLScheduler(vhId, parser, feedbackManager, theSchedulingClock, new BMLASchedulingHandler(
                    new SortedSmartBodySchedulingStrategy(pegBoard), pegBoard), bmlBlockManager, pegBoard);
        }
        return scheduler;
    }

    public PipeLoader getPipeLoader(String id)
    {
        return pipeLoaders.get(id);
    }

    public Collection<PipeLoader> getPipeLoaders()
    {
        return pipeLoaders.values();
    }

    protected void readBMLRealizerSubsection() throws IOException
    {
        if (theTokenizer.atSTag("Scheduler"))
        {
            // TODO
            log.error("Encountered Scheduler section that was not at beginning of AsapRealizerEmbodiment section");
        }
        else if (theTokenizer.atSTag("PipeLoader"))
        {
            attrMap = theTokenizer.getAttributes();
            String id = adapter.getRequiredAttribute("id", attrMap, theTokenizer);
            String loaderClass = adapter.getRequiredAttribute("loader", attrMap, theTokenizer);
            PipeLoader pipeloader = null;
            try
            {
                pipeloader = (PipeLoader) Class.forName(loaderClass).newInstance();
            }
            catch (InstantiationException e)
            {
                throw theTokenizer.getXMLScanException("InstantiationException while starting PipeLoader " + loaderClass);
            }
            catch (IllegalAccessException e)
            {
                throw theTokenizer.getXMLScanException("IllegalAccessException while starting PipeLoader " + loaderClass);
            }
            catch (ClassNotFoundException e)
            {
                throw theTokenizer.getXMLScanException("ClassNotFoundException while starting PipeLoader " + loaderClass);
            }
            catch (ClassCastException e)
            {
                throw theTokenizer.getXMLScanException("ClassCastException while starting PipeLoader " + loaderClass);
            }

            theTokenizer.takeSTag("PipeLoader");
            log.debug("Parsing PipeLoader: {}", id);
            pipeloader.readXML(theTokenizer, id, vhId, name, realizerPort, theSchedulingClock);
            theTokenizer.takeETag("PipeLoader");
            realizerPort = pipeloader.getAdaptedRealizerPort();
            pipeLoaders.put(id, pipeloader);
        }
        else
        {
            throw theTokenizer.getXMLScanException("Unknown tag in AsapRealizerEmbodiment content");
        }
    }
}
