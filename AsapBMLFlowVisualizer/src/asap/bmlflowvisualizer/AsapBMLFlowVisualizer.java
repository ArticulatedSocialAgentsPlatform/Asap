package asap.bmlflowvisualizer;

import javax.swing.JPanel;

import saiba.bml.core.BehaviourBlock;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

public class AsapBMLFlowVisualizer implements RealizerPort, BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private final JPanel panel = new JPanel();
    private final PlanningQueueVisualization planningQueue = new PlanningQueueVisualization();
    
    public AsapBMLFlowVisualizer(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(this);
        panel.add(planningQueue.getJPanel());
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
