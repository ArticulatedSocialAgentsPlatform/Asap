package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * Unit tests for the BMLTProcAnimationGestureBehaviour
 * @author hvanwelbergen
 *
 */
public class BMLTProcAnimationGestureBehaviourTest extends AbstractBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;
    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:procanimationgesture xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS() + 
               "id=\"beh1\"" + extraAttributeString + "><ProcAnimation></ProcAnimation></bmlt:procanimationgesture>";
        return new BMLTProcAnimationGestureBehaviour(bmlId, new XMLTokenizer(str));
    }
    
    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTProcAnimationGestureBehaviour(bmlId, new XMLTokenizer(bmlString));
    }
    
    @Test
    public void testReadXMLProcAniInternal() throws IOException
    {
        String bmlString = "<bmlt:procanimationgesture xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\">"
                + "<bmlt:parameter name=\"amplitude\" value=\"10\"/>" +"<ProcAnimation></ProcAnimation>" +"</bmlt:procanimationgesture>";
        BMLTProcAnimationGestureBehaviour beh = new BMLTProcAnimationGestureBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);        
        assertEquals(10, beh.getFloatParameterValue("amplitude"), PARAMETER_PRECISION);
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
        assertEquals("<ProcAnimation></ProcAnimation>", beh.getContent());
    }
    
    @Test
    public void testReadXMLProcAniFile() throws IOException
    {
        String bmlString = "<bmlt:procanimationgesture xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" fileName=\"procani.xml\" start=\"nod1:end\"/>";                
        BMLTProcAnimationGestureBehaviour beh = new BMLTProcAnimationGestureBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);        
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
        assertNull(beh.getContent());
        assertEquals("procani.xml",beh.getFileName());
    }
}
