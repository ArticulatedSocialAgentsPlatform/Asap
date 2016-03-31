/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLInfo;
import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.BMLParser;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;
import asap.realizer.scheduler.BMLASchedulingHandler;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;
import asap.realizerport.BMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Use this thin wrapper to a AbstractScheduler to control one virtual human by sending snippets of BML to it.
 * 
 * This is one of the central accesspoints to controlling a virtual human. (The
 * 
 * A BMLRealizer is built from the following elements:
 * <ul>
 * <li>A BML parser, to parse the BML snippets (stored in BehaviourBlocks) and collect the constraints (this parser is a scheduler.Scheduler object)
 * <li>Planners that control the Virtual Human (constructing the virtual human is outside the scope of this fragment of documentation). For example:
 * <ul>
 * <li>An AudioPlanner to process BMLTAudioFile elements into audio
 * <li>A ttsPlanner to process SpeechBehaviour elements into speech
 * <li>An AnimationPlanner to process Head and BMLTProcAnimation behaviors into animation (and more, later?), constructed from an AnimationPlayer (to control the body of the
 * VH) and a GestureBinding (to map BML behavior specifications to MotionUnits
 * <li>A facePlanner to control the face
 * <li>And in the future, possibly more planners
 * </ul>
 * <li>A BMLScheduler (e.g. SmartBodyScheduler) that combines the parser and several planners (verbal, animation) registered for several behaviors (head, BMLTProc, speech) to
 * play behavior sent from XML
 * <li>Possibly a number of Anticipators [DOCUMENT WHAT THESE DO!]
 * </ul>
 * 
 * One way to obtain a BMLRealizer is to request one upon creation of a virtual human in an ElckerlycEnvironment. By default, the BML Realizer will
 * use SmartBody scheduler. One can override the construction of the realizer by overriding the initXXX methods.
 * 
 * @author Dennis Reidsma
 * @author Herwin van Welbergen
 * 
 */
public class AsapRealizer
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AsapRealizer.class.getName());

    private final Clock schedulingClock;
    /**
     * to parse the BML snippets (stored in BehaviourBlocks) and collect the constraints (this parser is a scheduler.Scheduler object)
     */
    private final BMLParser parser;

    /**
     * A BaseScheduler (e.g. SmartBodyScheduler) that combines the parser and several planners (verbal, animation) registered for several behaviors
     * (head, BMLTProc, speech) to play behavior sent from XML
     */
    private final BMLScheduler scheduler;

    private final FeedbackManager fbManager;
    private final BMLBlockManager bmlBlockManager;

    /**
     * Constructs a ElckerlycRealizer facade and hooks up the planners to it
     */
    public AsapRealizer(BMLParser bmlparser, FeedbackManager fbm, Clock c, BMLScheduler bmlScheduler, Engine... engines)
    {
        schedulingClock = c;
        fbManager = fbm;
        parser = bmlparser;
        bmlBlockManager = bmlScheduler.getBMLBlockManager();
        LOGGER.info("Initializing Elckerlyc Scheduler");
        scheduler = bmlScheduler;
        for (Engine e : engines)
        {
            if (e != null) addEngine(e);
        }
        initBMLInfo();
    }

    /**
     * Constructs a AsapRealizer facade and hooks up the planners to it Uses a BMLScheduler with a SortedSmartBodySchedulingStrategy
     */
    public AsapRealizer(String characterId, BMLParser p, FeedbackManager fbm, Clock c, BMLBlockManager bbm, PegBoard pb, Engine... engines)
    {
        this(p, fbm, c, new BMLScheduler(characterId, p, fbm, c, new BMLASchedulingHandler(new SortedSmartBodySchedulingStrategy(pb), pb),
                bbm, pb), engines);
    }

    /**
     * Constructs a AsapRealizer facade and hooks up the planners to it Uses the default BMLParser as BMLParser and a BMLScheduler with a
     * SmartBodySchedulingStrategy.
     */
    public AsapRealizer(String characterId, FeedbackManager fbm, Clock c, BMLBlockManager bbm, PegBoard pb, Engine... engines)
    {
        this(characterId, new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().build()), fbm, c, bbm,
                pb, engines);
    }

    public void setParameterValue(String behId, String bmlId, String paramId, float value) throws ParameterException,
            BehaviorNotFoundException
    {
        scheduler.setParameterValue(bmlId, behId, paramId, value);
    }

    public void setParameterValue(String behId, String bmlId, String paramId, String value) throws ParameterException,
            BehaviorNotFoundException
    {
        scheduler.setParameterValue(bmlId, behId, paramId, value);
    }

    /**
     * Remove all feedback listeners and set a new one for the planners (who sets it in all newly created tmus).
     */
    public void setFeedbackListener(BMLFeedbackListener f)
    {
        fbManager.removeAllFeedbackListeners();
        fbManager.addFeedbackListener(f);
    }

    /**
     * add a feedbacklistener to the realizer. This listener will be registered with the animation planner and verbal planner.
     */
    public void addFeedbackListener(BMLFeedbackListener newListener)
    {
        scheduler.addFeedbackListener(newListener);
    }

    /**
     * Init the BMLInfo, to make sure that the BML parser knows how to handle all BMLT behavior types. This needs to be done for all non-core BML
     * element types.
     */
    private void initBMLInfo()
    {
        for (Engine e : scheduler.getEngines())
        {
            for (Class<? extends Behaviour> beh : e.getSupportedDescriptionExtensions())
            {
                if (!BMLInfo.supportedExtensions.contains(beh))
                {
                    BMLInfo.supportedExtensions.add(beh);
                }
            }
        }

    }

    /**
     * Add Engine e to plan behavior beh
     */
    public void addEngine(Class<? extends Behaviour> beh, Engine e)
    {
        scheduler.addEngine(beh, e);
    }

    /**
     * Add Engine e to plan all its supported behaviors and description extensions
     */
    public void addEngine(Engine e)
    {
        for (Class<? extends Behaviour> beh : e.getSupportedBehaviours())
        {
            addEngine(beh, e);
        }
        for (Class<? extends Behaviour> beh : e.getSupportedDescriptionExtensions())
        {
            addEngine(beh, e);
            BMLInfo.supportedExtensions.add(beh);
        }
    }

    /**
     * Schedules piece of BML. This call is blocking.
     * 
     * @throws IOException
     * @throws XMLScanException if the BML was invalid XML
     */
    public void scheduleBML(Reader in) throws IOException
    {
        scheduleBML(new XMLTokenizer(in));
    }

    public void shutdown()
    {
        scheduler.shutdown();
    }

    /**
     * Schedules piece of BML. This call is blocking.
     * 
     * @throws XMLScanException if the BML was invalid XML
     */
    public void scheduleBML(XMLTokenizer in)
    {
        BehaviourBlock block;

        try
        {
            block = parser.createBehaviourBlock();
        }
        catch (InstantiationException e)
        {
            String bmlId = "<no id>";
            String exceptionText = "InstantiationException " + e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace()) + "\n";
            scheduler.warn(new BMLWarningFeedback(bmlId, "InstantiationException", exceptionText), scheduler.getSchedulingTime());
            return;
        }
        catch (IllegalAccessException e)
        {
            String bmlId = "<no id>";
            String exceptionText = "IllegalAccessException " + e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace()) + "\n";
            scheduler.warn(new BMLWarningFeedback(bmlId, "IllegalAccessException", exceptionText), scheduler.getSchedulingTime());
            return;
        }
        try
        {
            block.readXML(in);
        }
        catch (XMLScanException e)
        {
            String bmlId = "<no id>";
            String exceptionText = "Parsing BML failed: see stack trace for more info. " + e.getLocalizedMessage() + "\n"
                    + Arrays.toString(e.getStackTrace()) + "\n";
            scheduler.warn(new BMLWarningFeedback(bmlId, BMLWarningFeedback.PARSING_FAILURE, exceptionText), scheduler.getSchedulingTime());
            return;
        }
        catch (IOException e)
        {
            String bmlId = "<no id>";
            String exceptionText = "IO Exception reading BML. " + e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace())
                    + "\n";
            scheduler.warn(new BMLWarningFeedback(bmlId, "IOException", exceptionText), scheduler.getSchedulingTime());
            return;
        }
        catch (Exception e)
        { // DO NOT REMOVE THIS CLAUSE!
            String bmlId = "<no id>";
            String exceptionText = "Exception reading the XML. " + e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace())
                    + "\n";
            scheduler.warn(new BMLWarningFeedback(bmlId, "Exception", exceptionText), scheduler.getSchedulingTime());
            return;
        }
        try
        {
            parser.addBehaviourBlock(block);
        }
        catch (Exception e)
        { // DO NOT REMOVE THIS CLAUSE!
            String exceptionText = "Exception parsing the BML. " + e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace())
                    + "\n";
            scheduler.warn(new BMLWarningFeedback(block.id, "Exception", exceptionText), scheduler.getSchedulingTime());
            return;
        }
        try
        {
            scheduler.schedule();
        }
        catch (Exception e)
        { // DO NOT REMOVE THIS CLAUSE!
            String exceptionText = "Exception scheduling the BML. " + e + "\n" + Arrays.toString(e.getStackTrace()) + "\n";
            scheduler.warn(new BMLWarningFeedback(block.id, "Scheduling Exception", exceptionText), scheduler.getSchedulingTime());
            return;
        }
    }

    /**
     * Schedules piece of BML. This call is blocking.
     * @throws XMLScanException if the BML was invalid XML
     */
    public void scheduleBML(String blockContent)
    {
        XMLTokenizer in = new XMLTokenizer(blockContent);
        scheduleBML(in);
    }

    /**
     * Stops and removes all behaviors, restores players to start state, empties parser.
     */
    public void reset()
    {
        parser.clear();
        scheduler.reset();
    }

    public Engine getEngine(Class<? extends Behaviour> c)
    {
        return scheduler.getEngine(c);
    }

    /**
     * @return the scheduler
     */
    public BMLScheduler getScheduler()
    {
        return scheduler;
    }

    public FeedbackManager getFeedbackManager()
    {
        return fbManager;
    }

    public Clock getSchedulingClock()
    {
        return schedulingClock;
    }

    public BMLParser getBMLParser()
    {
        return parser;
    }

    public BMLBlockManager getBMLBlockManager()
    {
        return bmlBlockManager;
    }
}
