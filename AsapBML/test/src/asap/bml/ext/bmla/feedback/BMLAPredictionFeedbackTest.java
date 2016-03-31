/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import saiba.utils.TestUtil;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmlt.BMLTInfo;

/**
 * Unit tests for BMLAPredictionFeedback
 * @author hvanwelbergen
 *
 */
public class BMLAPredictionFeedbackTest
{
    @Test
    public void testReadFromXML()
    {
        BMLTInfo.init();
        String feedback = "<predictionFeedback "+" xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" "+TestUtil.getDefNS()+">"
                + "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " +
                        "id=\"bml1\" bmla:posixStartTime=\"1\" bmla:posixEndTime=\"2\"  globalStart=\"1\" globalEnd=\"7\"/>"
                + "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " +
                "id=\"bml2\" bmla:posixStartTime=\"3\" bmla:posixEndTime=\"4\" globalStart=\"1\" globalEnd=\"7\"/>"
                + "</predictionFeedback>";
        BMLAPredictionFeedback fb = new  BMLAPredictionFeedback();
        fb.readXML(feedback);
        assertEquals(2, fb.getBMLABlockPredictions().size());
        assertEquals(1, fb.getBMLABlockPredictions().get(0).getPosixStartTime());
        assertEquals(2, fb.getBMLABlockPredictions().get(0).getPosixEndTime());
        assertEquals(3, fb.getBMLABlockPredictions().get(1).getPosixStartTime());
        assertEquals(4, fb.getBMLABlockPredictions().get(1).getPosixEndTime());        
    }
    
    @Test
    public void testWriteBMLAPrefix()
    {
        BMLAPredictionFeedback fb = new  BMLAPredictionFeedback();
        assertThat(fb.toBMLFeedbackString(),containsString("xmlns:bmla=\"http://www.asap-project.org/bmla\""));        
    }
}
