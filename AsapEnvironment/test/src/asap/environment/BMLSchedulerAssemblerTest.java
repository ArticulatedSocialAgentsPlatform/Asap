package asap.environment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.parser.BMLParser;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.scheduler.SchedulingHandler;
import hmi.elckerlyc.scheduler.SchedulingStrategy;
import hmi.elckerlyc.scheduler.SmartBodySchedulingStrategy;

import org.junit.Test;

import asap.utils.SchedulingClock;

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
    private SchedulingClock mockSchedulingClock = mock(SchedulingClock.class);
    private static SchedulingHandler stubbedSchedulingHandler;
    private PegBoard pegBoard = new PegBoard();
    
    private static class StubSchedulingHandler implements SchedulingHandler
    {
        private final SchedulingStrategy schedulingStrategy;

        @SuppressWarnings("unused")
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
        public void schedule(BehaviourBlock bb, BMLScheduler scheduler)
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
                + " schedulingStrategy=\"hmi.elckerlyc.scheduler.SmartBodySchedulingStrategy\"/>" + "</BMLScheduler>";
        assembler.readXML(str);
        assertNotNull(stubbedSchedulingHandler);
        assertThat(stubbedSchedulingHandler, instanceOf(StubSchedulingHandler.class));
        assertThat( ((StubSchedulingHandler)stubbedSchedulingHandler).getSchedulingStrategy(), instanceOf(SmartBodySchedulingStrategy.class));
    }
}
