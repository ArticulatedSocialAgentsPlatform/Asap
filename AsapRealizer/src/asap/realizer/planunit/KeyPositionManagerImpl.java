/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Default implementation of the KeyPositionManager
 * @author Herwin
 */
public class KeyPositionManagerImpl implements KeyPositionManager
{
    private List<KeyPosition> keys = Collections.synchronizedList(new ArrayList<KeyPosition>());

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keys.add(kp);
        synchronized (keys)
        {
            Collections.sort(keys);
        }
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        synchronized (keys)
        {
            Collections.sort(keys);
        }
        return ImmutableList.copyOf(keys);
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keys = p;
        synchronized (keys)
        {
            Collections.sort(keys);
        }
    }

    @Override
    public KeyPosition getKeyPosition(String kid)
    {
        synchronized (keys)
        {
            for (KeyPosition kp : getKeyPositions())
            {
                if (kp.id.equals(kid)) return kp;
            }
        }
        return null;
    }

    @Override
    public void removeKeyPosition(String id)
    {
        KeyPosition removePos = null;

        synchronized (keys)
        {
            for (KeyPosition kp : getKeyPositions())
            {
                if (kp.id.equals(id))
                {
                    removePos = kp;
                    break;
                }
            }
        }
        if (removePos != null)
        {
            keys.remove(removePos);
        }
    }
}
