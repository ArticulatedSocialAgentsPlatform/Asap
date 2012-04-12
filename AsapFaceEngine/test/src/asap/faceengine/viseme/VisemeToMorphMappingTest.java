package asap.faceengine.viseme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
/**
 * Unit tests for VisemeToMorphMapping
 * @author welberge
 *
 */
public class VisemeToMorphMappingTest
{
    private VisemeToMorphMapping map = new VisemeToMorphMapping();
    private static final double PARAMETER_PRECISION = 0.0001;
    @Test
    public void testReadXML()
    {
        map.readXML("<VisemeToMorphMapping><Mapping viseme=\"10\" target=\"visemetest\"/></VisemeToMorphMapping>");
        assertEquals("visemetest",map.getMorphTargetForViseme(10).morphName);
        assertEquals(1,map.getMorphTargetForViseme(10).intensity,PARAMETER_PRECISION);
    }
    
    @Test
    public void testReadXML2()
    {
        map.readXML("<VisemeToMorphMapping><Mapping viseme=\"10\" intensity=\"0.5\" target=\"visemetest\"/></VisemeToMorphMapping>");
        assertEquals("visemetest",map.getMorphTargetForViseme(10).morphName);
        assertEquals(0.5,map.getMorphTargetForViseme(10).intensity,PARAMETER_PRECISION);
    }
    
    @Test
    public void testGetNonExistingTarget()
    {
        assertNull(map.getMorphTargetForViseme(1));
    }
}
