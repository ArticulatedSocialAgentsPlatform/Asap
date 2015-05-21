/*******************************************************************************
 *******************************************************************************/
package asap.realizerport.util;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizerport.BMLFeedbackListener;

/**
 * Sends warning feedback to the stderr
 * @author Herwin
 *
 */
@Slf4j
public class StderrWarningListener implements BMLFeedbackListener
{   
    @Override
    public void feedback(String feedback)
    {
        BMLFeedback fb;
        try
        {
            fb = BMLFeedbackParser.parseFeedback(feedback);
        }
        catch (IOException e)
        {
            log.warn("Invalid feedback " + feedback, e);
            return;
        }
        if (fb instanceof BMLWarningFeedback)
        {
            System.err.print(feedback);
        }
    }
}
