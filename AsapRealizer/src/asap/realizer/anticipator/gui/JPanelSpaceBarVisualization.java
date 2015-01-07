/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator.gui;


import java.awt.Color;
import java.util.Observable;

import javax.swing.JPanel;

import asap.realizer.anticipator.KeyInfo;

/**
 * Visualizes the spacebar presses on a green/red jpanel
 * @author Herwin
 *
 */
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
