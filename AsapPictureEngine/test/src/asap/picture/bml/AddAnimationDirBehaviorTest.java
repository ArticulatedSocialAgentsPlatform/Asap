/*******************************************************************************
 *******************************************************************************/
package asap.picture.bml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.utils.TestUtil;

/**
 * Unit tests for the AddAnimationDirBehavior
 * @author Herwin
 *
 */
public class AddAnimationDirBehaviorTest extends AbstractBehaviourTest
{
    private static final double PARAMETER_PRECISION = 0.0001;
    
    @Override
    protected AddAnimationDirBehavior createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlp:addAnimationDir xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " +
        		"layer=\"1\" resourcePath=\"\" directoryName=\"\"" + TestUtil.getDefNS()
                + " id=\"beh1\"" + extraAttributeString + "/>";
        return new AddAnimationDirBehavior(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected AddAnimationDirBehavior parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new AddAnimationDirBehavior(bmlId, new XMLTokenizer(bmlString));
    }
    
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlp:addAnimationDir xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " +
                "layer=\"1\" resourcePath=\"rp\" directoryName=\"dn\"" + TestUtil.getDefNS()
                + " id=\"beh1\"/>";
        AddAnimationDirBehavior beh = new AddAnimationDirBehavior("bml1", new XMLTokenizer(str));
        assertEquals(1, beh.getFloatParameterValue("layer"),PARAMETER_PRECISION);
        assertEquals("rp",beh.getStringParameterValue("resourcePath"));
        assertEquals("dn",beh.getStringParameterValue("directoryName"));
        assertEquals("beh1",beh.id);
        assertEquals("bml1",beh.getBmlId());
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlp:addAnimationDir xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " +
                "layer=\"1\" resourcePath=\"rp\" directoryName=\"dn\"" + TestUtil.getDefNS()
                + " id=\"beh1\"/>";
        AddAnimationDirBehavior behIn = new AddAnimationDirBehavior("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        
        AddAnimationDirBehavior behOut = new AddAnimationDirBehavior("bml1", new XMLTokenizer(buf.toString()));
        assertEquals(1, behOut.getFloatParameterValue("layer"),PARAMETER_PRECISION);
        assertEquals("rp",behOut.getStringParameterValue("resourcePath"));
        assertEquals("dn",behOut.getStringParameterValue("directoryName"));
        assertEquals("beh1",behOut.id);
        assertEquals("bml1",behOut.getBmlId());
    }
}
