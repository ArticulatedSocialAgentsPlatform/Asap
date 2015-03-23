/*******************************************************************************
 *******************************************************************************/
package asap.realizerintegrationtest;

import static org.mockito.Mockito.mock;
import hmi.util.Clock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.parser.BMLParser;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerembodiments.impl.BMLSchedulerAssembler;

/**
 * Test cases to load some Asap-specific instantiations of an environment
 * @author hvanwelbergen
 */
public class EnvironmentLoadingTest
{
    private BMLBlockManager bbm = new BMLBlockManager();
    private PegBoard pegBoard = new PegBoard();
    private Clock mockClock = mock(Clock.class);

    @Test
    public void testLoadScheduler() throws IOException
    {
        BMLSchedulerAssembler asm = new BMLSchedulerAssembler("id1", new BMLParser(), NullFeedbackManager.getInstance(), bbm, mockClock,
                pegBoard);
        String str = "<BMLScheduler>" + "<SchedulingHandler class=\"asap.realizer.scheduler.BMLASchedulingHandler\" "
                + "schedulingStrategy=\"asap.realizer.scheduler.SortedSmartBodySchedulingStrategy\"/>" + "</BMLScheduler>";
        asm.readXML(new XMLTokenizer(str));
    }
}
