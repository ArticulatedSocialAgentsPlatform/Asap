/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * Unit tests for BMLTMorphKeyframeBehaviour
 * @author herwinvw
 *
 */
public class BMLTFaceKeyframeBehaviourTest extends AbstractBehaviourTest
{
    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"" + extraAttributeString + "/>";
        return new BMLTFaceKeyframeBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTFaceKeyframeBehaviour(bmlId, new XMLTokenizer(bmlString));
    }
    
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"/>";
        BMLTFaceKeyframeBehaviour beh = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("file1.xml", beh.name);
    }

    @Test
    public void readInternalKeyframe() throws IOException
    {
        String keyframe = "<FaceInterpolator parts=\"morph1\">" + "0 0"
                + "</FaceInterpolator>";
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS() + "id=\"kf1\">" + keyframe
                + "</bmlt:facekeyframe>";
        BMLTFaceKeyframeBehaviour beh = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(str));
        assertEquals(keyframe, beh.content);
    }

    @Test
    public void readInternalKeyframeWithParameters() throws IOException
    {
        String keyframe = "<FaceInterpolator parts=\"morph1\" >" + "0 0"
                + "</FaceInterpolator>";
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS() + "id=\"kf1\">"
                + "<bmlt:parameter name=\"before\" value=\"b1\"/>" + keyframe + "<bmlt:parameter name=\"after\" value=\"a1\"/>"
                + "</bmlt:facekeyframe>";
        BMLTFaceKeyframeBehaviour beh = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(str));
        assertEquals(keyframe, beh.content);
        assertEquals("a1", beh.getStringParameterValue("after"));
        assertEquals("b1", beh.getStringParameterValue("before"));
    }

    @Test
    public void writeInternalKeyframeWithParameters() throws IOException, SAXException
    {
        String keyframe = "<FaceInterpolator parts=\"morph1\">" + "0 0.5"
                + "</FaceInterpolator>";
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS() + "id=\"kf1\">"
                + "<bmlt:parameter name=\"before\" value=\"b1\"/>" + keyframe + "<bmlt:parameter name=\"after\" value=\"a1\"/>"
                + "</bmlt:facekeyframe>";
        BMLTFaceKeyframeBehaviour behIn = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(str));

        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);

        BMLTFaceKeyframeBehaviour behOut = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(buf.toString()));
        XMLTestCase xmlTester = new XMLTestCase("")
        {
        };
        XMLUnit.setIgnoreWhitespace(true);
        xmlTester.assertXMLEqual(keyframe, behOut.content);
        assertEquals("a1", behOut.getStringParameterValue("after"));
        assertEquals("b1", behOut.getStringParameterValue("before"));
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlt:facekeyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"/>";
        BMLTFaceKeyframeBehaviour behIn = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);

        BMLTFaceKeyframeBehaviour behOut = new BMLTFaceKeyframeBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("file1.xml", behOut.name);
    }
}
