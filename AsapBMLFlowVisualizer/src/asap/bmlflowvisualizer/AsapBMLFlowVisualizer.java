package asap.bmlflowvisualizer;

import javax.swing.JPanel;

import saiba.bml.core.BehaviourBlock;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

public class AsapBMLFlowVisualizer implements RealizerPort, BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private final JPanel panel = new JPanel();
    private final PlanningQueueVisualization planningQueue;
    
    public AsapBMLFlowVisualizer(RealizerPort port, PlanningQueueVisualization pqvis)
    {
        realizerPort = port;
        realizerPort.addListeners(this);
        this.planningQueue = pqvis;
        panel.add(planningQueue.getVisualization());        
    }
    
    public AsapBMLFlowVisualizer(RealizerPort port)
    {
        this(port, new PlanningQueueJPanelVisualization());
        
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

}
