package asap.testutil.bml.feedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;

import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

/**
 * Custom asserts for BML feedbacks
 * @author welberge
 */
public final class FeedbackAsserts
{
    private FeedbackAsserts()
    {
    }

    public static void assertEqualSyncPointProgress(BMLSyncPointProgressFeedback expected, BMLSyncPointProgressFeedback actual)
    {
        assertEquals(expected.getBMLId(), actual.getBMLId());
        assertEquals(expected.getBehaviourId(),actual.getBehaviourId());
        assertEquals(expected.getSyncId(), actual.getSyncId());
        assertEquals(expected.getTime(), actual.getTime(), 0.01f);
        assertEquals(expected.getGlobalTime(), actual.getGlobalTime(), 0.01f);
    }

    public static void assertEqualPlanningStart(BMLTSchedulingStartFeedback expected, BMLTSchedulingStartFeedback actual)
    {
        assertEquals(expected.bmlId, actual.bmlId);
        assertEquals(expected.timeStamp, actual.timeStamp, 0.01f);
    }

    public static void assertEqualPlanningFinished(BMLTSchedulingFinishedFeedback expected, BMLTSchedulingFinishedFeedback actual)
    {
        assertEquals(expected.bmlId, actual.bmlId);
        assertEquals(expected.timeStamp, actual.timeStamp, 0.01f);
        assertEquals(expected.predictedEnd, actual.predictedEnd, 0.01f);
        assertEquals(expected.predictedStart, actual.predictedStart, 0.01f);
    }

    public static void assertOneFeedback(BMLSyncPointProgressFeedback expected, List<BMLSyncPointProgressFeedback> actual)
    {
        assertTrue("Expected one BMLSyncPointProgressFeedback, got " + actual, 1 == actual.size());
        assertEqualSyncPointProgress(expected, actual.get(0));
    }

    public static void assertOneFeedback(BMLTSchedulingStartFeedback expected, List<BMLTSchedulingStartFeedback> actual)
    {
        assertTrue("Expected one BMLTSchedulingStartFeedback, got " + actual, 1 == actual.size());
        assertEqualPlanningStart(expected, actual.get(0));
    }

    public static void assertOneFeedback(BMLTSchedulingFinishedFeedback expected, List<BMLTSchedulingFinishedFeedback> actual)
    {
        assertTrue("Expected one BMLTSchedulingFinishedFeedback, got " + actual, 1 == actual.size());
        assertEqualPlanningFinished(expected, actual.get(0));
    }
}
