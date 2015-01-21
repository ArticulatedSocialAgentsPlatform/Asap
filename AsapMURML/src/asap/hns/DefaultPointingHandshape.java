/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

public class DefaultPointingHandshape extends XMLStructureAdapter 
{
	@Getter
	private String name;
	
	@Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);                
    }
    
    public static final String XMLTAG = "defaultPointingHandshape";

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
