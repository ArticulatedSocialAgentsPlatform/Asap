/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator.gui;


import java.awt.Color;
import java.util.Observable;

import javax.swing.JPanel;

import asap.realizer.anticipator.Anticipator;
import asap.realizer.anticipator.KeyInfo;
import asap.realizer.pegboard.TimePeg;

/**
 * Visualizes the prediction of spacebar presses on a red/green jpanel
 * @author Herwin
 *
 */
public class JPanelSpaceBarTempoAnticipatorVisualization extends SpaceBarTempoAnticipatorVisualization
{
    private final JPanel predictPanel;
    private final JPanelSpaceBarVisualization spaceBarPressViz;
    private final static double DISPLAY_DURATION = 0.1; 
    
    public JPanelSpaceBarTempoAnticipatorVisualization(JPanel jpPress, JPanel jpPredict, KeyInfo keyInfo, Anticipator ant)
    {
        super(ant);
        spaceBarPressViz = new JPanelSpaceBarVisualization(jpPress,keyInfo);
        predictPanel = jpPredict;                
    }

    @Override
    public void update(Observable arg0, Object arg1)
    {
        spaceBarPressViz.update(arg0, arg1);
    }
    
    public void update(double time)
    {
        for(TimePeg tp:anticipator.getTimePegs())
        {
            if( time-tp.getGlobalValue()<DISPLAY_DURATION && time-tp.getGlobalValue()>0)
            {
                predictPanel.setBackground(Color.GREEN);
                predictPanel.setForeground(Color.GREEN);                
                return;
            }
        }
        predictPanel.setBackground(Color.RED);
        predictPanel.setForeground(Color.RED); 
    }
}
