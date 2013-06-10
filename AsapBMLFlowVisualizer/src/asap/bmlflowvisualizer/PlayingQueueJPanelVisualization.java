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
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;

public class PlayingQueueJPanelVisualization implements BMLFlowVisualization
{
    private JPanel panel = new JPanel();
    private Map<String, JPanel> blockMap = new HashMap<String, JPanel>();
    private Set<String> preplannedBlocks = new HashSet<String>();

    public PlayingQueueJPanelVisualization()
    {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    private JPanel getBlock(String id)
    {
        JPanel p = blockMap.get(id);
        if (p == null)
        {
            p = new JPanel();
            JLabel label = new JLabel(id);
            p.add(label);
            Border b = new LineBorder(Color.BLACK);
            if (preplannedBlocks.contains(id))
            {
                b = new LineBorder(Color.BLUE,2);
            }
            p.setBorder(b);
            blockMap.put(id, p);
            panel.add(p);
        }
        return p;
    }

    @Override
    public void startBlock(final BMLBlockProgressFeedback bb)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = getBlock(bb.getBmlId());
                p.setBackground(Color.ORANGE);
                
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
                JPanel label = blockMap.get(id);
                blockMap.remove(id);
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
        preplannedBlocks.clear();
        for (String id : blockMap.keySet())
        {
            removeBlock(id);
        }
    }

    @Override
    public void planBlock(BehaviourBlock bb)
    {
        BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
        if (bmlaAttr != null)
        {
            if (bmlaAttr.isPrePlanned())
            {
                preplannedBlocks.add(bb.getBmlId());
            }
        }
    }

    @Override
    public void finishBlock(BMLBlockProgressFeedback bb)
    {
        removeBlock(bb.getBmlId());
    }

    @Override
    public void updateBlock(final BMLBlockPredictionFeedback pf)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = getBlock(pf.getId());
                p.setBackground(Color.YELLOW);
                panel.repaint();
                panel.updateUI();
            }
        });
    }

}
