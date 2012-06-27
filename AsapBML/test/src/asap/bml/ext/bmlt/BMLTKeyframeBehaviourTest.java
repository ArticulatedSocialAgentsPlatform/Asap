package asap.bml.ext.bmlt;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * unit tests for the BMLTKeyframeBehaviour
 * @author Herwin
 *
 */
public class BMLTKeyframeBehaviourTest extends AbstractBehaviourTest
{

    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmlt:keyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"" + extraAttributeString + "/>";
        return new BMLTKeyframeBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLTKeyframeBehaviour(bmlId, new XMLTokenizer(bmlString));
    }
    
    @Test
    public void testReadXML() throws IOException
    {
        String str = "<bmlt:keyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"/>";
        BMLTKeyframeBehaviour beh = new BMLTKeyframeBehaviour("bml1", new XMLTokenizer(str));
        assertEquals("file1.xml",beh.name);
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String str = "<bmlt:keyframe xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" " + TestUtil.getDefNS()
                + "id=\"kf1\" name=\"file1.xml\"/>";
        BMLTKeyframeBehaviour behIn = new BMLTKeyframeBehaviour("bml1", new XMLTokenizer(str));
        StringBuilder buf = new StringBuilder();
        behIn.appendXML(buf);
        
        BMLTKeyframeBehaviour behOut = new BMLTKeyframeBehaviour("bml1", new XMLTokenizer(buf.toString()));
        assertEquals("file1.xml",behOut.name);
    }
}
