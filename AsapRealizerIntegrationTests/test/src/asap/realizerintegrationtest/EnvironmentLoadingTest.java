package asap.realizerintegrationtest;

import static org.mockito.Mockito.mock;
import hmi.bml.parser.BMLParser;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.environment.BMLSchedulerAssembler;
import asap.utils.SchedulingClock;

/**
 * Test cases to load some Asap-specific instantiations of an environment
 * @author hvanwelbergen
 */
public class EnvironmentLoadingTest
{
    private BMLBlockManager bbm = new BMLBlockManager();
    private PegBoard pegBoard = new PegBoard();
    private SchedulingClock mockClock = mock(SchedulingClock.class);

    @Test
    public void testLoadScheduler() throws IOException
    {
        BMLSchedulerAssembler asm = new BMLSchedulerAssembler("id1", new BMLParser(), NullFeedbackManager.getInstance(), bbm, mockClock,
                pegBoard);
        String str = "<BMLScheduler>" + "<SchedulingHandler class=\"asap.scheduler.BMLBandTSchedulingHandler\" "
                + "schedulingStrategy=\"hmi.elckerlyc.scheduler.SortedSmartBodySchedulingStrategy\"/>" + "</BMLScheduler>";
        asm.readXML(new XMLTokenizer(str));
    }
}
