package asap.ipaacaeventengine.bml;

import static org.junit.Assert.assertEquals;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * Unit tests for IpaacaEventBehaviour
 * @author herwinvw
 *
 */
public class IpaacaEventBehaviourTest extends AbstractBehaviourTest
{
    @Test
    public void testReadXML() throws IOException
    {
        String bmlString = "<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" id=\"e1\" start=\"nod1:end\" >"
                + "<message category=\"cat1\">"
                + "</message>"
                + "</ipaaca:ipaacaevent>";
        IpaacaEventBehaviour beh = new IpaacaEventBehaviour("bml2", new XMLTokenizer(bmlString));
        assertEquals("bml2",beh.getBmlId());
        assertEquals("e1",beh.id);
        assertEquals("nod1",beh.getSyncPoints().get(0).getRef().sourceId);
        assertEquals("end",beh.getSyncPoints().get(0).getRef().syncId);
    }

    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" "+TestUtil.getDefNS()+"id=\"a1\" " + extraAttributeString+">"
                + "<message category=\"cat1\">"
                + "</message>"
                + "</ipaaca:ipaacaevent>";       
                return new IpaacaEventBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new IpaacaEventBehaviour(bmlId,new XMLTokenizer(bmlString));
    }
}
