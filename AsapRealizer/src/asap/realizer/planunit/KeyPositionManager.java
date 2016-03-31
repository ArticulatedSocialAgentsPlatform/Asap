/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.List;

/**
 * Contains generic KeyPosition management functions. 
 * @author Herwin
 */
public interface KeyPositionManager
{
    void addKeyPosition(KeyPosition kp);
    
    /**
     * Gets a sorted view of the list of KeyPositions
     */
    List<KeyPosition> getKeyPositions();
    
    void setKeyPositions(List<KeyPosition> p);
    KeyPosition getKeyPosition(String id);    
    
    void removeKeyPosition(String id);
}
