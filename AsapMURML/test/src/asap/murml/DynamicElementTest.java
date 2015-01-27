/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import asap.murml.DynamicElement.Type;

/**
 * Unit tests for the dynamicElement
 * @author hvanwelbergen
 * 
 */
public class DynamicElementTest
{
    @Test
    public void testRead()
    {
        // @formatter:off
        String dynElem = 
                "<dynamicElement xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" type=\"via\">"+
                "<value id=\"strokeStart\" name=\"val1\"/>"+
                "<value id=\"stroke1\" name=\"val2\"/>"+
                "<value id=\"stroke2\" name=\"val3\"/>"+
                "<value id=\"strokeEnd\" name=\"val4\"/>"+
                "</dynamicElement>";
        // @formatter:on
        DynamicElement elem = new DynamicElement();
        elem.readXML(dynElem);
        assertEquals(Type.VIA, elem.getType());
        assertEquals("val1", elem.getName("strokeStart"));
        assertEquals("val2", elem.getName("stroke1"));
        assertEquals("val3", elem.getName("stroke2"));
        assertEquals("val4", elem.getName("strokeEnd"));
    }
    
    @Test
    public void testWrite()
    {
     // @formatter:off
        String dynElem = 
                "<dynamicElement xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" type=\"via\">"+
                "<value id=\"strokeStart\" name=\"val1\"/>"+
                "<value id=\"stroke1\" name=\"val2\"/>"+
                "<value id=\"stroke2\" name=\"val3\"/>"+
                "<value id=\"strokeEnd\" name=\"val4\"/>"+
                "</dynamicElement>";
        // @formatter:on
        DynamicElement elemIn = new DynamicElement();
        elemIn.readXML(dynElem);
        StringBuilder buf = new StringBuilder();
        elemIn.appendXML(buf);
        DynamicElement elemOut = new DynamicElement();
        elemOut.readXML(buf.toString());
        
        assertEquals(Type.VIA, elemOut.getType());
        assertEquals("val1", elemOut.getName("strokeStart"));
        assertEquals("val2", elemOut.getName("stroke1"));
        assertEquals("val3", elemOut.getName("stroke2"));
        assertEquals("val4", elemOut.getName("strokeEnd"));
    }
}
