package asap.bmlflowvisualizer;

import hmi.xml.XMLScanException;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLPredictionFeedback;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.collect.Lists;

/**
 * Visualizes the status of the BML blocks submitted to the realizer
 * @author hvanwelbergen
 * 
 */
public class BMLFlowVisualizerPort implements RealizerPort, BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private JPanel panel;
    private List<BMLFlowVisualization> visualizations = new ArrayList<BMLFlowVisualization>();

    public static JComponent createBMLFlowVisualizerPortUI(RealizerPort rp)
    {
        BMLFlowVisualizerPort port = new BMLFlowVisualizerPort(rp);
        port.setVisualization(new PlanningQueueJPanelVisualization(), new PlayingQueueJPanelVisualization(),
                new FinishedQueueJPanelVisualization());
        return port.getVisualization();
    }
    
    public BMLFlowVisualizerPort(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(this);
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                panel = new JPanel();
            }
        });
    }

    public void setVisualization(BMLFlowVisualization... vis)
    {
        visualizations = Lists.newArrayList(vis);

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                panel.setLayout(new GridBagLayout());
                int i = 1;
                for (BMLFlowVisualization v : visualizations)
                {
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = i;
                    c.gridy = 1;
                    c.anchor = GridBagConstraints.NORTH;
                    panel.add(v.getVisualization(), c);
                    i++;
                }
            }
        });

    }

    @Override
    public void feedback(String feedback)
    {
        BMLFeedback fb;
        try
        {
            fb = BMLFeedbackParser.parseFeedback(feedback);
        }
        catch (IOException e)
        {
            // shouldn't happen since we parse strings
            throw new AssertionError(e);
        }
        catch (XMLScanException e)
        {
            return;
        }

        if (fb instanceof BMLBlockProgressFeedback)
        {
            BMLBlockProgressFeedback fbBlock = (BMLBlockProgressFeedback) fb;
            if (fbBlock.getSyncId().equals("end"))
            {
                for (BMLFlowVisualization v : visualizations)
                {
                    v.finishBlock(fbBlock);
                }
            }
            else if (fbBlock.getSyncId().equals("start"))
            {
                for (BMLFlowVisualization v : visualizations)
                {
                    v.startBlock(fbBlock);
                }
            }
        }
        if (fb instanceof BMLPredictionFeedback)
        {
            BMLPredictionFeedback pf = (BMLPredictionFeedback) fb;
            for (BMLBlockPredictionFeedback bbp : pf.getBmlBlockPredictions())
            {
                for (BMLFlowVisualization v : visualizations)
                {
                    v.updateBlock(bbp);
                }
            }
        }
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
    public void removeListener(BMLFeedbackListener l)
    {
        realizerPort.removeListener(l);        
    }
    
    @Override
    public void performBML(String bmlString)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLABMLBehaviorAttributes());
        try
        {
            bb.readXML(bmlString);
            if (bb.getComposition().equals(CoreComposition.REPLACE))
            {
                for (BMLFlowVisualization v : visualizations)
                {
                    v.clear();
                }            
            }
            for (BMLFlowVisualization v : visualizations)
            {
                v.planBlock(bb);
            }
        }
        catch (XMLScanException e)
        {
            //handle error at toplevel port
        }        
        realizerPort.performBML(bmlString);
    }

    public JComponent getVisualization()
    {
        return panel;
    }

    

}
