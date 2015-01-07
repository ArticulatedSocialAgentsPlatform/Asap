/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLScanException;

import org.junit.Test;

/**
 * Unit test cases for SpecConstraints
 * @author Herwin
 *
 */
public class SpecConstraintsTest
{
    private static final int READ_TIMEOUT = 300;
    
    @Test(timeout=READ_TIMEOUT,expected=XMLScanException.class)   
    public void testInvalidInnerElement()
    {
        String str = "<constraints><invalid/></constraints>";
        SpecConstraints constraints = new SpecConstraints();
        constraints.readXML(str);
    }
}
