/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.utils.TestUtil;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmlt.BMLTInfo;

/**
 * Unit tests for BMLABlockPredictionFeedback
 * @author hvanwelbergen
 *
 */
public class BMLABlockPredictionFeedbackTest
{
    private static final double PRECISION = 0.0001;
    private BMLABlockPredictionFeedback fb = new BMLABlockPredictionFeedback();
    
    @Before
    public void setup()
    {
        BMLTInfo.init();
    }
    
    @Test
    public void testReadXML()
    {
        String str = "<bml " + TestUtil.getDefNS() + " id=\"bml1\" globalStart=\"1\" globalEnd=\"2\"" +
                "xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" bmla:status=\"IN_PREP\"" + " bmla:posixStartTime=\"100\" "+"bmla:posixEndTime=\"200\" "+ 
        		"/>";
        fb.readXML(str);
        assertEquals("bml1", fb.getId());
        assertEquals(1, fb.getGlobalStart(), PRECISION);
        assertEquals(2, fb.getGlobalEnd(), PRECISION);
        assertEquals(100, fb.getPosixStartTime(), PRECISION);
        assertEquals(200, fb.getPosixEndTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, fb.getStatus());
    }
    
    @Test
    public void testWrite()
    {
        BMLABlockPredictionFeedback fbIn = new BMLABlockPredictionFeedback("bml1", 0, 1, BMLABlockStatus.IN_PREP, 100, 200);
        StringBuilder buf = new StringBuilder();
        fbIn.appendXML(buf);
        
        BMLABlockPredictionFeedback fbOut = new BMLABlockPredictionFeedback();
        fbOut.readXML(buf.toString());
        assertEquals(100, fbOut.getPosixStartTime(), PRECISION);
        assertEquals(200, fbOut.getPosixEndTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_PREP, fbOut.getStatus());
    }
    
    @Test
    public void testWriteBMLAPrefix()
    {
        BMLABlockPredictionFeedback fbIn = new BMLABlockPredictionFeedback("bml1", 0, 1, BMLABlockStatus.IN_PREP, 100, 200);
        assertThat(fbIn.toBMLFeedbackString(),containsString("xmlns:bmla=\"http://www.asap-project.org/bmla\""));        
    }
    
    @Test
    public void testBuild()
    {
        BMLBlockPredictionFeedback fbBML = new BMLBlockPredictionFeedback();
        String str = "<bml " + TestUtil.getDefNS() + " id=\"bml1\" globalStart=\"1\" globalEnd=\"2\"" +
                "xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" bmla:status=\"IN_PREP\"" + " bmla:posixStartTime=\"100\" "+"bmla:posixEndTime=\"200\" "+ 
                "/>";
        fbBML.readXML(str);
        fb = BMLABlockPredictionFeedback.build(fbBML);
        assertEquals("bml1", fb.getId());
        assertEquals(1, fb.getGlobalStart(), PRECISION);
        assertEquals(2, fb.getGlobalEnd(), PRECISION);
        assertEquals(100, fb.getPosixStartTime(), PRECISION);
        assertEquals(200, fb.getPosixEndTime(), PRECISION);        
        assertEquals(BMLABlockStatus.IN_PREP, fb.getStatus());
    }
}
