/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

/**
 * Contains information to create a TimedMotionUnit. Currently only MURML descriptions are supported.
 * @author hvanwelbergen
 *
 */
public class TimedMotionUnitConstructionInfo extends XMLStructureAdapter
{
    @Getter
    private String type;
    
    @Getter
    private String content;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        type = getRequiredAttribute("type", attrMap, tokenizer);        
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        content = tokenizer.getXMLSection();
    }
    
    static final String XMLTAG = "TimedMotionUnit";

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
