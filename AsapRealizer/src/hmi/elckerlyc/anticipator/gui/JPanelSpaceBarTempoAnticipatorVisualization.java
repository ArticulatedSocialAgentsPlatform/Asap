package hmi.elckerlyc.anticipator.gui;

import hmi.elckerlyc.anticipator.Anticipator;
import hmi.elckerlyc.anticipator.KeyInfo;
import hmi.elckerlyc.pegboard.TimePeg;

import java.awt.Color;
import java.util.Observable;

import javax.swing.JPanel;


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
