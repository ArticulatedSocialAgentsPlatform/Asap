package asap.binding;

import hmi.xml.XMLScanException;
import org.junit.Test;


/**
 * Unit test cases for the SpecParameterDefaults
 * @author Herwin
 *
 */
public class SpecParameterDefaultsTest
{
    private static final int READ_TIMEOUT = 200;
    
    @Test(timeout=READ_TIMEOUT,expected=XMLScanException.class)    
    public void testInvalidContent()
    {
        String str = "<parameterdefaults><invalid/></parameterdefaults>";
        SpecParameterDefaults fb = new SpecParameterDefaults();        
        fb.readXML(str);        
    }
}
