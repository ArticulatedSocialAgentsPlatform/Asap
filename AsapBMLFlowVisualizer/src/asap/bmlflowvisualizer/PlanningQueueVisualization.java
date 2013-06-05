package asap.bmlflowvisualizer;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saiba.bml.core.BehaviourBlock;

public class PlanningQueueVisualization
{
    private final JPanel panel = new JPanel();
    private Map<String,JLabel> planMap = new HashMap<>();
    
    public PlanningQueueVisualization()
    {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }
    
    public void addBlock(BehaviourBlock bb)
    {
        JLabel label = new JLabel(bb.getBmlId());
        planMap.put(bb.getBmlId(),label);
    }
    
    public void removeBlock(String id)
    {
        JLabel label = planMap.get(id);
        planMap.remove(id);
        if(label!=null)
        {
            panel.remove(label);        
        }
    }
    
    public JPanel getJPanel()
    {
        return panel;
    }
}
