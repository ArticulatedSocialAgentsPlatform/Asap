package asap.bmlflowvisualizer;

import javax.swing.JComponent;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;

/**
 * Visualizes BML blocks
 * @author hvanwelbergen
 *
 */
public interface BMLFlowVisualization
{
    void planBlock(BehaviourBlock bb);
    void startBlock(BMLBlockProgressFeedback bb);
    void finishBlock(BMLBlockProgressFeedback bb);
    void updateBlock (BMLBlockPredictionFeedback pred);
    
    void removeBlock(String id);
    JComponent getVisualization();
    
    /**
     * remove all visualizations
     */
    void clear();
}
