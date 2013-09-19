package asap.bmlflowvisualizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Visualizes the planning queue on a JPanel
 * @author hvanwelbergen
 * 
 */
public class PlanningQueueJPanelVisualization implements BMLFlowVisualization
{
    private JPanel panel;
    private Map<String, JComponent> planMap = new HashMap<>();

    public PlanningQueueJPanelVisualization()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JLabel(" Planning "));
            }
        });
    }

    @Override
    public void planBlock(final BehaviourBlock bb)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = new JPanel();
                BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
                Border b = new LineBorder(Color.BLACK);
                if (bmlaAttr != null)
                {
                    if (bmlaAttr.isPrePlanned())
                    {
                        b = new LineBorder(Color.BLUE, 2);
                    }
                }
                JLabel label = new JLabel(bb.getBmlId());
                p.setBorder(b);
                p.setBackground(Color.GRAY);

                p.add(label);
                planMap.put(bb.getBmlId(), p);
                panel.add(p);
                panel.repaint();
                panel.updateUI();
            }
        });

    }

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

    public JPanel getVisualization()
    {
        return panel;
    }

    @Override
    public void clear()
    {
        for (String id : planMap.keySet())
        {
            removeBlock(id);
        }
    }

    @Override
    public void startBlock(BMLBlockProgressFeedback bb)
    {
        removeBlock(bb.getBmlId());
    }

    @Override
    public void finishBlock(BMLBlockProgressFeedback bb)
    {
        removeBlock(bb.getBmlId());

    }

    @Override
    public void updateBlock(BMLBlockPredictionFeedback pred)
    {
        removeBlock(pred.getId());
    }
}
