/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * Unit tests for the BMLTNoiseBehaviour
 * @author Herwin
 * 
 */
public class BMLTNoiseBehaviourTest extends AbstractBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;

    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:noise xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" type=\"perlin\" joint=\"j1\"" + TestUtil.getDefNS()
                + " id=\"beh1\"" + extraAttributeString + "/>";
        return new BMLTNoiseBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTNoiseBehaviour(bmlId, new XMLTokenizer(bmlString));
    }

    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlt:noise xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" "
                + "id=\"noise1\" type=\"perlin\" joint=\"vl5\" start=\"0\" end=\"100\">"
                + "<bmlt:parameter name=\"basefreqx\" value=\"0.5\"/>" + "<bmlt:parameter name=\"baseamplitudex\" value=\"0.05\"/>"
                + "</bmlt:noise>";
        BMLTNoiseBehaviour noiseBeh = new BMLTNoiseBehaviour("bml1", new XMLTokenizer(str));
        assertEquals(0.5, noiseBeh.getFloatParameterValue("basefreqx"), PARAMETER_PRECISION);
        assertEquals(0.05, noiseBeh.getFloatParameterValue("baseamplitudex"), PARAMETER_PRECISION);
        assertEquals("vl5", noiseBeh.getStringParameterValue("joint"));
        assertEquals("perlin", noiseBeh.getStringParameterValue("type"));
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlt:noise xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" "
                + "id=\"noise1\" type=\"perlin\" joint=\"vl5\" start=\"0\" end=\"100\">"
                + "<bmlt:parameter name=\"basefreqx\" value=\"0.5\"/>" + "<bmlt:parameter name=\"baseamplitudex\" value=\"0.05\"/>"
                + "</bmlt:noise>";
        BMLTNoiseBehaviour noiseIn = new BMLTNoiseBehaviour("bml1", new XMLTokenizer(str));
        
        StringBuilder buf = new StringBuilder();
        noiseIn.appendXML(buf);
        BMLTNoiseBehaviour noiseOut = new BMLTNoiseBehaviour("bml1",new XMLTokenizer(buf.toString()));
        assertEquals(0.5, noiseOut.getFloatParameterValue("basefreqx"), PARAMETER_PRECISION);
        assertEquals(0.05, noiseOut.getFloatParameterValue("baseamplitudex"), PARAMETER_PRECISION);
        assertEquals("vl5", noiseOut.getStringParameterValue("joint"));
        assertEquals("perlin", noiseOut.getStringParameterValue("type"));
    }
}
