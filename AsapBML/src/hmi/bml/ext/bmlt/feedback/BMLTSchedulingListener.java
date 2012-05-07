package hmi.bml.ext.bmlt.feedback;

import hmi.bml.feedback.BMLListener;
/**
 * Listener interface for Scheduling feedback messages
 * @author welberge
 */
public interface BMLTSchedulingListener extends BMLListener
{
    void schedulingFinished(BMLTSchedulingFinishedFeedback pff);
    void schedulingStart(BMLTSchedulingStartFeedback psf);
}
