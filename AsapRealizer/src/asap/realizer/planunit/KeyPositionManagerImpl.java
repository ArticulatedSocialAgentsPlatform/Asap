package asap.realizer.planunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the KeyPositionManager
 * @author Herwin
 */
public class KeyPositionManagerImpl implements KeyPositionManager
{
    private List<KeyPosition> keys = new ArrayList<KeyPosition>();
    
    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keys.add(kp);
        Collections.sort(keys);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        Collections.sort(keys);
        return Collections.unmodifiableList(keys);        
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keys = p;
        Collections.sort(keys);
    }
    
    @Override
    public KeyPosition getKeyPosition(String kid)
    {
        for(KeyPosition kp:getKeyPositions())
        {
            if(kp.id.equals(kid)) return kp;
        }
        return null;
    }
    
    @Override
    public void removeKeyPosition(String id)
    {
        KeyPosition removePos = null;
        for(KeyPosition kp:getKeyPositions())
        {
            if(kp.id.equals(id))
            {
                removePos = kp;
                break;
            }             
        }
        if(removePos!=null)
        {
            keys.remove(removePos);
        }
    }
}
