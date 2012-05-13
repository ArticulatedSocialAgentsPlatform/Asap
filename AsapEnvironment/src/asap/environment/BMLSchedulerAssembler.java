package asap.environment;

import saiba.bml.parser.BMLParser;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.scheduler.BMLTSchedulingHandler;
import hmi.elckerlyc.scheduler.SchedulingHandler;
import hmi.elckerlyc.scheduler.SortedSmartBodySchedulingStrategy;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.utils.SchedulingClock;

/**
 * Constructs a BMLScheduler from an xml description.
 * @author hvanwelbergen
 *
 */
public class BMLSchedulerAssembler extends XMLStructureAdapter
{
    private SchedulingHandler schedulingHandler;
    private final BMLParser parser;
    private final FeedbackManager feedbackManager;
    private final String name;
    private final SchedulingClock schedulingClock;
    private final BMLBlockManager bmlBlockManager;
    private BMLScheduler bmlScheduler;
    private final PegBoard pegBoard;

    public BMLScheduler getBMLScheduler()
    {
        return bmlScheduler;
    }

    public BMLSchedulerAssembler(String name, BMLParser parser, FeedbackManager feedbackManager, BMLBlockManager bmlBlockManager,
            SchedulingClock schedulingClock, PegBoard pb)
    {
        this.parser = parser;
        this.name = name;
        this.feedbackManager = feedbackManager;
        this.schedulingClock = schedulingClock;
        this.bmlBlockManager = bmlBlockManager;
        this.pegBoard = pb;
        schedulingHandler = new BMLTSchedulingHandler(new SortedSmartBodySchedulingStrategy(pb));

    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (!tokenizer.atETag())
        {
            if (tokenizer.atSTag("SchedulingHandler"))
            {
                SchedulingHandlerAssembler asm = new SchedulingHandlerAssembler(pegBoard);
                asm.readXML(tokenizer);
                schedulingHandler = asm.getBMLSchedulingHandler();
            }
        }
        bmlScheduler = new BMLScheduler(name, parser, feedbackManager, schedulingClock, schedulingHandler, bmlBlockManager, pegBoard);
    }

    private static final String XMLTAG = "BMLScheduler";    
    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
