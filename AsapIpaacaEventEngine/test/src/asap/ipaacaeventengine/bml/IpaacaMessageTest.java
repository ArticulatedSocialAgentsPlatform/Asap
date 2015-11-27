package asap.ipaacaeventengine.bml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the IpaacaMessage
 * @author hvanwelbergen
 *
 */
public class IpaacaMessageTest
{
    @Test
    public void test()
    {
        String xml="<message category=\"cat1\">"
                + "<payload>"
                + "<item key=\"key1\" value=\"val1\"/>"
                + "<item key=\"key2\" value=\"val2\"/>"
                + "</payload>"
                + "</message>";
        IpaacaMessage message = new IpaacaMessage();
        message.readXML(xml);
        assertEquals("cat1", message.getCategory());
        assertEquals("val1", message.getPayload().get("key1"));
        assertEquals("val2", message.getPayload().get("key2"));
        assertEquals("default", message.getChannel());
    }
    
    @Test
    public void testChannel()
    {
        String xml="<message channel=\"ch1\" category=\"cat1\">"
                + "<payload>"
                + "<item key=\"key1\" value=\"val1\"/>"
                + "<item key=\"key2\" value=\"val2\"/>"
                + "</payload>"
                + "</message>";
        IpaacaMessage message = new IpaacaMessage();
        message.readXML(xml);
        assertEquals("cat1", message.getCategory());
        assertEquals("val1", message.getPayload().get("key1"));
        assertEquals("val2", message.getPayload().get("key2"));
        assertEquals("ch1", message.getChannel());
    }
}
