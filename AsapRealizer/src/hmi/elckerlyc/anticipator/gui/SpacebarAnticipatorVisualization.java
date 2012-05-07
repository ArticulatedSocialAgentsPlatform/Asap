package hmi.elckerlyc.anticipator.gui;

import hmi.elckerlyc.anticipator.KeyInfo;

import java.util.Observer;

public abstract class SpacebarAnticipatorVisualization implements Observer
{
    protected KeyInfo keyInfo;
    public SpacebarAnticipatorVisualization(KeyInfo ki)
    {
        keyInfo = ki;        
    }    
}
