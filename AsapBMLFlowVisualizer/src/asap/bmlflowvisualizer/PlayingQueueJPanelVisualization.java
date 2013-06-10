package asap.bmlflowvisualizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;

public class PlayingQueueJPanelVisualization implements BMLFlowVisualization
{
    private JPanel panel = new JPanel();
    private Map<String, JPanel> blockMap = new HashMap<String, JPanel>();

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
            p.setBorder(new LineBorder(Color.BLACK));
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
        for (String id : blockMap.keySet())
        {
            removeBlock(id);
        }
    }

    @Override
    public void planBlock(BehaviourBlock bb)
    {
                
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
