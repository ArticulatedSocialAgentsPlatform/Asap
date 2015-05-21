/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.utils.TestUtil;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmlt.BMLTInfo;

/**
 * Unit tests for BMLABlockProgressFeedback
 * @author hvanwelbergen
 *
 */
public class BMLABlockProgressFeedbackTest
{
    private static final double PRECISION = 0.0001;
    private BMLABlockProgressFeedback fb = new BMLABlockProgressFeedback();
    
    
    @Before
    public void setup()
    {
        BMLTInfo.init();
    }
    
    @Test
    public void testReadXML()
    {
        String str = "<blockProgress " + TestUtil.getDefNS() + " xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " +
        		"bmla:posixTime=\"100\" id=\"bml1:start\" globalTime=\"10\" bmla:status=\"IN_EXEC\" characterId=\"doctor\"/>";
        fb.readXML(str);
        assertEquals("bml1", fb.getBmlId());
        assertEquals("start", fb.getSyncId());
        assertEquals(10, fb.getGlobalTime(), PRECISION);
        assertEquals("doctor", fb.getCharacterId());
        assertEquals(100, fb.getPosixTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, fb.getStatus());
    }
    
    @Test
    public void testWrite()
    {
        BMLABlockProgressFeedback fbIn = new BMLABlockProgressFeedback("bml1", "start", 10, 100, BMLABlockStatus.IN_EXEC);
        StringBuilder buf = new StringBuilder();
        fbIn.appendXML(buf);
        
        BMLABlockProgressFeedback fbOut = new BMLABlockProgressFeedback();
        fbOut.readXML(buf.toString());
        assertEquals(100, fbOut.getPosixTime(), PRECISION);
        assertEquals(BMLABlockStatus.IN_EXEC, fbOut.getStatus());
    }
    
    @Test
    public void testConstruct()
    {
        BMLBlockProgressFeedback fbBML = new BMLBlockProgressFeedback();
        String str = "<blockProgress " + TestUtil.getDefNS() + " xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " +
                "bmla:posixTime=\"100\" id=\"bml1:relax\"  bmla:status=\"SUBSIDING\" globalTime=\"10\" characterId=\"doctor\"/>";
        fbBML.readXML(str);
        fb = BMLABlockProgressFeedback.build(fbBML);
        assertEquals("bml1", fb.getBmlId());
        assertEquals("relax", fb.getSyncId());
        assertEquals(BMLABlockStatus.SUBSIDING, fb.getStatus());
        assertEquals(10, fb.getGlobalTime(), PRECISION);
        assertEquals("doctor", fb.getCharacterId());
        assertEquals(100, fb.getPosixTime(), PRECISION);
    }
    
    @Test
    public void testConstructNoPosixTime()
    {
        BMLBlockProgressFeedback fbBML = new BMLBlockProgressFeedback();
        String str = "<blockProgress " + TestUtil.getDefNS() + " xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " +
                " id=\"bml1:start\" globalTime=\"10\" characterId=\"doctor\"/>";
        fbBML.readXML(str);
        fb = BMLABlockProgressFeedback.build(fbBML);
        assertEquals("bml1", fb.getBmlId());
        assertEquals("start", fb.getSyncId());
        assertEquals(10, fb.getGlobalTime(), PRECISION);
        assertEquals("doctor", fb.getCharacterId());
        assertEquals(0, fb.getPosixTime(), PRECISION);
        assertEquals(BMLABlockStatus.NONE, fb.getStatus());
    }
    
    @Test
    public void testWriteBMLAPrefix()
    {
        BMLABlockProgressFeedback fbIn = new BMLABlockProgressFeedback("bml1", "start", 10, 100, BMLABlockStatus.IN_EXEC);
        assertThat(fbIn.toBMLFeedbackString(),containsString("xmlns:bmla=\"http://www.asap-project.org/bmla\""));        
    }
}
