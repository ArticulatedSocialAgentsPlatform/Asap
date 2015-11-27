package asap.ipaacaeventengine.bml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

/**
 * XML encoding for an ipaaca-message
 * @author hvanwelbergen
 *
 */
public class IpaacaMessage extends XMLStructureAdapter
{
    @Getter
    private String category = "";
    
    @Getter
    private String channel = "default";
            
    private Map<String, String> payload;

    public ImmutableMap<String, String> getPayload()
    {
        return ImmutableMap.copyOf(payload);
    }
    
    private static class Item extends XMLStructureAdapter
    {
        @Getter
        private String key = "";
        @Getter
        private String value = "";

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            key = getRequiredAttribute("key", attrMap, tokenizer);
            value = getRequiredAttribute("value", attrMap, tokenizer);
            super.decodeAttributes(attrMap, tokenizer);
        }

        private static final String XMLTAG = "item";

        public static String xmlTag()
        {
            return XMLTAG;
        }

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    private static class Payload extends XMLStructureAdapter
    {
        private final Map<String, String> map;

        public Payload(Map<String, String> map)
        {
            this.map = map;
        }

        @Override
        public void decodeContent(XMLTokenizer tokenizer) throws IOException
        {
            while (tokenizer.atSTag())
            {
                String tag = tokenizer.getTagName();
                if (tag.equals(Item.xmlTag()))
                {
                    Item item = new Item();
                    item.readXML(tokenizer);
                    map.put(item.getKey(), item.getValue());
                }
                else
                {
                    throw new XMLScanException("Unknown tag " + tag);
                }
            }
        }

        private static final String XMLTAG = "payload";

        public static String xmlTag()
        {
            return XMLTAG;
        }

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        payload = new HashMap<String, String>();
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Payload.xmlTag()))
            {
                Payload pl = new Payload(payload);
                pl.readXML(tokenizer);
            }
            else
            {
                throw new XMLScanException("Unknown tag " + tag);
            }
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        category = getRequiredAttribute("category", attrMap, tokenizer);
        channel = getOptionalAttribute("channel",attrMap,"default");        
    }

    private static final String XMLTAG = "message";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
