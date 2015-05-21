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
 * Unit tests for the SetImageBehavior
 * @author Herwin
 * 
 */
public class SetImageBehaviorTest extends AbstractBehaviourTest
{
    @Override
    protected SetImageBehavior createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlp:setImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"\" fileName=\"\""
                + TestUtil.getDefNS() + " id=\"beh1\"" + extraAttributeString + "/>";
        return new SetImageBehavior(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected SetImageBehavior parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new SetImageBehavior(bmlId, new XMLTokenizer(bmlString));
    }

    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlp:setImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"fp\" fileName=\"fn\""
                + TestUtil.getDefNS() + " id=\"beh1\"/>";
        SetImageBehavior beh = new SetImageBehavior("bml1", new XMLTokenizer(str));
        assertEquals("fp", beh.getStringParameterValue("filePath"));
        assertEquals("fn", beh.getStringParameterValue("fileName"));
        assertEquals("beh1", beh.id);
        assertEquals("bml1", beh.getBmlId());
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlp:setImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"fp\" fileName=\"fn\""
                + TestUtil.getDefNS() + " id=\"beh1\"/>";
        SetImageBehavior behIn = new SetImageBehavior("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        
        SetImageBehavior behOut = new SetImageBehavior("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("fp", behOut.getStringParameterValue("filePath"));
        assertEquals("fn", behOut.getStringParameterValue("fileName"));
        assertEquals("beh1", behOut.id);
        assertEquals("bml1", behOut.getBmlId());
    }
}
