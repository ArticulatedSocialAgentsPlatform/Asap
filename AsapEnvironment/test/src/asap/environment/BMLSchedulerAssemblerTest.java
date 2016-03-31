/*******************************************************************************
 *******************************************************************************/
package asap.environment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.util.Clock;

import org.junit.Test;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.parser.BMLParser;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SchedulingHandler;
import asap.realizer.scheduler.SchedulingStrategy;
import asap.realizer.scheduler.SmartBodySchedulingStrategy;
import asap.realizerembodiments.impl.BMLSchedulerAssembler;

/**
 * Unit testcases for the BMLSchedulerAssembler
 * @author hvanwelbergen
 * 
 */
public class BMLSchedulerAssemblerTest
{
    private BMLParser mockParser = mock(BMLParser.class);
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private BMLBlockManager bmlBlockManager = new BMLBlockManager();
    private Clock mockSchedulingClock = mock(Clock.class);
    private static SchedulingHandler stubbedSchedulingHandler;
    private PegBoard pegBoard = new PegBoard();
    
    /**
     * SchedulingHandler test stub 
     * @author Herwin
     */
    public static class StubSchedulingHandler implements SchedulingHandler
    {
        private final SchedulingStrategy schedulingStrategy;

        public StubSchedulingHandler(SchedulingStrategy ss, PegBoard pb)
        {
            schedulingStrategy = ss;
            stubbedSchedulingHandler = this;
        }

        public SchedulingStrategy getSchedulingStrategy()
        {
            return schedulingStrategy;
        }

        @Override
        public void schedule(BehaviourBlock bb, BMLScheduler scheduler, double time)
        {
            
        }
    }

    @Test
    public void testSchedulingHandler()
    {

        BMLSchedulerAssembler assembler = new BMLSchedulerAssembler("x", mockParser, mockFeedbackManager, bmlBlockManager,
                mockSchedulingClock, pegBoard);
        String str = "<BMLScheduler>"
                + "<SchedulingHandler class=\"asap.environment.BMLSchedulerAssemblerTest$StubSchedulingHandler\""
                + " schedulingStrategy=\"asap.realizer.scheduler.SmartBodySchedulingStrategy\"/>" + "</BMLScheduler>";
        assembler.readXML(str);
        assertNotNull(stubbedSchedulingHandler);
        assertThat(stubbedSchedulingHandler, instanceOf(StubSchedulingHandler.class));
        assertThat( ((StubSchedulingHandler)stubbedSchedulingHandler).getSchedulingStrategy(), instanceOf(SmartBodySchedulingStrategy.class));
    }
}
