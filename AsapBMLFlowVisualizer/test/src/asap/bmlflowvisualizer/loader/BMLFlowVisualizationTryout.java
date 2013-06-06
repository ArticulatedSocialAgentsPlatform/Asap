package asap.bmlflowvisualizer.loader;

import javax.swing.JFrame;

import org.junit.Test;

import asap.bmlflowvisualizer.AsapBMLFlowVisualizerPort;
import asap.bmlflowvisualizer.PlanningQueueJPanelVisualization;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

public class BMLFlowVisualizationTryout
{
    public static void main(String args[])
    {
        JFrame jf = new JFrame();
        
        RealizerPort rp = new RealizerPort()
        {
            @Override
            public void addListeners(BMLFeedbackListener... listeners)
            {
                
            }

            @Override
            public void removeAllListeners()
            {
                
            }

            @Override
            public void performBML(String bmlString)
            {
                
            }
        };

        AsapBMLFlowVisualizerPort port = new AsapBMLFlowVisualizerPort(rp);
        port.addVisualization(new PlanningQueueJPanelVisualization());
        jf.add(port.getVisualization());
        jf.setSize(1024, 768);
        jf.setVisible(true);
        port.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml1\"/>");
        port.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml2\"/>");
    }
}
