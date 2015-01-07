/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.BMLInfo;
import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.utils.TestUtil;

/**
 * Unit test cases for the bmlt procanimation behavior
 * @author hvanwelbergen
 * 
 */
public class BMLTProcAnimationBehaviourTest extends AbstractBehaviourTest
{
    private static final float PARAMETER_PRECISION = 0.0001f;
    static
    {
        BMLTInfo.init();
    }

    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:procanimation xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS() + 
                "name=\"ani1\" id=\"beh1\"" + extraAttributeString + "/>";
        return new BMLTProcAnimationBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTProcAnimationBehaviour(bmlId, new XMLTokenizer(bmlString));
    }

    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<bmlt:procanimation xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" name=\"ani1\">"
                + "<bmlt:parameter name=\"amplitude\" value=\"10\"/>" + "</bmlt:procanimation>";
        BMLTProcAnimationBehaviour beh = new BMLTProcAnimationBehaviour("bmla", new XMLTokenizer(bmlString));
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);
        assertEquals("ani1", beh.name);
        assertEquals(10, beh.getFloatParameterValue("amplitude"), PARAMETER_PRECISION);
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String bmlString = "<bmlt:procanimation xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" name=\"ani1\">"
                + "<bmlt:parameter name=\"amplitude\" value=\"10\"/>" + "</bmlt:procanimation>";
        BMLTProcAnimationBehaviour behIn = new BMLTProcAnimationBehaviour("bmla", new XMLTokenizer(bmlString));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTProcAnimationBehaviour behOut = new BMLTProcAnimationBehaviour("bmla", new XMLTokenizer(buf.toString()));

        assertEquals("bmla", behOut.getBmlId());
        assertEquals("a1", behOut.id);
        assertEquals("ani1", behOut.name);
        assertEquals(10, behOut.getFloatParameterValue("amplitude"), PARAMETER_PRECISION);
        assertEquals("nod1", behOut.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", behOut.getSyncPoints().get(0).getRef().syncId);
    }

    @Test
    public void testProcAniExtension() throws IOException
    {
        BMLInfo.supportedExtensions.add(BMLTProcAnimationBehaviour.class);
        String bmlString = "<gesture xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"a1\" start=\"nod1:end\" lexeme=\"BEAT\">"
                + "<description priority=\"1\" type=\"procanimation\">"
                + "<bmlt:procanimation xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"a1\" start=\"nod1:end\" name=\"ani1\">"
                + "<bmlt:parameter name=\"amplitude\" value=\"10\"/>" + "</bmlt:procanimation>" + "</description>" + "</gesture>";
        GestureBehaviour g = new GestureBehaviour("bmla", new XMLTokenizer(bmlString));
        assertThat(g.descBehaviour, instanceOf(BMLTProcAnimationBehaviour.class));
        BMLTProcAnimationBehaviour beh = (BMLTProcAnimationBehaviour) g.descBehaviour;
        assertEquals("bmla", beh.getBmlId());
        assertEquals("a1", beh.id);
        assertEquals("ani1", beh.name);
        assertEquals(10, beh.getFloatParameterValue("amplitude"), PARAMETER_PRECISION);
        assertEquals("nod1", beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end", beh.getSyncPoints().get(0).getRef().syncId);
    }

}
