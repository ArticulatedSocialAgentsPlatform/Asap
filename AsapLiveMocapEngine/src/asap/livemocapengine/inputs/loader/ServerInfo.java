/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs.loader;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * Parses generic serverinfo XML element<br> 
 * <serverinfo host=".." port=".."/>
 * @author welberge
 *
 */
class ServerInfo extends XMLStructureAdapter
{
    @Getter
    private String hostName;
    @Getter
    private int port;

    private static final String XMLTAG = "serverinfo";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        hostName = getRequiredAttribute("host", attrMap, tokenizer);
        port = getRequiredIntAttribute("port", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
