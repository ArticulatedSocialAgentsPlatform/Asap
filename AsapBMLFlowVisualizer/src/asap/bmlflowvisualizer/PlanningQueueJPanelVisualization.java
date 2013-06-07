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

/**
 * Visualizes the planning queue on a JPanel
 * @author hvanwelbergen
 * 
 */
public class PlanningQueueJPanelVisualization implements PlanningQueueVisualization
{
    private final JPanel panel = new JPanel();
    private Map<String, JComponent> planMap = new HashMap<>();

    public PlanningQueueJPanelVisualization()
    {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    public void addBlock(final BehaviourBlock bb)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = new JPanel();
                JLabel label = new JLabel(bb.getBmlId());
                p.setBackground(Color.GRAY);
                p.setBorder(new LineBorder(Color.BLACK));
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
}
