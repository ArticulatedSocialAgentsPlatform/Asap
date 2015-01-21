/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;



/**
 * Basejoint specification
 * @author hvanwelbergen
 */
class BaseJoint extends XMLStructureAdapter
{
    @Getter
    private String sid;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        sid = getRequiredAttribute("sid", attrMap, tokenizer);                
    }
    
    public static final String XMLTAG = "basejoint";

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
