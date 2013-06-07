package asap.bmlflowvisualizer;

import javax.swing.JComponent;

import saiba.bml.core.BehaviourBlock;

/**
 * Visualizes the queue of BML blocks that are waiting to be planned.
 * @author hvanwelbergen
 *
 */
public interface PlanningQueueVisualization
{
    void addBlock(BehaviourBlock bb);
    void removeBlock(String id);
    JComponent getVisualization();
    
    /**
     * remove all visualizations
     */
    void clear();
}
