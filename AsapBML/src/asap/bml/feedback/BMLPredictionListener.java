package asap.bml.feedback;

import saiba.bml.feedback.BMLPredictionFeedback;

/**
 * Captures BMLPredictionFeedback
 * @author Herwin
 *
 */
public interface BMLPredictionListener extends BMLListener
{
    void prediction(BMLPredictionFeedback bpf);
}
