package hmi.elckerlyc.util;

import static org.mockito.Mockito.when;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Mocks up the getKeyPosition(id) and getKeyPositions functionality of a KeyPositionManager
 * @author welberge
 */
public final class KeyPositionMocker
{
    private KeyPositionMocker(){}
    public static void stubKeyPositions(KeyPositionManager muKpm, KeyPosition... keyPositions)
    {
        List<KeyPosition> kps = new ArrayList<KeyPosition>();
        for(KeyPosition keyPos: keyPositions)
        {
            when(muKpm.getKeyPosition(keyPos.id)).thenReturn(keyPos);
            kps.add(keyPos);
        }
        when(muKpm.getKeyPositions()).thenReturn(kps);
    }
}
