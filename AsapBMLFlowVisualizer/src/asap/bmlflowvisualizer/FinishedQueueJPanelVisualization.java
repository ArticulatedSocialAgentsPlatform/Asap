package asap.bmlflowvisualizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import saiba.bml.feedback.BMLBlockProgressFeedback;

public class FinishedQueueJPanelVisualization implements FinishedQueueVisualization
{
    private JPanel panel = new JPanel();
    private Map<String, JComponent> planMap = new HashMap<>();

    @Override
    public void addBlock(final BMLBlockProgressFeedback bb)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel p = new JPanel();
                JLabel label = new JLabel(bb.getBmlId());
                p.setBackground(Color.GREEN);
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

}
