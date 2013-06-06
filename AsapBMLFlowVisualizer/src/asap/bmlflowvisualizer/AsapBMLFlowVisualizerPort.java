package asap.bmlflowvisualizer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import saiba.bml.core.BehaviourBlock;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * Visualizes the status of the BML blocks submitted to the realizer
 * @author hvanwelbergen
 *
 */
public class AsapBMLFlowVisualizerPort implements RealizerPort, BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private final JPanel panel = new JPanel();
    private PlanningQueueVisualization planningQueue;
    
    public AsapBMLFlowVisualizerPort(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(this);                
    }
    
    public void addVisualization(PlanningQueueVisualization pqvis)
    {
        this.planningQueue = pqvis;
        panel.add(planningQueue.getVisualization());
    }
    
    @Override
    public void feedback(String feedback)
    {
                
    }

    @Override
    public void addListeners(BMLFeedbackListener... listeners)
    {
        realizerPort.addListeners(listeners);        
    }

    @Override
    public void removeAllListeners()
    {
        realizerPort.removeAllListeners();        
    }

    @Override
    public void performBML(String bmlString)
    {
        BehaviourBlock bb = new BehaviourBlock();
        bb.readXML(bmlString);
        planningQueue.addBlock(bb);
        realizerPort.performBML(bmlString);        
    }
    
    public JComponent getVisualization()
    {
        return panel;
    }

}
