/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.utils.TestUtil;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmlt.BMLTInfo;


/**
 * Unit tests for BMLASyncPointProgressFeedbackTest
 * @author hvanwelbergen
 *
 */
public class BMLASyncPointProgressFeedbackTest
{
    private static final double PRECISION = 0.0001;
    private BMLASyncPointProgressFeedback fb = new BMLASyncPointProgressFeedback();
        
    @Before
    public void setup()
    {
        BMLTInfo.init();
    }
    
    @Test
    public void testReadXML()
    {
        String str = "<syncPointProgress " + TestUtil.getDefNS() 
                + " characterId=\"doctor\" id=\"bml1:gesture1:stroke\" "+"xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " 
        +"time=\"10\" globalTime=\"111\" bmla:posixTime=\"100\"/>";
        fb.readXML(str);
        assertEquals("doctor", fb.getCharacterId());
        assertEquals(10, fb.getTime(), PRECISION);
        assertEquals(111, fb.getGlobalTime(), PRECISION);
        assertEquals(100, fb.getPosixTime(), PRECISION);
    }
    
    @Test
    public void testWrite()
    {
        BMLASyncPointProgressFeedback fbIn = new BMLASyncPointProgressFeedback("bml1", "beh", "start", 0, 1, 100);
        StringBuilder buf = new StringBuilder();
        fbIn.appendXML(buf);
        
        BMLASyncPointProgressFeedback fbOut = new BMLASyncPointProgressFeedback();
        fbOut.readXML(buf.toString());
        assertEquals(100, fbOut.getPosixTime(), PRECISION);
    }
    
    @Test
    public void testWriteBMLAPrefix()
    {
        BMLASyncPointProgressFeedback fbIn = new BMLASyncPointProgressFeedback("bml1", "beh", "start", 0, 1, 100);
        assertThat(fbIn.toBMLFeedbackString(),containsString("xmlns:bmla=\"http://www.asap-project.org/bmla\""));        
    }
    
    @Test
    public void testBuild()
    {
        BMLSyncPointProgressFeedback fbBML = new BMLSyncPointProgressFeedback();
        String str = "<syncPointProgress " + TestUtil.getDefNS() 
                + " characterId=\"doctor\" id=\"bml1:gesture1:stroke\" "+"xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " 
        +"time=\"10\" globalTime=\"111\" bmla:posixTime=\"100\"/>";
        fbBML.readXML(str);
        fb = BMLASyncPointProgressFeedback.build(fbBML);
        assertEquals("doctor", fb.getCharacterId());
        assertEquals(10, fb.getTime(), PRECISION);
        assertEquals(111, fb.getGlobalTime(), PRECISION);
        assertEquals(100, fb.getPosixTime(), PRECISION);
    }
}
