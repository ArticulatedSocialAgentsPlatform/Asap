package hmi.elckerlyc.anticipator.gui;

import hmi.elckerlyc.anticipator.KeyInfo;

import java.awt.Color;
import java.util.Observable;

import javax.swing.JPanel;

public class JPanelSpaceBarVisualization extends SpacebarAnticipatorVisualization
{
    private final JPanel panel;
    
    public JPanelSpaceBarVisualization(JPanel jp, KeyInfo ki)
    {
        super(ki);
        panel = jp;
        panel.setBackground(Color.GREEN);
        panel.setForeground(Color.GREEN);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if(keyInfo.isPressed())
        {
            panel.setBackground(Color.GREEN);          
            panel.setForeground(Color.GREEN);
        }
        else
        {
            panel.setBackground(Color.RED);
            panel.setForeground(Color.RED);
        }
        panel.repaint();
    }
}
