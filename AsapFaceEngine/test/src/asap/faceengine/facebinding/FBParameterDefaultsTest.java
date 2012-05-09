package asap.faceengine.facebinding;

import hmi.xml.XMLScanException;

import org.junit.Test;
import static org.mockito.Mockito.mock;
/**
 * Unit tests for FBParameterDefaults
 * @author welberge
 *
 */
public class FBParameterDefaultsTest
{
    private FaceUnitSpec mockFUSpec = mock(FaceUnitSpec.class);
    
    @Test(timeout=100,expected=XMLScanException.class)    
    public void testInvalidContent()
    {
        String str = "<parameterdefaults><invalid/></parameterdefaults>";
        FBParameterDefaults fb = new FBParameterDefaults(mockFUSpec);
        fb.readXML(str);        
    }
}
