package asap.picture.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.utils.TestUtil;

/**
 * Unit tests for the AddAnimationDirBehavior
 * @author Herwin
 *
 */
public class AddAnimationDirBehaviorTest extends AbstractBehaviourTest
{
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
}
