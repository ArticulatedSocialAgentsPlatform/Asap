package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

/**
 * MURML Gesture parser
 * @author hvanwelbergen
 */
public class Gesture extends MURMLElement
{
    @Getter
    private String id;
    
    @Getter
    private String scope;
    
    @Getter
    private List<Function>functions = new ArrayList<Function>();
    
    private Constraints constraints;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        id = getRequiredAttribute("id", attrMap, tokenizer);
        scope = getOptionalAttribute("scope", attrMap);
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if(tag.equals(Function.xmlTag()))
            {
                Function f = new Function();
                f.readXML(tokenizer);
                functions.add(f);
            }            
            else if(tag.equals(Constraints.xmlTag()))
            {
                constraints = new Constraints();
                constraints.readXML(tokenizer);
            }
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <gesture>");
            }
        }
    }
    
    private static final String XMLTAG = "gesture";

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
