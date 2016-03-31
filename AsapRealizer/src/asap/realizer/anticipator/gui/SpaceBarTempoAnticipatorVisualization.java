/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator.gui;


import java.util.Observer;

import asap.realizer.anticipator.Anticipator;

/**
 * Visualizes the anticipated tempo of spacebar presses
 * @author Herwin
 *
 */
public abstract class SpaceBarTempoAnticipatorVisualization implements Observer
{
    protected final Anticipator anticipator;
    
    public SpaceBarTempoAnticipatorVisualization(Anticipator a)
    {
        anticipator = a;
    }
}
