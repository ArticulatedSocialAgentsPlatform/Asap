/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * unit test cases for the value parser
 * @author hvanwelbergen
 * 
 */
public class ValueTest
{
    Value value = new Value();

    @Test
    public void test()
    {
        String valueScript = "<value xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" "
                + "type=\"start\" name=\"LocLowerChest LocCenterRight LocNorm\"/>";
        value.readXML(valueScript);
        assertEquals("start", value.getType());
        assertEquals("LocLowerChest LocCenterRight LocNorm", value.getName());
    }
}
