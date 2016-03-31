/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for SpecParameterDefault
 * @author Herwin
 *
 */
public class SpecParameterDefaultTest
{
    @Test
    public void test()
    {
        String str = "<parameterdefault name=\"namex\" value=\"valx\"/>";
        SpecParameterDefault p = new SpecParameterDefault();
        p.readXML(str);
        assertEquals("namex", p.name);
        assertEquals("valx", p.value);
    }
}
