package asap.bmlflowvisualizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;

public class FinishedQueueJPanelVisualization implements BMLFlowVisualization
{
    private JPanel panel = new JPanel();
    private Map<String, JComponent> planMap = new HashMap<>();

    private Set<String> addedBlocks = new HashSet<String>();
    private Set<String> plannedBlocks = new HashSet<String>();
    private Set<String> startedBlocks = new HashSet<String>();
    private Set<String> interruptSet = new HashSet<String>();

    public FinishedQueueJPanelVisualization()
    {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(" Finished "));
    }

    @Override
    public void finishBlock(final BMLBlockProgressFeedback bb)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = new JPanel();                
                JLabel label = new JLabel(bb.getBmlId());
                if (startedBlocks.contains(bb.getBmlId()) && interruptSet.contains(bb.getBmlId()))
                {
                    p.setBackground(Color.RED);
                }
                else if (startedBlocks.contains(bb.getBmlId()))
                {
                    p.setBackground(Color.GREEN);
                }
                else if (plannedBlocks.contains(bb.getBmlId()))
                {
                    p.setBackground(Color.YELLOW);
                }
                else if (addedBlocks.contains(bb.getBmlId()))
                {
                    p.setBackground(Color.GRAY);
                }
                p.setBorder(new LineBorder(Color.BLACK));
                p.add(label);
                planMap.put(bb.getBmlId(), p);
                panel.add(p);
                panel.repaint();
                panel.updateUI();
            }
        });
    }

    @Override
    public void removeBlock(final String id)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JComponent label = planMap.get(id);
                planMap.remove(id);
                if (label != null)
                {

                    panel.remove(label);
                    panel.repaint();
                    panel.updateUI();
                }

            }
        });
    }

    @Override
    public JComponent getVisualization()
    {
        return panel;
    }

    @Override
    public void clear()
    {
        plannedBlocks.clear();
        addedBlocks.clear();
        startedBlocks.clear();
        interruptSet.clear();
        for (String id : planMap.keySet())
        {
            removeBlock(id);
        }
    }

    @Override
    public void planBlock(BehaviourBlock bb)
    {
        addedBlocks.add(bb.getBmlId());
        BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
        if (bmlaAttr != null)
        {
            interruptSet.addAll(bmlaAttr.getInterruptList());
        }
    }

    @Override
    public void startBlock(BMLBlockProgressFeedback bb)
    {
        startedBlocks.add(bb.getBmlId());
    }

    @Override
    public void updateBlock(BMLBlockPredictionFeedback pred)
    {
        plannedBlocks.add(pred.getId());
    }
}
