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
    private final int READ_TIMEOUT = 100;
    
    @Test(timeout=READ_TIMEOUT,expected=XMLScanException.class)    
    public void testInvalidContent()
    {
        String str = "<parameterdefaults><invalid/></parameterdefaults>";
        FBParameterDefaults fb = new FBParameterDefaults(mockFUSpec);
        fb.readXML(str);        
    }
}
