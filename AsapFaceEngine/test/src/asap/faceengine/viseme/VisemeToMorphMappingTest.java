/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
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
        assertThat(map.getMorphTargetForViseme(10).getMorphNames(), IsIterableContainingInAnyOrder.containsInAnyOrder("visemetest"));
        assertEquals(1,map.getMorphTargetForViseme(10).intensity,PARAMETER_PRECISION);        
    }
    
    @Test
    public void testReadXML2()
    {
        map.readXML("<VisemeToMorphMapping><Mapping viseme=\"10\" intensity=\"0.5\" target=\"visemetest\"/></VisemeToMorphMapping>");
        assertThat(map.getMorphTargetForViseme(10).getMorphNames(), IsIterableContainingInAnyOrder.containsInAnyOrder("visemetest"));
        assertEquals(0.5,map.getMorphTargetForViseme(10).intensity,PARAMETER_PRECISION);
    }
    
    @Test
    public void testReadXMLMultipleMorphs()
    {
        map.readXML("<VisemeToMorphMapping><Mapping viseme=\"10\" target=\"visemetest,visemetest2\"/></VisemeToMorphMapping>");
        assertThat(map.getMorphTargetForViseme(10).getMorphNames(), IsIterableContainingInAnyOrder.containsInAnyOrder("visemetest","visemetest2"));
        assertEquals(1,map.getMorphTargetForViseme(10).intensity,PARAMETER_PRECISION);
    }
    
    @Test
    public void testGetUsedMorphs()
    {
        map.readXML("<VisemeToMorphMapping>"
                + "<Mapping viseme=\"10\" target=\"vis10a,vis10b\"/>"
                + "<Mapping viseme=\"11\" target=\"vis11\"/>"
                + "<Mapping viseme=\"12\" target=\"vis11\"/>"
                + "</VisemeToMorphMapping>");
        assertThat(map.getUsedMorphs(), IsIterableContainingInAnyOrder.containsInAnyOrder("vis10a","vis10b","vis11"));
        map.getUsedMorphs();
    }
    @Test
    public void testGetNonExistingTarget()
    {
        assertNull(map.getMorphTargetForViseme(1));
    }
}
