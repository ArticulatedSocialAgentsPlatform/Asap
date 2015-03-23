/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.utils.TestUtil;
import asap.bml.ext.bmlt.BMLTInfo;

/**
 * Unit tests for the BMLAFeedbackParser
 * @author hvanwelbergen
 */
public class BMLAFeedbackParserTest
{
    @Before
    public void setup()
    {
        BMLTInfo.init();
    }
    
    private void assertFeedbackType(Class<?>feedbackType, String str) throws IOException
    {
        BMLFeedback fb = BMLAFeedbackParser.parseFeedback(str);
        assertThat(fb,instanceOf(feedbackType));
    }
    
    @Test
    public void testBMLBlockProgressFeedback() throws IOException
    {
        String str = "<blockProgress "+TestUtil.getDefNS()+
                "id=\"bml1:start\" globalTime=\"10\" characterId=\"doctor\"/>";
        assertFeedbackType(BMLBlockProgressFeedback.class, str);
    }
    
    @Test
    public void testBMLPredictionFeedback() throws IOException
    {
        String feedback = "<predictionFeedback "+TestUtil.getDefNS()+">"
                + "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " +
                        "id=\"bml1\" globalStart=\"1\" globalEnd=\"7\"/>"
                + "<gesture id=\"bml1:gesture1\" lexeme=\"BEAT\" start=\"0\" ready=\"1\" strokeStart=\"3\" " +
                "stroke=\"4\" strokeEnd=\"5\" relax=\"6\" end=\"7\"/>"
                + "<head id=\"bml1:head1\" lexeme=\"NOD\" start=\"0\" ready=\"1\" " +
                "strokeStart=\"3\" stroke=\"4\" strokeEnd=\"5\" relax=\"6\" end=\"7\"/>"
                + "</predictionFeedback>";
        assertFeedbackType(BMLAPredictionFeedback.class, feedback);
    }
    
    @Test
    public void testBMLSyncPointProgressFeedback() throws IOException
    {
        String str = "<syncPointProgress "+TestUtil.getDefNS()+
                " characterId=\"doctor\" id=\"bml1:gesture1:stroke\" time=\"10\" globalTime=\"111\"/>";
        assertFeedbackType(BMLASyncPointProgressFeedback.class, str);
    }
    
    @Test
    public void testBMLWarningFeedback() throws IOException
    {
        String str = "<warningFeedback "+TestUtil.getDefNS()+"id=\"bml1\" characterId=\"doctor\" type=\"PARSING_FAILURE\">content</warningFeedback>";
        assertFeedbackType(BMLWarningFeedback.class, str);
    }
    
    @Test(timeout=200)
    public void testLoop() throws IOException
    {
        String str = "<predictionFeedback xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">"+
        "<unknown id=\"unknown\"/>"+        
        "</predictionFeedback>";
        assertFeedbackType(BMLAPredictionFeedback.class, str);
    }
}
