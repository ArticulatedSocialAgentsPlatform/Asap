package asap.bmlflowvisualizer;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;

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

/**
 * Visualizes the status of the BML blocks submitted to the realizer
 * @author hvanwelbergen
 * 
 */
public class BMLFlowVisualizerPort implements RealizerPort, BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private final JPanel panel = new JPanel();
    private BMLFlowVisualization planningQueue;
    private BMLFlowVisualization finishedQueue;
    private BMLFlowVisualization playingQueue;

    public BMLFlowVisualizerPort(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(this);
    }

    public void addVisualization(BMLFlowVisualization pqvis, BMLFlowVisualization fqvis, BMLFlowVisualization plqvis)
    {
        this.planningQueue = pqvis;
        this.finishedQueue = fqvis;
        this.playingQueue = plqvis;
        panel.setLayout(new FlowLayout());
        panel.add(planningQueue.getVisualization());
        panel.add(plqvis.getVisualization());
        panel.add(finishedQueue.getVisualization());        
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
        if(fb instanceof BMLBlockProgressFeedback)
        {
            BMLBlockProgressFeedback fbBlock = (BMLBlockProgressFeedback)fb;
            if(fbBlock.getSyncId().equals("end"))
            {
                finishedQueue.finishBlock(fbBlock);
                planningQueue.finishBlock(fbBlock);
                playingQueue.finishBlock(fbBlock);
            }
            else if(fbBlock.getSyncId().equals("start"))
            {
                planningQueue.startBlock(fbBlock);
                playingQueue.startBlock(fbBlock);
                finishedQueue.startBlock(fbBlock);
            }
        }
        if(fb instanceof BMLPredictionFeedback)
        {
            BMLPredictionFeedback pf = (BMLPredictionFeedback)fb;
            for(BMLBlockPredictionFeedback bbp:pf.getBmlBlockPredictions())
            {
                planningQueue.updateBlock(bbp);
                playingQueue.updateBlock(bbp);
                finishedQueue.updateBlock(bbp);
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
    public void performBML(String bmlString)
    {
        BehaviourBlock bb = new BehaviourBlock(new BMLABMLBehaviorAttributes());
        
        try
        {
            bb.readXML(bmlString);
        }
        catch (RuntimeException e)
        {
            System.out.println(bmlString);
            e.printStackTrace();
            
        }
        if(bb.getComposition().equals(CoreComposition.REPLACE))
        {
            planningQueue.clear();
            playingQueue.clear();
            finishedQueue.clear();
        }
        planningQueue.planBlock(bb);
        playingQueue.planBlock(bb);
        finishedQueue.planBlock(bb);
        realizerPort.performBML(bmlString);
    }

    public JComponent getVisualization()
    {
        return panel;
    }

}
