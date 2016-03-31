/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator.gui;


import java.util.Observer;

import asap.realizer.anticipator.KeyInfo;

/**
 * Visualizes a spacebar press
 * @author Herwin
 */
public abstract class SpacebarAnticipatorVisualization implements Observer
{
    protected KeyInfo keyInfo;
    public SpacebarAnticipatorVisualization(KeyInfo ki)
    {
        keyInfo = ki;        
    }    
}
