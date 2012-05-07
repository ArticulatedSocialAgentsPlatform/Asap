package asap.speechengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import hmi.bml.core.SpeechBehaviour;
import hmi.bml.feedback.BMLExceptionFeedback;
import hmi.bml.feedback.ListBMLExceptionListener;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.DefaultPlayer;
import hmi.elckerlyc.Player;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.MultiThreadedPlanPlayer;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.speechengine.TextOutput;
import asap.speechengine.TextPlanner;
import asap.speechengine.TimedTextSpeechUnit;

/**
 * Tests the combination of a TextPlanner, (default) player and 'real' TimedTextUnits
 * 
 * @author welberge
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class,TimedTextSpeechUnit.class})
public class TextPlannerIntegrationTest
{
    TextOutput mockTextOutput = mock(TextOutput.class);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private PlanManager<TimedTextSpeechUnit> planManager = new PlanManager<TimedTextSpeechUnit>();

    @Test
    public void testPlayExceptionLeadsToBMLException() throws BehaviourPlanningException, InterruptedException, IOException,
            TimedPlanUnitPlayException
    {
        // checks if TextUnit failure properly appears as BMLExceptionFeedback
        List<BMLExceptionFeedback> exceptionList = new ArrayList<BMLExceptionFeedback>();
        Player vp = new DefaultPlayer(new MultiThreadedPlanPlayer<TimedTextSpeechUnit>(fbManager, planManager));
        TextPlanner textP = new TextPlanner(fbManager, mockTextOutput, planManager);
        fbManager.addExceptionListener(new ListBMLExceptionListener(exceptionList));
        final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);

        SpeechBehaviour beh = new SpeechBehaviour("bml1", new XMLTokenizer("<speech id=\"speech1\"><text>Hello world</text></speech>"));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sp.setGlobalValue(1);

        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        TimedTextSpeechUnit pu = textP.resolveSynchs(bbPeg, beh, sacs);

        TimedTextSpeechUnit spyPu = spy(pu);
        doThrow(new TimedPlanUnitPlayException("Play failed!", pu)).when(spyPu).play(anyDouble());

        spyPu.setState(TimedPlanUnitState.LURKING);
        textP.addBehaviour(bbPeg, beh, sacs, spyPu);
        assertEquals(1, spyPu.getStartTime(), 0.0001);
        assertThat(spyPu.getEndTime(), greaterThan(spyPu.getStartTime()));

        vp.play(0);
        assertThat(exceptionList, hasSize(0));

        vp.play(1.1);
        Thread.sleep(400);
        assertThat(exceptionList, hasSize(1));
    }

}
