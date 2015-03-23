/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLScanException;

import org.junit.Test;

/**
 * Unit test cases for the SpecParameterMap
 * @author Herwin
 *
 */
public class SpecParameterMapTest
{
    
    private static final int READ_TIMEOUT = 400;
    
    @Test(timeout=READ_TIMEOUT,expected=XMLScanException.class)   
    public void testInvalidInnerElement()
    {
        String str = "<parametermap><invalid/></parametermap>";
        SpecParameterMap map = new SpecParameterMap();
        map.readXML(str);
    }
}
