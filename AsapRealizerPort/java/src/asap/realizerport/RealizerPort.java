/*******************************************************************************
 *******************************************************************************/
package asap.realizerport;

/**
 * See package documentation.
 * @author welberge
 */
public interface RealizerPort
{
    /**
     * Add some listeners to which BML Feedback will be sent
     */
    void addListeners(BMLFeedbackListener ... listeners);
    
    
    void removeListener(BMLFeedbackListener l);
    
    /**
     * Removes all BMLListeners
     */
    void removeAllListeners();
    
    /**
     * Asks the realizer to perform a BML block. Non-blocking: this call will NOT block until the BML 
     * has been completely performed! It may block until the BML has been scheduled, though -- this is undetermined.
     */
    void performBML(String bmlString);
}