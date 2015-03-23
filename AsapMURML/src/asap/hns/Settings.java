/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * hns settings parser
 * @author hvanwelbergen
 *
 */
public class Settings extends XMLStructureAdapter
{
    @Getter
    private Map<String,String> settings = new HashMap<>();
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if(tag.equals(Setting.xmlTag()))
            {
                Setting s = new Setting();
                s.readXML(tokenizer);   
                settings.put(s.getName(),s.getValue());
            }            
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <hns>");
            }
        }
    }    
    
    public static final String XMLTAG = "settings";    
    
    public static final String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
