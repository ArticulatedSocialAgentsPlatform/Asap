package hmi.elckerlyc.anticipator.gui;

import hmi.elckerlyc.anticipator.Anticipator;

import java.util.Observer;

public abstract class SpaceBarTempoAnticipatorVisualization implements Observer
{
    protected final Anticipator anticipator;
    
    public SpaceBarTempoAnticipatorVisualization(Anticipator a)
    {
        anticipator = a;
    }
}
