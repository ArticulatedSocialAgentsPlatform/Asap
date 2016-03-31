/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import java.io.IOException;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

/**
 * Wraps the BMLFeedbackParser to provide BMLAFeedback
 * @author hvanwelbergen
 *
 */
public class BMLAFeedbackParser
{
    private BMLAFeedbackParser()
    {
    }

    /**
     * @param str the feedback String
     * @return the corresponding instance of feedback, null if none matching. Returns either BMLABlockProgressFeedback, 
     * BMLASyncPointProgressFeedback, BMLAPredictionFeedback, BMLWarningFeedback or null.
     * @throws IOException
     */
    public static final BMLFeedback parseFeedback(String str) throws IOException
    {
        BMLFeedback fb = BMLFeedbackParser.parseFeedback(str);
        if (fb instanceof BMLBlockProgressFeedback)
        {
            return BMLABlockProgressFeedback.build((BMLBlockProgressFeedback) fb);
        }
        else if (fb instanceof BMLSyncPointProgressFeedback)
        {
            return BMLASyncPointProgressFeedback.build((BMLSyncPointProgressFeedback) fb);
        }
        else if (fb instanceof BMLPredictionFeedback)
        {
            return BMLAPredictionFeedback.build((BMLPredictionFeedback)fb);
        }
        return fb;
    }
}
