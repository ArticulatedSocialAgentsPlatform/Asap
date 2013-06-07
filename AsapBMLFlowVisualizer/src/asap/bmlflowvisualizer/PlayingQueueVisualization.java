package asap.bmlflowvisualizer;

import javax.swing.JComponent;

import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;

/**
 * Visualizes the currently planned, may be playing bml blocks
 * @author hvanwelbergen
 *
 */
public interface PlayingQueueVisualization
{
    void startBlock(BMLBlockProgressFeedback bb);
    void updateBlock(BMLBlockPredictionFeedback pf);
    void removeBlock(String id);
    JComponent getVisualization();
    
    /**
     * remove all visualizations
     */
    void clear();
}
