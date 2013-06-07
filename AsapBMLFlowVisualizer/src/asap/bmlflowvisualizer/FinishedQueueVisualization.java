package asap.bmlflowvisualizer;

import javax.swing.JComponent;

import saiba.bml.feedback.BMLBlockProgressFeedback;

/**
 * Visualizes the queue of BML blocks that are waiting to be planned.
 * @author hvanwelbergen
 *
 */
public interface FinishedQueueVisualization
{
    void addBlock(BMLBlockProgressFeedback bb);
    void removeBlock(String id);
    
    /**
     * remove all visualizations
     */
    void clear();
    
    JComponent getVisualization();
}
