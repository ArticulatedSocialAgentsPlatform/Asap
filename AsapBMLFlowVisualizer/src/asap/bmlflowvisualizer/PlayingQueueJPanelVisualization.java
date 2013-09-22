package asap.bmlflowvisualizer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import asap.bmlflowvisualizer.graphutils.DAGUtils;
import asap.bmlflowvisualizer.graphutils.Edge;

/**
 * UI Element that shows the BML blocks in the scheduling queue.
 * @author Herwin
 *
 */
public class PlayingQueueJPanelVisualization implements BMLFlowVisualization
{
    private JPanel panel;
    private Map<String, JPanel> blockMap = Collections.synchronizedMap(new HashMap<String, JPanel>());
    private Set<String> preplannedBlocks = Collections.synchronizedSet(new HashSet<String>());
    private Set<BehaviourBlock> behaviorBlocks = Collections.synchronizedSet(new HashSet<BehaviourBlock>());

    public PlayingQueueJPanelVisualization()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                panel = new JPanel();
                panel.setLayout(new GridBagLayout());
                panel.add(new JLabel(" Playing "));
            }
        });
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
                b = new LineBorder(Color.BLUE, 2);
            }
            p.setBorder(b);
            blockMap.put(id, p);
        }
        return p;
    }

    private void layout()
    {
        List<Edge<String>> edges = new ArrayList<Edge<String>>();
        List<String> vertices = new ArrayList<String>();
        for (BehaviourBlock bb : behaviorBlocks)
        {
            if (!blockMap.containsKey(bb.getBmlId())) continue;
            BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
            if (bmlaAttr != null)
            {
                for (String id : bmlaAttr.getChunkBeforeList())
                {
                    if (blockMap.containsKey(id))
                    {
                        edges.add(new Edge<String>(bb.getBmlId(), id));
                    }
                }
                for (String id : bmlaAttr.getPrependBeforeList())
                {
                    if (blockMap.containsKey(id))
                    {
                        edges.add(new Edge<String>(bb.getBmlId(), id));
                    }
                }
                for (String id : bmlaAttr.getChunkAfterList())
                {
                    if (blockMap.containsKey(id))
                    {
                        edges.add(new Edge<String>(id, bb.getBmlId()));
                    }
                }
                for (String id : bmlaAttr.getAppendAfterList())
                {
                    if (blockMap.containsKey(id))
                    {
                        edges.add(new Edge<String>(id, bb.getBmlId()));
                    }
                }
            }
            vertices.add(bb.getBmlId());
        }
        final Map<String, Point> layout = DAGUtils.layout(vertices, edges);

        panel.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        panel.add(new JLabel(" Playing "), c);
        for (Entry<String, Point> entry : layout.entrySet())
        {
            c = new GridBagConstraints();
            c.gridx = entry.getValue().x + 1;
            c.gridy = entry.getValue().y + 2;
            panel.add(blockMap.get(entry.getKey()), c);
        }
        panel.repaint();
        panel.updateUI();
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
                    layout();
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
        behaviorBlocks.clear();
        synchronized (blockMap)
        {
            for (String id : blockMap.keySet())
            {
                removeBlock(id);
            }
        }
    }

    @Override
    public void planBlock(BehaviourBlock bb)
    {
        BMLABMLBehaviorAttributes bmlaAttr = bb.getBMLBehaviorAttributeExtension(BMLABMLBehaviorAttributes.class);
        behaviorBlocks.add(bb);
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
                layout();
                panel.repaint();
                panel.updateUI();
            }
        });
    }

}
