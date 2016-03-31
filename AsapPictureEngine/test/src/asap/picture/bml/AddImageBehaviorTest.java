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
 * Unit tests for the AddImageBehavior
 * @author Herwin
 *
 */
public class AddImageBehaviorTest extends AbstractBehaviourTest
{
    private static final double PARAMETER_PRECISION = 0.0001;
    
    @Override
    protected AddImageBehavior createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlp:addImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " +
                "layer=\"1\" filePath=\"\" fileName=\"\"" + TestUtil.getDefNS()
                + " id=\"beh1\"" + extraAttributeString + "/>";
        return new AddImageBehavior(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected AddImageBehavior parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new AddImageBehavior(bmlId,new XMLTokenizer(bmlString));
    }

    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlp:addImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"fp\" fileName=\"fn\" layer=\"1\""
                + TestUtil.getDefNS() + " id=\"beh1\"/>";
        AddImageBehavior beh = new AddImageBehavior("bml1", new XMLTokenizer(str));
        assertEquals("fp", beh.getStringParameterValue("filePath"));
        assertEquals("fn", beh.getStringParameterValue("fileName"));
        assertEquals(1, beh.getFloatParameterValue("layer"), PARAMETER_PRECISION);
        assertEquals("beh1", beh.id);
        assertEquals("bml1", beh.getBmlId());
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlp:addImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"fp\" fileName=\"fn\" layer=\"1\""
                + TestUtil.getDefNS() + " id=\"beh1\"/>";
        AddImageBehavior behIn = new AddImageBehavior("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        
        AddImageBehavior behOut = new AddImageBehavior("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("fp", behOut.getStringParameterValue("filePath"));
        assertEquals("fn", behOut.getStringParameterValue("fileName"));
        assertEquals(1, behOut.getFloatParameterValue("layer"), PARAMETER_PRECISION);
        assertEquals("beh1", behOut.id);
        assertEquals("bml1", behOut.getBmlId());
    }
}
