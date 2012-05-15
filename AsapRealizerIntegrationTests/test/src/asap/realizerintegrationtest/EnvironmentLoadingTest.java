package asap.realizerintegrationtest;

import static org.mockito.Mockito.mock;
import saiba.bml.parser.BMLParser;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.environment.BMLSchedulerAssembler;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
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
        String str = "<BMLScheduler>" + "<SchedulingHandler class=\"asap.realizer.scheduler.BMLBandTSchedulingHandler\" "
                + "schedulingStrategy=\"asap.realizer.scheduler.SortedSmartBodySchedulingStrategy\"/>" + "</BMLScheduler>";
        asm.readXML(new XMLTokenizer(str));
    }
}
