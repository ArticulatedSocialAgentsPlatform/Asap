package asap.picture.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.utils.TestUtil;

/**
 * Unit tests for the AddAnimationXMLBehavior
 * @author Herwin
 *
 */
public class AddAnimationXMLBehaviorTest extends AbstractBehaviourTest
{
    @Override
    protected AddAnimationXMLBehavior createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlp:addAnimationXML xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " +
                "layer=\"1\" filePath=\"\" fileName=\"\"" + TestUtil.getDefNS()
                + " id=\"beh1\"" + extraAttributeString + "/>";
        return new AddAnimationXMLBehavior(bmlId, new XMLTokenizer(str));
    }
    
    @Override
    protected AddAnimationXMLBehavior parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new AddAnimationXMLBehavior(bmlId, new XMLTokenizer(bmlString));
    }    
}
